<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2007 XBRL International. All Rights Reserved. -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:cfcn="http://xbrl.org/2008/conformance/function"
  xmlns:conf="http://xbrl.org/2008/conformance"
  xmlns:fcn="http://xbrl.org/2008/function"
  xmlns:reg="http://xbrl.org/2008/registry">
  
  <xsl:output method="xml" encoding="UTF-8"/>

  <xsl:include href="conformance.xsl" />

  <xsl:template match="cfcn:call">
    <li>
      Function Call: <code><xsl:value-of select="."/></code><br/>
      <xsl:if test="@file">
        (against file <xsl:value-of select="@file"/>)
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="cfcn:test">
    <li>
      Result test: <code><xsl:value-of select="."/></code>
    </li>
  </xsl:template>


</xsl:stylesheet>
