package a4.config

import a4.config.ConfigParser.NonEmptySection

object Main {

  def list(): Unit =
    ConfigParser.parseDefaultConfig.foreach(println)

  def add(key: String, value: String): Unit = {
    val div = key.indexOf('.')
    if (div < 0) {
      System.err.println(s"error: key does not contain a section: $key")
    } else {
      val section = NonEmptySection(key.substring(0, div))
      val keyName = key.substring(div + 1)
      if (keyName.contains(".")) {
        System.err.println(s"error: key contains too many sections: $key")
      } else {
        val map = ConfigParser.parseDefaultConfigMap
        val map2 = map(section) + (keyName -> value)
        val res = map + (section -> map2)
        ConfigParser.writeConfig(Config(res))
      }
    }

  }
}
