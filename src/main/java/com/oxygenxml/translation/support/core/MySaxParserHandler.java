package com.oxygenxml.translation.support.core;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MySaxParserHandler extends DefaultHandler {
  private ArrayList<File> ditamapHrefs;
  private File baseUrl;
  
  public ArrayList<File> getDitaMapHrefs() {
    return ditamapHrefs;
  }

  private static final String HREF_ATTRIBUTE_NAME = "href";
  
  public MySaxParserHandler(String url){
    this.ditamapHrefs = new ArrayList<File>();
    this.baseUrl = new File(url).getParentFile();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    for (int att = 0; att < attributes.getLength(); att++) {
      String attName = attributes.getQName(att);
      /**
       *  TODO 
       *  The href is relative
       *    1. Remove the anchor part (a.dita#id_topic/element_id)
       *    2. Make it absolute. Use the URL of the parsed file as base. 
       */
      if(attName.equals(HREF_ATTRIBUTE_NAME)){
        String value = attributes.getValue(attName);
        int indexOf = value.indexOf("#");
        if(value.contains("#")){
          File absolutePath = new File(baseUrl, value.substring(0, indexOf));
          String normalizedFile = FilenameUtils.normalize(absolutePath.getAbsolutePath());
          ditamapHrefs.add(new File(normalizedFile));
        } else {
          File absolutePath = new File(baseUrl, value);
          String normalizedFile = FilenameUtils.normalize(absolutePath.getAbsolutePath());
          ditamapHrefs.add(new File(normalizedFile));
        }
      }
    } 
  }
}
