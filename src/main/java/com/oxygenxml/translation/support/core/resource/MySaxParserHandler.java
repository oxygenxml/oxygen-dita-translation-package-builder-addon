package com.oxygenxml.translation.support.core.resource;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ro.sync.util.URLUtil;

/**
 * Detects the "href" attributes and extracts the referenced files. 
 */
class MySaxParserHandler extends DefaultHandler {
  /**
   * The name of the attribute that represents a reference.
   */
  private static final String HREF_ATTRIBUTE_NAME = "href";
  /**
   * The name of the format attribute which tells if this is a reference to be collected or not.
   */
  private static final String FORMAT_ATTRIBUTE_NAME = "format";
  /**
   * The detected references.
   */
  private Set<URL> ditamapHrefs;
  /**
   * Base URL for resolvinf relative references.
   */
  private URL baseUrl;
  
  /**
   * @return The detected references. <code>null</code> if no references were detected.
   */
  public Set<URL> getDitaMapHrefs() {
    return ditamapHrefs;
  }
  
  /**
   * Constructor.
   * 
   * @param baseUrl Base URL to resolve relative references.
   */
  public MySaxParserHandler(URL baseUrl){
    this.baseUrl = baseUrl;
  }

  @Override
  public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
    boolean shouldParse = true;
    for (int att = 0; att < attributes.getLength(); att++) {
      String attName = attributes.getQName(att);
      
      if(attName.equals(FORMAT_ATTRIBUTE_NAME)){
        String value = attributes.getValue(attName);
        if(value.equals("html")){
          shouldParse = false;
        }
        
        break;
      }
    }
    
    if (shouldParse) {
      for (int att = 0; att < attributes.getLength(); att++) {
        String attName = attributes.getQName(att);
        /**
         *  The href is relative
         *    1. Remove the anchor part (a.dita#id_topic/element_id)
         *    2. Make it absolute. Use the URL of the parsed file as base. 
         */
        if (attName.equals(HREF_ATTRIBUTE_NAME)) {
          String href = attributes.getValue(attName);
          int indexOf = href.indexOf("#");
          if(indexOf > 0 && indexOf < href.length() - 1){
            href = href.substring(0, indexOf);
          }
          
          URL absoluteHref = URLUtil.resolveRelativeSystemIDs(baseUrl, href);
          if (ditamapHrefs == null) {
            ditamapHrefs = new HashSet<URL>();
          }
          ditamapHrefs.add(absoluteHref);
          
          break;
        }
      } 
    }
  }
}
