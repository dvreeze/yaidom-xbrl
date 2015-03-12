<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2007 XBRL International. All Rights Reserved. -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:fcn="http://xbrl.org/2008/function"
  xmlns:reg="http://xbrl.org/2008/registry">
  
  <xsl:output method="xml" encoding="UTF-8"/>
  
  <xsl:key name="OWNERS" match="reg:owner" use="@id"/>

  <xsl:template match="fcn:function">
    <html>

      <head>
        <title>
          <xsl:apply-templates select="fcn:signature" mode="header" />
        </title>
      </head>

      <body>

        <h1>
          <xsl:apply-templates select="fcn:signature" mode="header" />
        </h1>

        <p>
          Last updated on <xsl:apply-templates select="fcn:lastUpdated" />.
        </p>

        <p>
          <xsl:apply-templates select="fcn:summary"/>
        </p>

        <xsl:if test="fcn:documentation/*">
          <h2>Documentation</h2>
          <xsl:apply-templates select="fcn:documentation"/>
        </xsl:if>

        <xsl:if test="fcn:reference">
          <xsl:apply-templates select="fcn:reference"/>
        </xsl:if>

        <xsl:if test="count(//fcn:signature) &gt; 1">
          <xsl:apply-templates select="fcn:signature" mode="bodyPolymorphic" />
        </xsl:if>
        <xsl:if test="count(//fcn:signature) = 1">
          <xsl:apply-templates select="fcn:signature" mode="body" />
        </xsl:if>

        <xsl:if test="fcn:error">
          <h2>Errors</h2>
          <table border="solid">
            <thead>
              <tr>
                <th>
                  Code
                </th>
                <th>
                  Details
                </th>
              </tr>
            </thead>
            <tbody>
              <xsl:apply-templates select="fcn:error" />
            </tbody>
          </table>
        </xsl:if>

        <xsl:if test="fcn:example">
          <h2>Examples</h2>
          <xsl:apply-templates select="fcn:example" />
        </xsl:if>

        <xsl:if test="fcn:conformanceTest">
          <h2>Conformance suite</h2>
          <xsl:apply-templates select="fcn:conformanceTest"/>
        </xsl:if>


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
            <xsl:apply-templates select="fcn:owners/reg:owner" />
          </tbody>
        </table>

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
            <xsl:apply-templates select="fcn:revisions/reg:revision" />
          </tbody>
        </table>
        
      </body>
    </html>
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
              <a href="{reg:url}"><xsl:value-of select="reg:url"/></a>
            </p>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates />
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="fcn:assumedOwnership | fcn:relinquishedOwnership | fcn:lastUpdated">
    <xsl:apply-templates select="@moment"/>
  </xsl:template>
  
  <xsl:template match="@moment | @on">
    <xsl:variable name="date" select="substring-before(.,'T')"/>
    <xsl:variable name="time" select="substring-after(.,'T')"/>
    <xsl:variable name="year" select="substring-before($date,'-')"/>
    <xsl:variable name="rest" select="substring-after($date,'-')"/>
    <xsl:variable name="month" select="substring-before($rest,'-')"/>
    <xsl:variable name="day" select="substring-after($rest,'-')"/>
    <xsl:value-of select="$year"/>-<xsl:value-of select="$month"/>-<xsl:value-of select="$day"/>
  </xsl:template>

  <xsl:template match="fcn:documentation">
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

  <xsl:template match="fcn:reference">
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


  <xsl:template match="fcn:signature" mode="header" >
    <xsl:value-of select="fcn:output/@type"/>=<em><xsl:value-of select="@name"/></em>(<xsl:apply-templates select="fcn:input" mode="summary"/>)
    <xsl:if test="count(//fcn:signature) &gt; 1"> <br/> </xsl:if>
  </xsl:template>

  <xsl:template match="fcn:input" mode="summary">
    <xsl:choose>
      <xsl:when test="count(following-sibling::fcn:input)=0">
          <xsl:value-of select="@type"/>
      </xsl:when>
      <xsl:otherwise>
          <xsl:value-of select="@type"/>,
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="fcn:signature" mode="bodyPolymorphic" >
    <li>
       <xsl:apply-templates select="." mode="body" />
        <br/><br/>
    </li>
  </xsl:template>

  <xsl:template match="fcn:signature" mode="body" >
        <xsl:if test="fcn:input">
          <h2>Parameters</h2>
          <table border="solid">
            <thead>
              <tr>
                <th>
                  Name
                </th>
                <th>
                  Type
                </th>
                <th>
                  Details
                </th>
              </tr>
            </thead>
            <tbody>
              <xsl:apply-templates select="fcn:input" />
            </tbody>
          </table>
        </xsl:if>

        <xsl:if test="count(fcn:input) = 0 and count(//fcn:signature) &gt; 1">
          <h2>Parameters</h2>
          <p>(No parameters)</p>
        </xsl:if>

          <h2>Output</h2>
          <xsl:apply-templates select="fcn:output" />
  </xsl:template>

  <xsl:template match="fcn:input" >
    <tr>
      <td><xsl:value-of select="@name"/></td>
      <td><xsl:value-of select="@type"/></td>
      <td><xsl:apply-templates select="*"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="fcn:output" >
    <p>
      <b>Type: </b> <xsl:value-of select="@type"/>
    </p>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="fcn:error" >
    <tr>
      <td><xsl:value-of select="@code"/></td>
      <td><xsl:apply-templates select="*"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="fcn:example">
    <p>
      <h3>Title: <xsl:value-of select="@title"/></h3>
    </p>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="fcn:conformanceTest">
    <p>
      <a href="{./@xlink:href}"><xsl:value-of select="./@xlink:href"/></a>
    </p>
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
