package com.oxygenxml.translation.support.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.oxygenxml.translation.progress.ProgressChangeEvent;
import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.progress.StoppedByUserException;

/**
 * 
 * This class contains 2 methods : 
 * - zipDirectory - this method makes an archive of a directory at the specified location
 * - unzipDirectory - this method extracts the content of an archive at a specified location
 * - copyDirectory - this method copies the content of a source directory into a destination directory.
 */
public final class ArchiveBuilder {
  
  private List<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
  
  public ArchiveBuilder(){
    
  }
  
  public void addListener(ProgressChangeListener listener) {
    this.listeners.add(listener);
  }
  
  /**
   * 
   * @param dir  The location of the file/directory we want to zip.
   * @param zipFile  The location of the package.
   * @throws IOException  Problems reading the file.
   * @throws StoppedByUserException The user pressed the Cancel button.
   */
  public void zipDirectory(File dir, File zipFile) throws IOException, StoppedByUserException {
    zipFile.getParentFile().mkdirs();
    
    FileOutputStream fout = null;
    ZipOutputStream zout = null;
    try {
      fout = new FileOutputStream(zipFile);
      zout = new ZipOutputStream(fout);
      zipSubDirectory("", dir, zout, new int[] {0});
    } finally{
      zout.close();
    }
  }

  /**
   * 
   * @param basePath  It helps us create the relative path of every file from dir.
   * @param dir  The location of the file/directory we want to zip.
   * @param zout  Where we create the archive.
   * @param resourceCounter Counter the number of resources added in the archive.
   * 
   * 
   * @throws IOException  Problems reading the files.
   * @throws StoppedByUserException The user pressed the Cancel button.
   */
  private void zipSubDirectory(String basePath, File dir, ZipOutputStream zout, int[] resourceCounter) throws IOException, StoppedByUserException {
    byte[] buffer = new byte[4096];
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          String path = basePath + file.getName() + "/";
          zout.putNextEntry(new ZipEntry(path));
          
          if(isCanceled()){
            throw new StoppedByUserException("You pressed the Cancel button.");
          }
          
          zipSubDirectory(path, file, zout, resourceCounter);
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
            if(isCanceled()){
              throw new StoppedByUserException("You pressed the Cancel button.");
            }
            resourceCounter[0]++;
            ProgressChangeEvent progress = new ProgressChangeEvent(resourceCounter[0], resourceCounter[0] + " files packed.");
            fireChangeEvent(progress);
            
          } finally{
            zout.closeEntry();
            fin.close();
          }
        }
      }
    }
  }


  private void fireChangeEvent(ProgressChangeEvent progress) {
    for (ProgressChangeListener progressChangeListener : listeners) {
      progressChangeListener.change(progress);
    }
  }

  private boolean isCanceled() {
    boolean result = false;
    for (ProgressChangeListener progressChangeListener : listeners) {
      if (progressChangeListener.isCanceled()) {
        result =  true;
      }
    }
    return result;
  }

  /**
   *    
   * @param packageLocation  The location of the package.
   * @param destDir Where to extract the package content.
   * @return A list with the relative path of every extracted file.
   * @throws StoppedByUserException The user pressed the Cancel button.
   */
  public ArrayList<String> unzipDirectory(File packageLocation, File destDir) throws StoppedByUserException{

    //File baseDir = destDir.getParentFile();

    ArrayList<String> nameList = new ArrayList<String>();
    int counter = 0;
    
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
          
          if(isCanceled()){
            throw new StoppedByUserException("You pressed the Cancel button.");
          }
          
          counter++;
          ProgressChangeEvent progress = new ProgressChangeEvent(counter, counter + " files unpacked.");
          fireChangeEvent(progress);
          
        }
      }finally{
        zipFile.close();
      }
    } catch (IOException e) {
      // TODO Fire a notification ProgressChangeListener.operationFailed(Exception)
      e.printStackTrace();
    }

    return nameList;
  }
  /**
   * 
   * @param sourceLocation The location of the files that are about to be copied.
   * @param targetLocation  Where to copy the files.
   * @param counter  Computes the number of copied files.
   * @throws IOException Problems reading the files.
   * @throws StoppedByUserException  The user pressed the Cancel button.
   */
  public void copyDirectory(File sourceLocation , File targetLocation, int[] counter) throws IOException, StoppedByUserException {
    
    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdir();
      }

      String[] children = sourceLocation.list();
      for (int i=0; i<children.length; i++) {
        copyDirectory(new File(sourceLocation, children[i]),
            new File(targetLocation, children[i]), counter);
        if(isCanceled()){
          throw new StoppedByUserException("You pressed the Cancel button.");
        }
      }
    }
    else {

      InputStream in = null;
      OutputStream out = null;

      // Copy the bits from instream to outstream
      try{
        in = new FileInputStream(sourceLocation);
        out = new FileOutputStream(targetLocation);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        counter[0]++;
      }
      finally{
        in.close();
        out.close();
      }
      if(isCanceled()){
        throw new StoppedByUserException("You pressed the Cancel button.");
      }
      
      ProgressChangeEvent progress = new ProgressChangeEvent(counter[0], counter[0] + " files copied.");
      fireChangeEvent(progress);
      
    }
  }
  
}


