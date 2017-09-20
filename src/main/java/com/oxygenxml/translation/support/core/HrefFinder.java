package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.oxygenxml.translation.support.util.CustomParserCreator;
import com.oxygenxml.translation.support.util.ParserCreator;

public class HrefFinder {
  
  public ArrayList<File> gatherHrefAttributes(ParserCreator parserCreator, String url)
    throws ParserConfigurationException, SAXException, IOException {

    InputSource is = new InputSource(url);

    XMLReader xmlReader = parserCreator.createXMLReader();

    MySaxParserHandler userhandler = new MySaxParserHandler(url);
    xmlReader.setContentHandler(userhandler);
    xmlReader.parse(is);

    return userhandler.getDitaMapHrefs();

  }
//  public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
//    ArrayList<File> list = new HrefFinder().gatherHrefAttributes(new CustomParserCreator(), "C:\\Users\\intern1\\Documents\\userguide-hotfixes-19.0\\userguide-hotfixes-19.0\\DITA\\UserManual.ditamap");
//    for (File file : list) {
//      System.out.println(file.getAbsolutePath());
//    }
//  }
  
}
