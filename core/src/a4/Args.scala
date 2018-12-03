package a4

import java.io.File

import a4.Args.{Command, NoCommand}
import scopt.{EvilHack, Read}

case class Args(command: Command = NoCommand)

object Args {

  import Node._

  case class Node(isArg: Boolean, name: String, description: Option[String], children: List[Node], parent: Option[Node]) {
    def fqn: String =
      parent.fold("")(x => s"${sanitize(x.fqn)}/") + sanitize(name)
  }

  object Node {
    def sanitize(s: String): String = s
  }

  def str(tree: List[Node]): String = {
    def r(in: Int, n: Node): String = {
      if (n.children.isEmpty) {
        "  ".repeat(in) + n.name + "\n"
      } else {
        "  ".repeat(in) + n.name + "\n" +
        n.children.map(x => r(in + 1, x)).mkString
      }

    }
    tree.map(x => r(0, x)).mkString
  }

  case class Parser[C](name: String) extends scopt.OptionParser[C](name) {
    def asTree: List[Node] = {
      EvilHack.toTree(options.toList)
    }
  }

  case class KV(key: String, value: String)

  def getNodes(fqn: String): List[Node] = {
    if (fqn == "config/--add/key value") {
      List(
        Node(false, "user.name", Some("default name of the sender"), Nil, None),
        Node(false, "user.street", Some("default street of the sender"), Nil, None),
        Node(false, "user.zip", Some("default zip of the sender"), Nil, None),
        Node(false, "user.city", Some("default city of the sender"), Nil, None)
      )
    } else {
      Nil
    }
  }

  object KV {
    implicit def read: Read[KV] = new Read[KV] {
      var k: Option[String] = None
      def arity: Int = 2
      def reads: String => KV = s => {
        if (k.isEmpty) {
          k = Some(s)
          KV(s, "")
        } else {
          KV(k.get, s)
        }
      }
    }
  }

  sealed trait Command {
    def run(): Unit
  }
  case object NoCommand extends Command {
    def run(): Unit = {}
  }
  case class ConfigCommand(action: ConfigCommandAction = NoConfigAction) extends Command {
    def run(): Unit = action match {
      case ListConfigAction => config.Main.list()
      case a: AddConfigAction => config.Main.add(a.key, a.value)
      case _ => println("error. no action specified")
    }
  }
  case class PrintCommand(
      out: Option[File] = None,
      force: Boolean = false,
      background: Int = 0
  ) extends Command {
    def run(): Unit = {
      a4.Main.renderMain(out, force, background)
    }
  }

  sealed trait ConfigCommandAction
  case object NoConfigAction extends ConfigCommandAction
  case object ListConfigAction extends ConfigCommandAction
  case class AddConfigAction(key: String, value: String) extends ConfigCommandAction

  def parser: Parser[Args] = new Parser[Args]("a4") {
    cmd("letter")
      .text("write a letter")
      .action((_, args) =>
        args.copy(command = PrintCommand()))
      .children(
        opt[File]('o', "out")
          .action((f, args) => {
            args.command match {
              case c: PrintCommand =>
                args.copy(command = c.copy(out = Some(f)))
              case _ =>
                args
            }
          }),
        opt[Int]('b', "background")
          .action((b, args) => {
            args.command match {
              case c: PrintCommand =>
                args.copy(command = c.copy(background = b))
              case _ =>
                args
            }
          }),
        opt[Unit]('f', "force")
          .action((_, args) => {
            args.command match {
              case c: PrintCommand =>
                args.copy(command = c.copy(force = true))
              case _ =>
                args
            }
          })
      )
    cmd("config")
      .text("configure default settings")
      .action((_, args) =>
        args.copy(command = ConfigCommand()))
      .children(
        opt[Unit]('l', "list")
          .text("list configuration options")
          .action((_, args) => {
            args.command match {
              case c: ConfigCommand =>
                args.copy(command = c.copy(action = ListConfigAction))
              case _ =>
                args
            }
          }),
        opt[Unit]("add")
          .text("add or replace configuration option")
          .children(
            arg[KV]("key value")
              .minOccurs(2)
              .maxOccurs(2)
              .action((a, args) => {
                args.command match {
                  case c: ConfigCommand =>
                    args.copy(command = c.copy(action = AddConfigAction(a.key, a.value)))
                  case _ =>
                    args
                }
              })
          )

      )
  }

  def apply(args: Array[String]): Option[Args] =
    parser.parse(args, Args())
}
