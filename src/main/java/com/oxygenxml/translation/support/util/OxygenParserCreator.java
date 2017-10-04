package com.oxygenxml.translation.support.util;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import ro.sync.exml.workspace.api.util.XMLReaderWithGrammar;
import ro.sync.exml.workspace.api.util.XMLUtilAccess;

/**
 * A parser creator that delegates uses Oxygen's API to reuse the grammar. 
 */
public class OxygenParserCreator implements ParserCreator {
  /**
   * Utility for creating parsers.
   */
  private XMLUtilAccess access;

  /**
   * Constructor.
   * 
   * @param access Utility for creating parsers.
   */
  public OxygenParserCreator(XMLUtilAccess access) {
    this.access = access;
  }
  /**
   * We reuse the grammar to speed up the parsing.
   */
  private Object grammar;
  
  /**
   * @see com.oxygenxml.translation.support.util.ParserCreator#createXMLReader()
   */
  public XMLReader createXMLReader()
      throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException, SAXException {
    
    XMLReaderWithGrammar bundle = 
        access.newNonValidatingXMLReader(grammar);
    // Keep the grammar.
    grammar = bundle.getGrammarCache();
    
    return bundle.getXmlReader();
  }
}
