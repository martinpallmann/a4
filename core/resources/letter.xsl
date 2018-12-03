<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <fo:root>

      <fo:layout-master-set>
        <fo:simple-page-master
            master-name="page"
            margin="0cm">
          <fo:region-body
              margin-right="20mm"
              margin-bottom="30mm"
              margin-left="25mm"
              margin-top="45mm" />
          <fo:region-before
              extent="45mm"
              precedence="true" />
          <fo:region-after
              extent="20mm" />
          <fo:region-start
              extent="25mm" />
          <fo:region-end
              extent="20mm" />
        </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="page">
        <fo:static-content
            flow-name="xsl-region-before"
            text-align="center">
          <fo:block font-size="14pt" margin-top="10mm">
            <xsl:value-of select="letter/sender/name"/>
          </fo:block>
          <fo:block font-size="10pt">
            <xsl:value-of select="letter/sender/street"/>
          </fo:block>
          <fo:block font-size="10pt">
            <xsl:value-of select="letter/sender/zip"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="letter/sender/city"/>
          </fo:block>
        </fo:static-content>
        <fo:static-content
            flow-name="xsl-region-after"
            text-align="center">
          <fo:block font-size="10pt">
            Seite <fo:page-number/> von <fo:page-number-citation ref-id="end"/>
          </fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <fo:block-container
              absolute-position="absolute"
              left="-5mm"
              font-size="7pt"
              width="85mm"
              height="5mm">
            <fo:block white-space-collapse="false">
              <xsl:value-of select="letter/sender/name"/>
              <xsl:text>    </xsl:text>
              <xsl:value-of select="letter/sender/street"/>
              <xsl:text>    </xsl:text>
              <xsl:value-of select="letter/sender/zip"/>
              <xsl:text> </xsl:text>
              <xsl:value-of select="letter/sender/city"/>
            </fo:block>
          </fo:block-container>
          <fo:block-container
              absolute-position="absolute"
              top="5mm"
              font-size="10pt"
              width="75mm"
              height="40mm">
            <fo:block linefeed-treatment="preserve"><xsl:value-of select="letter/recipient"/></fo:block>
          </fo:block-container>
          <fo:block-container
              font-size="10pt"
              absolute-position="absolute"
              left="100mm"
              top="35.46mm"
              width="65mm"
              height="9.54mm"
              text-align="right">
            <fo:block><xsl:value-of select="letter/date"/></fo:block>
          </fo:block-container>
          <fo:block-container
              font-size="10pt"
              margin-top="53.46mm">
            <fo:block font-weight="bold" space-after="10pt"><xsl:value-of select="letter/subject"/></fo:block>
            <fo:block space-after="10pt">
              <xsl:value-of select="letter/salutation"/>
            </fo:block>
            <fo:block space-after="10pt" linefeed-treatment="preserve" line-height="15pt">
              <xsl:value-of select="letter/text" />
            </fo:block>
            <fo:block space-after="50pt">
              <xsl:value-of select="letter/greeting"/>
            </fo:block>
            <fo:block>
              <xsl:value-of select="letter/sender/name"/>
            </fo:block>
            <fo:block id="end"/>
          </fo:block-container>
        </fo:flow>
      </fo:page-sequence>

    </fo:root>
  </xsl:template>
</xsl:stylesheet>
