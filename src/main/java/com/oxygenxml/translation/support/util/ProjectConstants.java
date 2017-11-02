/**
 * 
 */
package com.oxygenxml.translation.support.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * All constants from project should be defined here. 
 * 
 * @author adrian_sorop
 */
public class ProjectConstants {
  
  /**
   * DITA file extension: .dita.
   */
  public static final String DITA_EXTENSION = ".dita";
  
  /**
   * DITA Map file extension: .ditamap.
   */
  public static final String DITA_MAP_EXTENSION = ".ditamap";
  
  /**
   * Predefined name suffix of the file that stores the relative path for modified files.
   */
  public static final String REPORT_FILE_SUFFIX = "_translation_report.xhtml";
  
  /**
   * Predefined name suffix of the modified resources archive.
   */
  public static final String ZIP_FILE_SUFFIX = "_translation_package.zip";
  
  /**
   * ZIP file extension.
   */
  public static final String ZIP_FILE_EXTENSION = ".zip";
  
  /**
   * @return The name of the zip with the files to be translated.
   */
  public static String getZipFileName(File ditaRootMap) {
    return FilenameUtils.removeExtension(ditaRootMap.getName()) + ZIP_FILE_SUFFIX;
  }
  
  /**
   * @return The name of the HTML report of files contained in the ZIP.
   */
  public static String getHTMLReportFile(File ditaRootMap) {
    return FilenameUtils.removeExtension(ditaRootMap.getName()) + REPORT_FILE_SUFFIX;
  }
}
