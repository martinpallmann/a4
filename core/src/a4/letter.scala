package a4

import java.io.{Reader, StringReader}
import java.time.LocalDate
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale

import a4.reflection.Reflection

object Letter {
  def toString(o: Any): String = o match {
    case x: LocalDate =>
      DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.GERMAN)
        .format(x)
    case x =>
      x.toString
  }
}

case class Letter(
                 salutation: String,
                 greeting: String,
                 subject: String,
                 date: LocalDate,
                 sender: Sender,
                 recipient: String,
                 text: String
                 ) {
  override def toString: String = {
    val res = Reflection.fieldsOf(this).foldLeft(""){
      case (acc, (k, v)) => acc + s"<$k>${Letter.toString(v)}</$k>"
    }
    s"<letter>$res</letter>"
  }

  def toReader: Reader = new StringReader(toString)
}

case class Sender(
  name: String,
  street: String,
  zip: String,
  city: String
) {
  override def toString: String =
    Reflection.fieldsOf(this).foldLeft(""){
      case (acc, (k, v)) => acc + s"<$k>$v</$k>"
    }
}
