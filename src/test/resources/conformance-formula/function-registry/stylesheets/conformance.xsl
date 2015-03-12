<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2007 XBRL International. All Rights Reserved. -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:conf="http://xbrl.org/2008/conformance"
  xmlns:fcn="http://xbrl.org/2008/function"
  xmlns:reg="http://xbrl.org/2008/registry">
  
  <xsl:output method="xml" encoding="UTF-8"/>

  <xsl:key name="OWNERS" match="reg:owner" use="@id"/>

  <xsl:template match="conf:testcase">

    <html>
      <head>
        <title>
          <xsl:apply-templates select="conf:number" />:
          <xsl:apply-templates select="conf:name" />
        </title>
      </head>

      <body>
        <h1>
          <xsl:apply-templates select="conf:number" />:
          <xsl:apply-templates select="conf:name" />
        </h1>

        <xsl:apply-templates select="conf:documentation"/>

        <h2>Owners</h2>
        <table border="solid">
          <thead>
            <tr>
              <th>
                Name
              </th>
              <th>
                Affiliation
              </th>
              <th>
                Email
              </th>
              <th>
                Start
              </th>
              <th>
                End
              </th>
            </tr>
          </thead>
          <tbody>
            <xsl:apply-templates select="conf:owners/reg:owner" />
          </tbody>
        </table>

        <xsl:if test="conf:reference">
          <h2>References</h2>
          <xsl:apply-templates select="conf:reference"/>
        </xsl:if>

        <h2>Test Case Variations</h2>
        <xsl:apply-templates select="conf:variation" />

        <h2>Revisions</h2>
        <table border="solid">
          <thead>
            <tr>
              <th>
                Name
              </th>
              <th>
                On
              </th>
              <th>
                Details
              </th>
            </tr>
          </thead>
          <tbody>
            <xsl:apply-templates select="conf:revisions/reg:revision" />
          </tbody>
        </table>
        

      </body>
    </html>
  </xsl:template>

  <xsl:template match="conf:variation">

    <hr/>

    <h2>
      <xsl:if test="conf:number">
        <xsl:apply-templates select="conf:number" />:
      </xsl:if>
      <xsl:if test="@id">
        <xsl:apply-templates select="@id" />
      </xsl:if>
      <xsl:apply-templates select="conf:name" />
    </h2>
  
    <xsl:apply-templates select="conf:documentation"/>
  
    <xsl:if test="conf:owners">
      <h3>Owners</h3>
      <table border="solid">
        <thead>
          <tr>
            <th>
              Name
            </th>
            <th>
              Affiliation
            </th>
            <th>
              Email
            </th>
            <th>
              Start
            </th>
            <th>
              End
            </th>
          </tr>
        </thead>
        <tbody>
          <xsl:apply-templates select="conf:owners/reg:owner" />
        </tbody>
      </table>
    </xsl:if>

    <h3>Inputs</h3>
    <ul>
      <xsl:apply-templates select="conf:inputs"/>
    </ul>
    
    <h3>Outputs</h3>
    <ul>
      <xsl:apply-templates select="conf:outputs"/>
    </ul>

  </xsl:template>

  <xsl:template match="conf:reference">
    <p>
      <xsl:choose>
        <xsl:when test="count(text())">
          <a href="{@xlink:href}"><xsl:value-of select="text()"/></a>
        </xsl:when>
        <xsl:otherwise>
          <a href="{@xlink:href}"><xsl:value-of select="@xlink:href"/></a>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>

  <xsl:template match="reg:owner">
    <tr>
      <td>
        <xsl:apply-templates select="reg:name"/>
      </td>
      <td>
        <xsl:apply-templates select="reg:affiliation"/>
      </td>
      <td>
        <a href="mailto:{reg:email}"><xsl:value-of select="reg:email"/></a>
      </td>
      <td>
        <xsl:apply-templates select="reg:assumedOwnership"/>
      </td>
      <td>
        <xsl:apply-templates select="reg:relinquishedOwnership"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="reg:revision">
    <tr>
      <td>
        <xsl:apply-templates select="key('OWNERS', @by)/reg:name"/>
      </td>
      <td>
        <xsl:apply-templates select="@on"/>
      </td>
      <td>
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
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="reg:assumedOwnership | reg:relinquishedOwnership | conf:lastUpdated">
    <xsl:apply-templates select="@moment"/>
  </xsl:template>
  
  <xsl:template match="@moment | @on">
    <xsl:variable name="date" select="substring-before(.,'T')"/>
    <xsl:variable name="time" select="substring-after(.,'T')"/>
    <xsl:variable name="year" select="substring-before($date,'-')"/>
    <xsl:variable name="rest" select="substring-after($date,'-')"/>
    <xsl:variable name="month" select="substring-before($rest,'-')"/>
    <xsl:variable name="day" select="substring-after($rest,'-')"/>
    <xsl:value-of select="$day"/>-<xsl:value-of select="$month"/>-<xsl:value-of select="$year"/> at <xsl:value-of select="$time"/>
  </xsl:template>

  <xsl:template match="conf:documentation">
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

  <xsl:template match="conf:schema | conf:instance | conf:linkbase">
    <li>
      <xsl:value-of select="local-name(.)"/>: <a href="{./@xlink:href}"><xsl:value-of select="./@xlink:href"/></a>
      <xsl:if test="@readMeFirst">
        (DTS Discovery starting point)
      </xsl:if>
      <xsl:if test="@id">
        (ID=<xsl:value-of select="@id"/>)
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="conf:error">
    <li>
      Error code: <xsl:value-of select="."/>
    </li>
  </xsl:template>

  <xsl:template match="@id">
     <xsl:value-of select="." />
     <xsl:text> </xsl:text>
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
