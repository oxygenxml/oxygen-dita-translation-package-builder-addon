package com.oxygenxml.translation.support.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.oxygenxml.translation.support.core.HrefFinder;
import com.oxygenxml.translation.support.core.models.ResourceInfo;

public class SaxResourceIteration implements IResource {
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(SaxResourceIteration.class);
  private ArrayList<File> oldParsedFiles = new ArrayList<File>();

  public ArrayList<File> listResources(ParserCreator parser, File dirPath) {
    try {
      if(!oldParsedFiles.contains(dirPath)){
        oldParsedFiles.add(dirPath);
//        logger.debug("Added to oldParsedFiles list : " + dirPath.getAbsolutePath());
        if(dirPath.getName().endsWith(".dita") || dirPath.getName().endsWith(".ditamap")){
//          logger.debug("Going to parse : " + dirPath);
          ArrayList<File> currentHrefs = new HrefFinder().gatherHrefAttributes(parser, dirPath.getAbsolutePath());
          for (File file : currentHrefs) {
//            logger.debug(file.getAbsolutePath());
            listResources(parser, file);
          }
        }
      }
    } catch (ParserConfigurationException e) {
      logger.error(e, e);
    } catch (SAXException e) {
      logger.error(e, e);
    } catch (IOException e) {
      logger.error(e, e);
    }
    return oldParsedFiles;
  }

  public Iterator<IResource> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

  public ResourceInfo getResourceInfo() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
