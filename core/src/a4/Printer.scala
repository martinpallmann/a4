package a4

import java.io.{BufferedOutputStream, File, FileOutputStream}

import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import org.apache.fop.apps.FopFactory
import org.apache.xmlgraphics.util.MimeConstants

object Printer {

  def print(letter: Letter, f: File): Unit = {
    val out = new BufferedOutputStream(new FileOutputStream(f))
    try {
      val conf = getClass.getClassLoader.getResource("fop.xconf").toURI
      val xsl = getClass.getClassLoader.getResourceAsStream("letter.xsl")
      val fopFactory = FopFactory.newInstance(conf)
      val fop = fopFactory.newFop(MimeConstants.MIME_PDF, out)
      val factory = TransformerFactory.newInstance()
      val xslt = new StreamSource(xsl)
      val transformer = factory.newTransformer(xslt)
      val src = new StreamSource(letter.toReader)
      val res = new SAXResult(fop.getDefaultHandler)
      transformer.transform(src, res)
    } finally {
      out.close()
    }
  }
}
