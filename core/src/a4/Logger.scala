package a4

import org.apache.commons.logging.Log

import scala.io.AnsiColor._

class Logger(name: String) extends Log with Serializable {

  private def print(marker: String, message: Any, t: Throwable): Unit = {
    System.err.println(s"$marker $message")
    Option(t).foreach(e => e.printStackTrace(System.err))
  }

  val ERROR = s"$RED${BOLD}ERROR$RESET"
  val FATAL = s"$WHITE$RED_B${BOLD}FATAL$RESET"

  def trace(message: Any): Unit = ()
  def trace(message: Any, t: Throwable): Unit = ()
  def debug(message: Any): Unit = ()
  def debug(message: Any, t: Throwable): Unit = ()
  def info(message: Any): Unit = ()
  def info(message: Any, t: Throwable): Unit = ()
  def warn(message: Any): Unit = warn(message, null)
  def warn(message: Any, t: Throwable): Unit = ()
  def error(message: Any): Unit = error(message, null)
  def error(message: Any, t: Throwable): Unit = print(ERROR, message, t)
  def fatal(message: Any): Unit = fatal(message, null)
  def fatal(message: Any, t: Throwable): Unit = print(FATAL, message, t)
  def isTraceEnabled: Boolean = false
  def isDebugEnabled: Boolean = false
  def isInfoEnabled: Boolean = false
  def isWarnEnabled: Boolean = true
  def isErrorEnabled: Boolean = true
  def isFatalEnabled: Boolean = true
}
