<?xml version="1.0" encoding="UTF-8"?>

<!--
  ~ Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
  ~
  ~ Project and contact information: http://www.concurrentinc.com/
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>

  <!-- Activate Graphics -->
  <xsl:param name="admon.graphics" select="1"/>
  <xsl:param name="admon.graphics.path">images/</xsl:param>
  <xsl:param name="admon.graphics.extension">.gif</xsl:param>
  <xsl:param name="callout.graphics" select="1"/>
  <xsl:param name="callout.graphics.path">images/callouts/</xsl:param>
  <xsl:param name="callout.graphics.extension">.gif</xsl:param>

  <xsl:param name="table.borders.with.css" select="1"/>
  <xsl:param name="html.stylesheet">css/stylesheet.css</xsl:param>
  <xsl:param name="html.stylesheet.type">text/css</xsl:param>
  <xsl:param name="generate.toc">book toc,title</xsl:param>

  <xsl:param name="admonition.title.properties">text-align: left</xsl:param>

  <!-- Label Chapters and Sections (numbering) -->
  <xsl:param name="chapter.autolabel" select="1"/>
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="section.autolabel.max.depth" select="1"/>

  <xsl:param name="section.label.includes.component.label" select="1"/>
  <xsl:param name="table.footnote.number.format" select="'1'"/>

  <!-- Remove "Chapter" from the Chapter titles... -->
  <xsl:param name="local.l10n.xml" select="document('')"/>
  <l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
    <l:l10n language="en">
      <l:context name="title-numbered">
        <l:template name="chapter" text="%n.&#160;%t"/>
        <l:template name="section" text="%n&#160;%t"/>
      </l:context>
    </l:l10n>
  </l:i18n>

  <xsl:template name="user.head.content">
    @ANALYTICS@
  </xsl:template>
  <xsl:template name="user.footer.navigation">
    <p align="center">
      <font size="-2">
        <i>Copyright &#169; 2007-2012 Concurrent, Inc. All Rights Reserved.</i>
      </font>
    </p>
  </xsl:template>
</xsl:stylesheet>
