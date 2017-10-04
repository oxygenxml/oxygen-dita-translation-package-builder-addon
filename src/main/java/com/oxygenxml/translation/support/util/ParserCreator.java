package com.oxygenxml.translation.support.util;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * An abstraction for obtaining a parser.
 */
public interface ParserCreator {
  
  /**
   * Creates a new parser.
   * 
   * @return An XML reader.
   * 
   * @throws SAXNotRecognizedException
   * @throws SAXNotSupportedException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  public XMLReader createXMLReader()
      throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException, SAXException;

}
