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
public class SaxContentHandler extends DefaultHandler {
  /**
   * The name of the attribute that represents a reference.
   */
  private static final String HREF_ATTRIBUTE_NAME = "href";
  /**
   * The name of the format attribute which tells if this is a reference to be collected or not.
   */
  private static final String FORMAT_ATTRIBUTE_NAME = "format";
  /**
   * Constant value: dita;
   */
  private static final String FORMAT_DITA_ATTRIBUTE_VALUE= "dita";
  /**
   * Constant value: ditamap;
   */
  private static final String FORMAT_DITAMAP_ATTRIBUTE_VALUE= "ditamap";
  /**
   * The name of the "scope" attribute which tells if this is a reference to be collected or not.
   */
  private static final String SCOPE_ATTRIBUTE_NAME = "scope";
  /**
   * Constant value: external;
   */
  private static final String SCOPE_ATTRIBUTE_VALUE= "external";
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
  public SaxContentHandler(URL baseUrl){
    this.baseUrl = baseUrl;
    
    if (ditamapHrefs == null) {
      ditamapHrefs = new HashSet<URL>();
    }
  }

  @Override
  public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
    
    boolean shouldParse = true;
    for (int att = 0; att < attributes.getLength(); att++) {
      String attribute = attributes.getQName(att);
      // Exclude all referred resources with scope external.
      if(SCOPE_ATTRIBUTE_NAME.equals(attribute) && 
         SCOPE_ATTRIBUTE_VALUE.equals(attributes.getValue(attribute))) {
        shouldParse = false;
        break;
      }
      
      // and all resources with format non dita.
      if (shouldParse) {
        if (FORMAT_ATTRIBUTE_NAME.equals(attribute)) {
          String value = attributes.getValue(attribute);
          boolean ditaFormat = FORMAT_DITA_ATTRIBUTE_VALUE.equals(value) || 
              FORMAT_DITAMAP_ATTRIBUTE_VALUE.equals(value) || "".equals(value);
          if (!ditaFormat) {
            shouldParse = false;
            break;
          }
        }
      }
    }
    
    if (shouldParse) {
      for (int att = 0; att < attributes.getLength(); att++) {
        String hrefAttribute = attributes.getQName(att);
        /**
         *  The href is relative
         *    1. Remove the anchor part (a.dita#id_topic/element_id)
         *    2. Make it absolute. Use the URL of the parsed file as base. 
         */
        if (HREF_ATTRIBUTE_NAME.equals(hrefAttribute)) {
          String href = attributes.getValue(hrefAttribute);
          int indexOf = href.indexOf("#");
          if(indexOf > 0 && indexOf < href.length() - 1){
            href = href.substring(0, indexOf);
          }

          URL absoluteHref = URLUtil.resolveRelativeSystemIDs(baseUrl, href);
          ditamapHrefs.add(absoluteHref);
          break;
        }
      } 
    }
  }
}
