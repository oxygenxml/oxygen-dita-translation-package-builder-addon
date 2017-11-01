<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="xs">
	
	<xsl:param name="mapTitle"/>
	
	<xsl:output method="xml" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" />
	
	<xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<title>Changed resources from <xsl:value-of select="$mapTitle"/></title>
			</head>
			<body>
				<h1>Modified files relative paths to <xsl:value-of select="$mapTitle"/></h1>
				<ol type="1">
					<xsl:for-each select="resources/info-resource">
						<xsl:sort  data-type="text" order="ascending" select="relativePath"/>
						<li>
							<xsl:value-of select="relativePath" />		
						</li>
					</xsl:for-each>
				</ol>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet> 