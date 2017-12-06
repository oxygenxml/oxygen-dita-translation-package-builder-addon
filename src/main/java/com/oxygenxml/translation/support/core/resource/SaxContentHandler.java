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
  private static final String HREF = "href";
  /**
   * The name of the attribute that represents a content reference.
   */
  private static final String CONREF = "conref";
  /**
   * The name of the format attribute which tells if this is a reference to be collected or not.
   */
  private static final String FORMAT = "format";
  /**
   * Constant value: dita;
   */
  private static final String FORMAT_DITA= "dita";
  /**
   * Constant value: ditamap;
   */
  private static final String FORMAT_DITAMAP= "ditamap";
  /**
   * The name of the "scope" attribute which tells if this is a reference to be collected or not.
   */
  private static final String SCOPE = "scope";
  /**
   * Constant value: external;
   */
  private static final String EXTERNAL= "external";
  /**
   * The detected references.
   */
  private Set<ReferredResource> ditamapHrefs;
  /**
   * Base URL for resolvinf relative references.
   */
  private URL baseUrl;

  /**
   * @return The detected references. <code>null</code> if no references were detected.
   */
  public Set<ReferredResource> getDitaMapHrefs() {
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
      ditamapHrefs = new HashSet<ReferredResource>();
    }
  }

  @Override
  public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
    boolean collect = true;
    for (int att = 0; att < attributes.getLength(); att++) {
      String name = attributes.getQName(att);
      // Exclude all referred resources with scope external.
      if(SCOPE.equals(name) && 
         EXTERNAL.equals(attributes.getValue(name))) {
        collect = false;
        break;
      }
      
      // and all resources with format non dita.
      if (collect) {
        if (FORMAT.equals(name)) {
          String value = attributes.getValue(name);
          boolean ditaFormat = FORMAT_DITA.equals(value) || 
              FORMAT_DITAMAP.equals(value) || "".equals(value);
          if (!ditaFormat) {
            collect = false;
            break;
          }
        }
      }
    }
    
    if (collect) {
      for (int att = 0; att < attributes.getLength(); att++) {
        String attributeName = attributes.getQName(att);
        boolean isConref = CONREF.equals(attributeName);
        boolean isHref = HREF.equals(attributeName);
        if (isHref || isConref) {
          boolean isBinary = (isHref && "image".equals(localName));
          ditamapHrefs.add(new ReferredResource(makeAbsoluteURL(attributes, attributeName), isBinary));
          break;
        }
      } 
    }
  }

  private URL makeAbsoluteURL(Attributes attributes, String attributeName) {
    String href = attributes.getValue(attributeName);
    int indexOf = href.indexOf("#");
    // An anchor exist..somewhere in the relative path.
    if(indexOf != -1){
      // The anchor should be dropped here....
      href = href.substring(0, indexOf);
    }

    URL absoluteHref = URLUtil.resolveRelativeSystemIDs(baseUrl, href);
    return absoluteHref;
  }
}
