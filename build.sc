import mill._
import mill.eval.Evaluator
import scalalib._

object core extends ScalaModule {
  def scalaVersion = "2.12.7"
  def mainClass = Some("a4.Main")
  def ivyDeps = Agg(
    ivy"com.github.scopt::scopt:3.7.0",
    ivy"org.apache.xmlgraphics:fop:2.3",
    ivy"com.googlecode.lanterna:lanterna:3.0.1"
  )
}

