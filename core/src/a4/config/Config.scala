package a4.config

import java.util.Locale

import a4.config.Config.{User, fields}
import a4.config.ConfigParser.{NonEmptySection, Section}
import a4.reflection.Reflection

import scala.util.Try

final case class Config(user: Option[User] = None) {
  override def toString: String = fields(Nil, this).mkString("\n")
  def writeConfig: String =
    toMap.foldLeft("") {
      case (acc, (section, values)) =>
        acc + s"$section\n" + values.map {
          case (k, v) => s"  $k=$v"
        }.mkString("", "\n", "\n")
    }

  def toMap: Map[Section, Map[String, String]] = {
    def ref(i: Any) =
      Reflection
        .fieldsOf(i)
        .flatMap({
          case (k, Some(v)) => Some(k, v.toString)
          case (_, None) => None
          case (k, v) => Some(k, v.toString)
        })
        .toMap
    Reflection.fieldsOf(this).foldLeft(Map.empty[Section, Map[String, String]]){
      case (acc, (name, Some(value))) =>
        acc + (NonEmptySection(name) -> ref(value))
      case (acc, (name, _)) =>
        acc + (NonEmptySection(name) -> Map.empty)
    }
  }
}

object Config {

  def apply(map: Map[Section, Map[String, String]]): Config = {
    val user = map.get(NonEmptySection("user"))
    Config(user.map(
      x => User(
        x.get("name"),
        x.get("street"),
        x.get("zip"),
        x.get("city"),
        x.get("lang").flatMap(x => Try { Locale.forLanguageTag(x) }.toOption)
      )
    ))
  }

  def fields(prefix: List[String], instance: Any): List[String] = {
    def str(name: String, value: String) = List(s"""${(prefix ++ List(name)).mkString(".")}=$value""")
    val result: List[String] = Reflection.fieldsOf(instance).flatMap {
      case (_, None) => Nil
      case (name, Some(value: Locale)) => str(name, value.toString)
      case (name, Some(value: String)) => List(s"""${(prefix ++ List(name)).mkString(".")}=$value""")
      case (name, Some(value)) => fields(prefix ++ List(name), value)
      case (name, value) => fields(prefix ++ List(name), value)
    }
    result
  }

  final case class User(
    name: Option[String],
    street: Option[String],
    zip: Option[String],
    city: Option[String],
    lang: Option[Locale]
  )
}
