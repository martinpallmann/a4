package a4

import java.io.File
import java.time.LocalDate
import java.util.Locale

import a4.Args.Node
import a4.Errors.FileExists
import a4.config.{Config, ConfigParser}
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import scopt.OptionDef

object Main {

  def main(args: Array[String]): Unit = {
    if (args.headOption.contains("--_completion")) {
      def removeName(a: Array[String]): Array[String] = {
        if (a.headOption.map(_.trim).contains("a4")) a.drop(1)
        else a
      }
      val restArgs = args.drop(1)
      if (restArgs.length == 1) {
        complete(removeName(restArgs.head.split(' '))).foreach {
          case (s, Some(d)) => println(s"$s\t$d")
          case (s, _) => println(s)
        }
      }
    } else if (args.headOption.contains("colors")) {
      testColors()
    } else {
      Args(args).foreach(_.command.run())
    }
  }

  def testColors(): Unit = {
    val term = new DefaultTerminalFactory().createTerminal
    val screen = new TerminalScreen(term)
    screen.getTerminal.setForegroundColor(TextColor.ANSI.WHITE)
    for (i <- 0 to 255) {
      val s = f"$i%3d"
      if (i == 1) screen.getTerminal.setForegroundColor(TextColor.ANSI.BLACK)
      if (i == 16) screen.getTerminal.setForegroundColor(TextColor.ANSI.WHITE)
      if (i == 26) screen.getTerminal.setForegroundColor(TextColor.ANSI.BLACK)
      if (i == 52) screen.getTerminal.setForegroundColor(TextColor.ANSI.WHITE)
      if (i == 61) screen.getTerminal.setForegroundColor(TextColor.ANSI.BLACK)
      if (i == 88) screen.getTerminal.setForegroundColor(TextColor.ANSI.WHITE)
      if (i == 100) screen.getTerminal.setForegroundColor(TextColor.ANSI.BLACK)
      if (i == 232) screen.getTerminal.setForegroundColor(TextColor.ANSI.WHITE)
      if (i == 241) screen.getTerminal.setForegroundColor(TextColor.ANSI.BLACK)
      screen.getTerminal.setBackgroundColor(new TextColor.Indexed(i))
      screen.getTerminal.putCharacter(' ')
      screen.getTerminal.putCharacter(s(0))
      screen.getTerminal.putCharacter(s(1))
      screen.getTerminal.putCharacter(s(2))
      screen.getTerminal.putCharacter(' ')
      screen.getTerminal.setBackgroundColor(TextColor.ANSI.BLACK)
      screen.getTerminal.putCharacter(' ')
      if (i % 15 == 0) {
        screen.getTerminal.putCharacter('\n')
        screen.getTerminal.putCharacter('\n')
      }
    }

  }

  def renderMain(out: Option[File], force: Boolean, background: Int): Unit = {
    val config = ConfigParser.parseDefaultConfig.getOrElse(Config())
    val locale = Locale.ENGLISH
    val labels = Resources("letter", locale)
    val term = new DefaultTerminalFactory().createTerminal
    val screen = new TerminalScreen(term)
    val theme = MyTheme()
    val gui = new MultiWindowTextGUI(screen, new TextColor.Indexed(background))
    gui.setTheme(theme)
    screen.startScreen()

    def print(l: Letter): Unit = {
      screen.stopScreen()
      val f = out.getOrElse(new File("letter.pdf"))
      if (!force && f.exists()) {
        FileExists(f)
      }
      Printer.print(l, f)
    }

    def cancel(): Unit = {
      screen.stopScreen()
    }

    val window = new MyWindow(
      labels,
      Letter(
        "Sehr geehrte Damen und Herren,",
        "Mit freundlichen Grüßen",
        "Betreff",
        LocalDate.now(),
        Sender(
          config.user.flatMap(_.name).getOrElse("Erika Mustermann"),
          config.user.flatMap(_.street).getOrElse("Musterstraße 13"),
          config.user.flatMap(_.zip).getOrElse("00000"),
          config.user.flatMap(_.city).getOrElse("Musterstadt"),
        ),
        "", ""
      ),
      print,
      cancel
    )
    gui.addWindow(window)
    window.waitUntilClosed()
  }

  def complete(args: Array[String]): List[(String, Option[String])] = {
    args
      .foldLeft(Args.parser.asTree) {
        case (acc, elem) =>
          acc
            .find(_.name == elem)
            .fold(acc.filter(x => x.name.startsWith(elem))) {
              x => {
                if (x.children.isEmpty) {
                  x.parent.toList.flatMap(p =>
                    p.children
                      .filterNot(_ == x)
                      .filterNot(x => args.contains(x.name))
                  )
                } else {
                  x.children
                }
              }
            }
      }
      .map(x => (x.name, x.description))
  }
}
