package com.oxygenxml.translation.support.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 
 * This class contains 2 methods : 
 * - zipDirectory - this method makes an archive of a directory at the specified location
 * - unzipDirectory - this method extracts the content of an archive at a specified location
 *
 */
public final class ZipFileUtil {

  /**
   * 
   * @param dir  The location of the file/directory we want to zip.
   * @param zipFile  The location of the package.
   * @throws IOException  Problems reading the file.
   */
  public static void zipDirectory(File dir, File zipFile) throws IOException {
    zipFile.getParentFile().mkdirs();

    FileOutputStream fout = null;
    ZipOutputStream zout = null;
    try {
      fout = new FileOutputStream(zipFile);
      zout = new ZipOutputStream(fout);
      zipSubDirectory("", dir, zout);
    } finally{
      zout.close();
    }
  }

  /**
   * 
   * @param basePath  It helps us create the relative path of every file from dir.
   * @param dir  The location of the file/directory we want to zip.
   * @param zout  Where we create the archive.
   * @throws IOException  Problems reading the files.
   */
  private static void zipSubDirectory(String basePath, File dir, ZipOutputStream zout) throws IOException {
    byte[] buffer = new byte[4096];
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          String path = basePath + file.getName() + "/";
          zout.putNextEntry(new ZipEntry(path));
          zipSubDirectory(path, file, zout);
          zout.closeEntry();
        } else {
          FileInputStream fin = null;
          try{
            fin = new FileInputStream(file);
            zout.putNextEntry(new ZipEntry(basePath + file.getName()));
            int length;
            while ((length = fin.read(buffer)) > 0) {
              zout.write(buffer, 0, length);
            }
          }finally{
            zout.closeEntry();
            fin.close();
          }
        }
      }
    }
  }

  /**
   *    
   * @param packageLocation  The location of the package.
   * @param destDir Where to extract the package content.
   * @return A list with the relative path of every extracted file.
   */
  public static ArrayList<String> unzipDirectory(File packageLocation, File destDir){

    //File baseDir = destDir.getParentFile();

    ArrayList<String> nameList = new ArrayList<String>();

    try {
      // Open the zip file
      ZipFile zipFile = new ZipFile(packageLocation);
      Enumeration<?> enu = zipFile.entries();
      try{
        while (enu.hasMoreElements()) {
          ZipEntry zipEntry = (ZipEntry) enu.nextElement();

          String name = zipEntry.getName();

          //We create the directories 
          File file = new File(destDir, name);

          if (name.endsWith("/")) {
            file.mkdirs();
            continue;
          }

          File parent = file.getParentFile();
          if (parent != null) {
            parent.mkdirs();
          }

          //  Extract the file
          InputStream is = null;
          FileOutputStream fos = null;
          try{
            is = zipFile.getInputStream(zipEntry);                	
            fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
              fos.write(bytes, 0, length);
            }
          }finally{
            is.close();
            fos.close();
          }
          
          nameList.add(name);
          
        }
      }finally{
        zipFile.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return nameList;
  }

}


