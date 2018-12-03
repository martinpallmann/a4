package a4.config

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.io.Source

object ConfigParser {

  sealed trait ConfigElement
  trait Section extends ConfigElement
  object EmptySection extends Section
  case class NonEmptySection(name: String) extends Section {
    override def toString: String = s"[$name]"
  }
  case class KeyValue(key: String, value: String) extends ConfigElement {
    def asTuple: (String, String) = (key, value)
  }
  object Section {
    def unapply(s: String): Option[Section] =
      if (s.startsWith("[") && s.endsWith("]"))
        Some(NonEmptySection(s.substring(1, s.length - 1).trim))
      else
        None
  }
  object KeyValue {
    def unapply(s: String): Option[KeyValue] = {
      val eqIndex = s.indexOf('=')
      if (eqIndex > -1) s.splitAt(eqIndex) match {
        case (k, _) if k.trim.isEmpty => None
        case (k, v) => Some(KeyValue(k.trim, v.trim.substring(1).trim))
      }
      else None
    }
  }

  def removeComments(line: String): String = {
    val cIndex = line.indexOf('#')
    if (cIndex > -1) line.substring(0, cIndex).trim
    else line.trim
  }

  def parseLine(line: String): Option[ConfigElement] = removeComments(line) match {
    case Section(x) => Some(x)
    case KeyValue(x) => Some(x)
    case _ => None
  }

  def parseDefaultConfig: Option[Config] =
    getDefaultConfigFile.map(ConfigParser.parse)

  def getDefaultConfigFileUnsafe: File = {
    val p = System.getProperties.asScala
    val home = p("user.home")
    Paths.get(home, ".config", "a4").toFile
  }

  def getDefaultConfigFile: Option[File] =  {
    val f = getDefaultConfigFileUnsafe
    if (f.exists()) {
      Some(f)
    } else {
      None
    }
  }

  def writeConfig(c: Config): Unit = {
    val f = getDefaultConfigFileUnsafe
    f.createNewFile()
    if (f.exists()) {
      f.delete()
    }
    val w = new BufferedWriter(new FileWriter(f))
    w.write(c.writeConfig)
    w.close()
  }

  def parseDefaultConfigMap: Map[Section, Map[String, String]] =
    getDefaultConfigFile
      .map(parseMap)
      .getOrElse(Map.empty)
      .withDefaultValue(Map.empty)

  def parseMap(f: File): Map[Section, Map[String, String]] = {
    val config = Source.fromFile(f).getLines().flatMap(parseLine)
    val res = config.foldLeft(List.empty[(Section, List[KeyValue])]) {
      case (acc,    s: Section ) => (s, Nil) :: acc
      case (Nil,    x: KeyValue) => (EmptySection, List(x)) :: Nil
      case (h :: t, x: KeyValue) => (h._1, x :: h._2) :: t
      case (acc,    _          ) => acc
    }
    res.toMap.mapValues(_.map(_.asTuple).toMap)
  }

  def parse(f: File): Config = Config(parseMap(f))
}
