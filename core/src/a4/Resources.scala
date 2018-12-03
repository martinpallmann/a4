package a4

import java.util.{Locale, ResourceBundle}

import scala.util.Try

class Resources(name: String, locale: Locale) {
  private val bundle = Try {
    ResourceBundle.getBundle(name, locale)
  }
  def apply(s: String): String = try {
    bundle.fold(_ => s"could not load resource: $name", _.getString(s))
  } catch {
    case _: Exception => s">>$s<<"
  }
}

object Resources {

  class letter(resources: Resources) {
    def recipient: String = resources("recipient")
    def cancel: String = resources("cancel")
    def print: String = resources("print")
    def sender: String = resources("sender")
    def text: String = resources("text")
    def name: String = resources("name")
    def street: String = resources("street")
    def zip: String = resources("zip")
    def city: String = resources("city")
  }

  def apply(name: String, locale: Locale): letter =
    new letter(new Resources(name, locale))
}
