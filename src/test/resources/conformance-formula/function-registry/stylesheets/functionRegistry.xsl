<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2007 XBRL International. All Rights Reserved. -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:reg="http://xbrl.org/2008/registry"
  xmlns:fcn="http://xbrl.org/2008/function"
  xmlns:conf="http://xbrl.org/2008/conformance"
  >

  <xsl:output method="xml" encoding="UTF-8"/>

  <xsl:include href="functionDefinition.xsl" />

  <xsl:template match="reg:registry">
    <html>
      <head>
        <title>
          <xsl:apply-templates select="reg:name" />
        </title>
      </head>
      <body>
        <h1>
          <xsl:apply-templates select="reg:name" />
        </h1>
        <p>
          Last updated on <xsl:apply-templates select="reg:lastUpdated" />.
        </p>
        
        <xsl:apply-templates select="reg:documentation"/>
        <table border="solid">
          <thead>
            <tr>
              <th>
                Added
              </th>
              <th>
                Status
              </th>
              <th>
                Signature
              </th>
            </tr>
          </thead>
          <tbody>
            <xsl:apply-templates select="reg:entry"/>
          </tbody>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="reg:name">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="reg:lastUpdated | reg:added">
    <xsl:variable name="date" select="substring-before(@moment,'T')"/>
    <xsl:variable name="time" select="substring-after(@moment,'T')"/>
    <xsl:variable name="year" select="substring-before($date,'-')"/>
    <xsl:variable name="rest" select="substring-after($date,'-')"/>
    <xsl:variable name="month" select="substring-before($rest,'-')"/>
    <xsl:variable name="day" select="substring-after($rest,'-')"/>
    <xsl:value-of select="$year"/>-<xsl:value-of select="$month"/>-<xsl:value-of select="$day"/>
    <!--at <xsl:value-of select="$time"/-->
  </xsl:template>

  <xsl:template match="reg:documentation">
    <h2>Documentation</h2>
    <xsl:choose>
      <xsl:when test="reg:url">
        <p>
          <a href="{reg:url/@xlink:href}"><xsl:value-of select="reg:url/@xlink:href"/></a>
        </p>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="reg:entry">
    <tr>
      <td>
        <xsl:apply-templates select="reg:added"/>
      </td>
      <td>
        <xsl:apply-templates select="reg:status"/>
      </td>
      <td>
        <xsl:variable name="function" select="document(reg:url/@xlink:href,/.)/fcn:function"/>
        <xsl:variable name="sig" select="$function/fcn:signature"/>
        <xsl:variable name="conformanceURL" select="$function/fcn:conformanceTest"/>
        <p>
          <a href="{reg:url/@xlink:href}">
            <b><xsl:value-of select="$sig/@name"/></b>(<xsl:apply-templates select="$sig/fcn:input" mode="summary"/>) returns <xsl:value-of select="$sig/fcn:output/@type"/>
          </a>
        </p>
        <!--p>
          <xsl:apply-templates select="$function/fcn:summary"/>          
        </p>
        <p>
          <xsl:apply-templates select="$function/fcn:documentation"/>          
        </p>
        <p>
          <xsl:apply-templates select="$function/fcn:reference"/>          
        </p>
        
        <p>
          <b>Conformance suite documentation:</b>
        </p>
        <p>
          <xsl:apply-templates select="document($conformanceURL/@xlink:href)/conf:testcase/conf:documentation/*"/>
        </p-->
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="/ | *">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="comment() | text() | processing-instruction() | *"/>
    </xsl:copy>
  </xsl:template>
    
  <xsl:template match="@* | text() | comment() | processing-instruction()">
    <xsl:copy/>
  </xsl:template>

</xsl:stylesheet>
