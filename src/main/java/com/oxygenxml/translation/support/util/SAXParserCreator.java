package com.oxygenxml.translation.support.util;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

/**
 * An implementation that uses the SAX API ({@link SAXParserFactory}) to create a SAX Parser.
 */
public class SAXParserCreator implements ParserCreator {

  /**
   * @see com.oxygenxml.translation.support.util.ParserCreator#createXMLReader()
   */
  public XMLReader createXMLReader(){
    return ro.sync.xml.parser.ParserCreator.newXRNoValid();
  }
}
