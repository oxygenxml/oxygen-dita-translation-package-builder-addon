package com.oxygenxml.translation.support.util;

import java.io.File;
import java.util.ArrayList;

public class CustomResourceIteration implements ResourceIteration {

  public ArrayList<File> listResources(ParserCreator parser, File ditaMap) {
    ArrayList<File> currentDirContent = new ArrayList<File>();
    File parentFile = ditaMap.getParentFile();
    File[] everythingInThisDir = parentFile.listFiles();
    for (File file : everythingInThisDir) {
      currentDirContent.add(file);
    }
    return currentDirContent;
  }

}
