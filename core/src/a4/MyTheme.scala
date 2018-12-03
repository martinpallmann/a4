package a4

import java.util.Properties

import com.googlecode.lanterna.graphics.{PropertyTheme, Theme}
import com.googlecode.lanterna.gui2.AbstractTextGUI

object MyTheme {
  def apply(): Theme = {
    val properties = new Properties()
    loadProp(properties, "theme.properties")
    new PropertyTheme(properties)
  }

  private def loadProp(p: Properties, name: String): Unit = {
    val classLoader = classOf[AbstractTextGUI].getClassLoader
    val resourceAsStream = classLoader.getResourceAsStream(name)
    p.load(resourceAsStream)
    resourceAsStream.close()
  }
}
