package com.oxygenxml.translation.support.util;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * An implementation that uses the SAX API ({@link SAXParserFactory}) to create a SAX Parser.
 */
public class SAXParserCreator implements ParserCreator {

  /**
   * @see com.oxygenxml.translation.support.util.ParserCreator#createXMLReader()
   */
  public XMLReader createXMLReader()
      throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException, SAXException {
    
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    SAXParser saxParser = factory.newSAXParser();
    
    return saxParser.getXMLReader();
  }
}
