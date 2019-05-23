import $ivy.`ch.epfl.scala::mill-bloop:1.1.1`

import java.io.{File, PrintWriter}

import mill._
import os.{Path, RelPath}
import scalalib._

object core extends ScalaModule {
  def scalaVersion = "2.12.7"
  def mainClass = Some("a4.Main")
  def ivyDeps = Agg(
    ivy"com.github.scopt::scopt:3.7.0",
    ivy"org.apache.xmlgraphics:fop:2.3",
    ivy"com.googlecode.lanterna:lanterna:3.0.1"
  )
  def moduleDeps = Seq()
  def resourceBundles = Seq("a4.letter.Resources")
  def createI18n(in: Seq[PathRef], dest: Path): PathRef = {
    import java.io.{BufferedWriter, FileReader, FileWriter}
    import java.util.Properties
    import scala.collection.JavaConverters._

    def split(fqn: String): (String, String) = {
      def r(pn: String, elems: List[String]): (String, String) = elems match {
        case Nil => throw new RuntimeException("ooopsie")
        case cn :: Nil => pn -> cn
        case x :: xs if pn.isEmpty => r(x, xs)
        case x :: xs => r(s"$pn.$x", xs)
      }
      r("", fqn.split('.').toList)
    }

    def asPath(root: Path, pn: String): Path = pn.split('.').foldLeft(root) {
      case (acc, elem) => acc / elem
    }

    val rbs = in.flatMap(
      x => resourceBundles.map(rb => {
        val (pn, cn) = split(rb)
        (pn, cn, (asPath(x.path, pn) / s"$cn.properties").toIO)
      })
    )

    def explode(it: Iterator[AnyRef]): List[List[String]] =
      it.toList.map(x => x.toString.split('.').toList)

    case class Obj(fields: List[String], children: List[(String, Obj)]) {
      def write(tab: Int, out: BufferedWriter): Unit = {
        fields
          .filterNot(x => children.map(_._1).contains(x))
          .foreach(x =>
            out.write("  " * tab + s"""def $x: String = __rb.getString("$x")\n"""))
        children
          .foreach({ case (name, obj) =>
            out.write("  " * tab + s"""object $name {\n""")
            obj.write(tab + 1, out)
            out.write("  " * tab + s"""}\n""")
        })
        out.flush()
      }
    }

    def mkos(xs: List[List[String]]): Obj = {
      val (a1, a2) = xs.filter(_.nonEmpty).partition(_.size == 1)
      Obj(
        a1.map(x => x.head),
        a2.map(x => x.head -> mkos(a2.map(_.tail)))
      )
    }

    rbs
      .filter {
        case (_, _, f) =>
          val res = f.exists()
          if (!res) {
            println("didn't find file: $x")
          }
          res
      }
      .foreach {
        case (pn, cn, f) =>
          val fcn = if (pn.isEmpty) cn else s"$pn.$cn"
          val props = new Properties()
          props.load(new FileReader(f))
          val outFile = (asPath(dest, pn) / s"$cn.scala").toIO
          outFile.getParentFile.mkdirs()
          val writer = new BufferedWriter(new FileWriter(outFile))
          writer.write(
            s"""// This class is generated.
              |package $pn
              |
              |class $cn(__rb: java.util.ResourceBundle) {
              |""".stripMargin)
          val os = mkos(explode(props.keys().asScala))
          os.write(1, writer)
          writer.write(
            s"""}
              |
              |object $cn {
              |  def apply(locale: java.util.Locale): $cn =
              |    new $cn(java.util.ResourceBundle.getBundle("$fcn", locale))
              |}
            """.stripMargin)
          writer.close()
      }
    PathRef(dest)
  }
  def generatedSources = T {
    Seq(createI18n(resources(), T.ctx.dest))
  }
}

