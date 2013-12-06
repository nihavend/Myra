<?xml version="1.0" encoding="ISO-8859-9"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
  <h2>Dosya Detayı</h2>
    <table border="1">
      <tr bgcolor="#9acd32">
        <th>Comment</th>
      </tr>
      <xsl:for-each select="properties">
      <tr>
        <td><xsl:value-of select="comment"/></td>
      </tr>
      </xsl:for-each>
      <xsl:for-each select="properties/entry">
      <tr bgcolor="#9acd32">
        <th><xsl:value-of select="@key"/></th>
      </tr>
      <tr>
		<td><xsl:value-of select="text()"/></td>
      </tr>
      </xsl:for-each>
      <!--xsl:for-each select="properties">
      <xsl:for-each select="entry">
      <tr bgcolor="#9acd32">
        <th><xsl:value-of select="@key"/></th>
      </tr>
      <tr>
		<td><xsl:value-of select="text()"/></td>
      </tr>
      </xsl:for-each>
      </xsl:for-each-->
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>