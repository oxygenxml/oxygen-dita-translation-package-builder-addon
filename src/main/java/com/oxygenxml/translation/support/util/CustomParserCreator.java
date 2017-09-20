package com.oxygenxml.translation.support.util;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class CustomParserCreator implements ParserCreator {

  public XMLReader createXMLReader()
      throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException, SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);

    factory.setFeature("http://apache.org/xml/features/xinclude", true);

    SAXParser saxParser = factory.newSAXParser();

    return saxParser.getXMLReader();
  }


}
