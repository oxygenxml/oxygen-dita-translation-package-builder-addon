package com.oxygenxml.translation.support.util;

import java.io.File;
import java.net.URL;

import com.oxygenxml.translation.support.TranslationPackageBuilderPlugin;
/**
 * Gets the resources the tests need to run on Travis.
 * 
 * @author Bivolan Dalina
 *
 */
public class PathOption {
  public PathOption() {}
  /**
   * Gets the needed resource.
   * 
   * @param dirName The name of the resource we need.
   * 
   * @return The file/directory specified by dirName.
   */
  public File getPath(String dirName){
    File basedir;
    if(TranslationPackageBuilderPlugin.getInstance() != null){
      URL resource = PathOption.class.getClassLoader().getResource(dirName);
      basedir = new File(resource.getPath());
    } else {
      basedir = new File("src/test/resources", dirName);
    }
    return basedir;
  }

}
