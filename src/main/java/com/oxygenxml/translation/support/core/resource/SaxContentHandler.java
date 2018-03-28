package com.oxygenxml.translation.support.core.resource;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ro.sync.util.URLUtil;

/**
 * Detects the "href" attributes and extracts the referenced files. 
 */
public class SaxContentHandler extends DefaultHandler {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(SaxContentHandler.class.getName());
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
  private Set<ReferencedResource> ditamapHrefs;
  /**
   * Base URL for resolvinf relative references.
   */
  private URL baseUrl;

  /**
   * @return The detected references. <code>null</code> if no references were detected.
   */
  public Set<ReferencedResource> getDitaMapHrefs() {
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
      ditamapHrefs = new LinkedHashSet<>();
    }
  }
  
  @Override
  public void startElement(String namespace, String localName, String qName, Attributes attributes) /*NOSONAR*/
      throws SAXException {

    URL location = null;
    boolean isDITA = false;

    if (attributes.getValue(CONREF) != null) {
      location = makeAbsoluteURL(attributes, CONREF);
      isDITA = true;
      if (logger.isDebugEnabled()) {
        logger.debug("CONREF: " + location);
      }
    }

    if (attributes.getValue(HREF) != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("HREF ATTRIBUTE WAS DETECTED");
      }

      if (EXTERNAL.equals(attributes.getValue(SCOPE))) {
        // DO NOTHING
        if (logger.isDebugEnabled()) {
          logger.debug("EXTERNAL SCOPE -- SKIP" );
        }

      } else if (attributes.getValue(FORMAT) != null) {

        String format = attributes.getValue(FORMAT);
        if (FORMAT_DITA.equals(format) || FORMAT_DITAMAP.equals(format) || "".equals(format)) {
          location = makeAbsoluteURL(attributes, HREF);
          isDITA = true;
          if (logger.isDebugEnabled()) {
            logger.debug("REFERNCE WITH DITA FORMAT: " + location);
          }

        }
      } else {
        boolean isImage = false;
        String sourceClass = attributes.getValue("class");
        if (sourceClass != null) {
          isImage = sourceClass.contains(" topic/image ") || sourceClass.contains(" topic/object ");
        }
        location = makeAbsoluteURL(attributes, HREF);
        isDITA = !isImage;

        if (logger.isDebugEnabled()) {
          logger.debug("IS " + location + " IMAGE?? " + isImage);
        }
      }
    }

    if (location != null) {
      ReferencedResource referencedResource = new ReferencedResource(location, isDITA);
      ditamapHrefs.add(referencedResource);
    }

  }
  
  /**
   * Retrieves the absolute URL from a href attribute. All anchors are removed.
   * 
   * @param attributes  The list of attributes of the current element.
   * @param attributeName The attribute which contains a reference.
   * 
   * @return The absolute URL. Anchors are removed.
   */
  private URL makeAbsoluteURL(Attributes attributes, String attributeName) {
    String href = attributes.getValue(attributeName);
    int indexOf = href.indexOf('#');
    // An anchor exist..somewhere in the relative path.
    if(indexOf != -1){
      // The anchor should be dropped here....
      href = href.substring(0, indexOf);
    }

    return URLUtil.resolveRelativeSystemIDs(baseUrl, href);
  }
}
