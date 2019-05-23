package a4

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean

import a4.letter.Resources
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Interactable.FocusChangeDirection
import com.googlecode.lanterna.gui2.TextBox.DefaultTextBoxRenderer
import com.googlecode.lanterna.gui2.Window.Hint._
import com.googlecode.lanterna.gui2.{Borders, GridLayout, _}
import com.googlecode.lanterna.input.KeyStroke

import scala.collection.JavaConverters._

class MyWindow(labels: Resources, default: Letter, print: Letter => Unit, cancel: () => Unit)
  extends BasicWindow() {

  setHints(List(CENTERED).asJava)

  addWindowListener(new WindowListenerAdapter {
    override def onInput(w: Window, k: KeyStroke, deliverEvent: AtomicBoolean): Unit = {
      if (k.isCtrlDown) {
        if (k.getCharacter == 'p') {
          MyWindow.this.close()
          print(getLetter)
        }
      }
    }
  })

  def textBox(span: Int, text: String, cols: Int, rows: Int): TextBox = {
    val result = new TextBox(new TerminalSize(cols, rows), text) {
      override def afterEnterFocus(d: FocusChangeDirection, p: Interactable): Unit = {
        if (getText == text) {
          setText("")
        }
      }

      override def afterLeaveFocus(d: FocusChangeDirection, n: Interactable): Unit = {
        if (getText.isBlank) {
          setText(text)
        }
      }
    }
    result.getRenderer.asInstanceOf[DefaultTextBoxRenderer].setUnusedSpaceCharacter(' ')
    result.setLayoutData(GridLayout.createLayoutData(
      GridLayout.Alignment.BEGINNING,
      GridLayout.Alignment.BEGINNING,
      true,
      false,
      span,
      1
    ))
    result
  }

  val recipient: TextBox = textBox(1, default.recipient, 40, 9)
  val senderName: TextBox = textBox(1, default.sender.name, 20, 1)
  val senderStreet: TextBox = textBox(1, default.sender.street, 20, 1)
  val senderZip: TextBox = textBox(1, default.sender.zip, 6, 1)
  val senderCity: TextBox = textBox(1, default.sender.city, 14, 1)
  val subject: TextBox = textBox(1, default.subject, 80, 1)
  val salutation: TextBox = textBox(1, default.salutation, 80, 1)
  val text: TextBox = textBox(1, default.text, 80, 20)
  val greeting: TextBox = textBox(1, default.greeting, 80, 1)

  def getLetter: Letter = Letter(
    salutation.getText,
    greeting.getText,
    subject.getText,
    LocalDate.now(),
    Sender(
      senderName.getText,
      senderStreet.getText,
      senderZip.getText,
      senderCity.getText
    ),
    recipient.getText,
    text.getText
  )

  val cancelButton: Component = new Button(labels.cancel, () => {
    MyWindow.this.close()
    cancel()
  }).setLayoutData(GridLayout.createLayoutData(
    GridLayout.Alignment.END,
    GridLayout.Alignment.END,
    true, false
  ))

  val printButton: Component = new Button(labels.print, () => {
    MyWindow.this.close()
    print(getLetter)
  }).setLayoutData(GridLayout.createLayoutData(
    GridLayout.Alignment.END,
    GridLayout.Alignment.END,
    false, false
  ))

  val buttonPanel: Panel = {
    val layout = new GridLayout(2)
    layout.setRightMarginSize(0)
    new Panel(layout) {
      this.addComponent(cancelButton)
      this.addComponent(printButton)
      this.setPreferredSize(new TerminalSize(80, 5))
    }
  }

  def emptySpace: Panel = Panels.horizontal(new EmptySpace())

  val senderZipCity: Panel = {
    val p = Panels.horizontal(senderZip, senderCity)
    p.getLayoutManager.asInstanceOf[LinearLayout].setSpacing(0)
    p
  }



  val sender: Border = {
    val labelName = labels.name
    val labelStreet = labels.street
    val labelZipCity = s"${labels.zip}/${labels.city}"
    val fmt = "%-" + (List(labelName, labelStreet, labelZipCity).foldLeft(0) {
      case (acc, elem) => Math.max(acc, elem.length)
    } + 1) + "s"
    Panels.grid(2,
      new Label(String.format(fmt, labelName)), senderName,
      new Label(String.format(fmt, labelStreet)), senderStreet,
      new Label(String.format(fmt, labelZipCity)), senderZipCity
    ).withBorder(Borders.singleLine(labels.sender))
  }

  val recipientPanel: Border =
    Panels
      .horizontal(new Label(1.to(9).mkString("\n")), recipient)
      .withBorder(Borders.singleLine(labels.recipient))

  val recipientAndSender: Panel = {
    val p = Panels.horizontal(recipientPanel, sender)
    p
  }

  def shiftRight(c: Component): Panel = {
    val p = Panels.horizontal(new EmptySpace(), c)
    p.getLayoutManager.asInstanceOf[LinearLayout].setSpacing(0)
    p
  }

  val p: Panel = Panels.vertical(
    recipientAndSender,
    emptySpace,
    shiftRight(subject),
    shiftRight(salutation),
    emptySpace,
    text.withBorder(Borders.singleLine(labels.text)),
    emptySpace,
    shiftRight(greeting),
    emptySpace,
    shiftRight(buttonPanel)
  )
  setComponent(p)
}
