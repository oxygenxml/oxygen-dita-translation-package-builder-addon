package com.oxygenxml.translation.progress.worker;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import com.oxygenxml.translation.progress.NoChangedFilesException;
import com.oxygenxml.translation.progress.PackResult;
import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.progress.StoppedByUserException;
import com.oxygenxml.translation.support.core.PackageBuilder;
import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;

/**
 * Creates an AbstractWorker for packing a directory.
 * 
 * @author Bivolan Dalina
 *
 */
public class ZipWorker extends AbstractWorker {
  /**
   *  The file we want to zip.
   */
  private File rootDir;
  /**
   *  Where to put the created package.
   */
  private File zipDir;
  /**
   * An object that holds the number of modified files.
   */
  private PackResult modifiedFilesNumber;
  public PackResult getModifiedFilesNumber() {
    return modifiedFilesNumber;
  }
  /**
   * True if the user wants to pack the entire directory.
   */
  private boolean packAll;
  /**
   * The list containing all the modified files.
   */
  private ArrayList<ResourceInfo> modifiedResources = new ArrayList<ResourceInfo>();
  
  public ZipWorker(File rootDir, File chosenDir,  ArrayList<ProgressChangeListener> listeners, boolean packAll, ArrayList<ResourceInfo> modifiedResources) {
    super(listeners);
    this.rootDir = rootDir;
    this.packAll = packAll;
    this.zipDir = chosenDir;
    this.modifiedResources = modifiedResources;
  }
  
  public ZipWorker(File rootDir, File chosenDir, ArrayList<ProgressChangeListener> listeners, boolean packAll) {
    super(listeners);
    this.rootDir = rootDir;
    this.zipDir = chosenDir;
    this.packAll = packAll;
  }

  /**
   * Main task. Executed in background thread.
   * 
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws NoSuchAlgorithmException  The MD5 algorithm is not available.
   * @throws StoppedByUserException  The user pressed the Cancel button.
   * @throws  IOException Problems reading the file.
   * @throws NoChangedFilesException No file was changed since the last generation of a milestone file.
   */
  @Override
  public Void doInBackground() throws IOException, StoppedByUserException, NoSuchAlgorithmException, JAXBException, NoChangedFilesException {
    if(packAll){
      ArchiveBuilder archiveBuilder = new ArchiveBuilder();
      for (ProgressChangeListener l : listeners) {
        archiveBuilder.addListener(l);
      }

      archiveBuilder.zipDirectory(rootDir, zipDir, false);
    }
    else{
      PackageBuilder packageBuilder = new PackageBuilder();
      for (ProgressChangeListener l : listeners) {
        packageBuilder.addListener(l);
      }
      
      modifiedFilesNumber = packageBuilder.generateChangedFilesPackage(rootDir,zipDir, modifiedResources, false);
    }
    return null;
  }
}
