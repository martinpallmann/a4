package scopt

import a4.Args
import a4.Args.Node

object EvilHack {
  def toTree(os: List[OptionDef[_, _]]): List[Node] = {
    val (parents, children) = os.partition(_.getParentId.isEmpty)
    def asNode(x: OptionDef[_, _]): Node = {
        Node(
          x.kind == Arg,
          x.fullName,
          if (x.desc.nonEmpty) Some(x.desc) else None,
          findChildren(x.id),
          None
        )
    }
    def findChildren(pId: Int): List[Node] = {
      children
        .filter(_.getParentId.contains(pId))
        .map(asNode)
    }
    def setParentForChildren(n: Node): Node = {
      val newChildren = n.children.map(x =>
        setParentForChildren(x.copy(parent = Some(n)))
      )
      n.copy(children = newChildren)
    }
    def explodeArgs(nodes: List[Node]): List[Node] = {
      nodes.flatMap(x => {
        if (x.isArg) {
          Args.getNodes(x.fqn)
        } else {
          List(x.copy(children = explodeArgs(x.children)))
        }
      })
    }
    explodeArgs(parents.map(asNode).map(setParentForChildren))
  }
}
