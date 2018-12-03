package a4

import java.io.File

object Errors {
  sealed abstract class Error(msg: String, errorCode: Int) {
    System.err.println(s"error: $msg")
    System.exit(errorCode)
  }
  case class FileExists(f: File) extends Error(s"file exists: $f", 1)
}
