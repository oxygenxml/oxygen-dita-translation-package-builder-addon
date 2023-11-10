package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.oxygenxml.translation.exceptions.NoChangedFilesException;
import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.PackageGeneratorUtil;
import com.oxygenxml.translation.support.util.ResultsManagerUtil;

import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.results.ResultsManager.ResultType;

/**
 * Creates an AbstractWorker for packing a directory.
 * @author Bivolan Dalina
 */
public class ZipWorker extends AbstractWorker<Void> {
  
  /**
   *  Where to put the created package.
   */
  private File zipDestinationDir;
  
  /**
   * An object that holds the number of modified files.
   */
  private int modifiedFilesNumber;
  
  /**
   * True if the user wants to pack the entire directory.
   */
  private boolean packAll;
  
  /**
   * The list containing all the modified files.
   */
  private List<ResourceInfo> modifiedResources = new ArrayList<>();
  
  /**
   * Current DITA map.
   */
  private URL rootMap;
  
  /**
   * Generates an archive only with the modified files.
   * 
   * @param rootMap           System ID of map opened in DMM.
   * @param archiveLocation   Where to save the zip file.
   * @param modifiedResources The list containing all the modified files.
   */
  public ZipWorker(URL rootMap, File archiveLocation, List<ResourceInfo> modifiedResources) {
    this.rootMap = rootMap;
    this.zipDestinationDir = archiveLocation;
    this.modifiedResources = modifiedResources;
  }
  
  /**
   * Generates an archive and adds all the files, modified or not.
   * 
   * @param rootMap           System ID of map opened in DMM.
   * @param archiveLocation   Where to save the zip file.
   * @param packAll           <code>true</code> to add all files.
   */
  public ZipWorker(URL rootMap, File archiveLocation, boolean packAll) {
    this.rootMap = rootMap;
    this.zipDestinationDir = archiveLocation;
    this.packAll = packAll;
  }

  /**
   * Main task. Executed in background thread.
   * 
   * @throws NoSuchAlgorithmException  The MD5 algorithm is not available.
   * @throws StoppedByUserException  The user pressed the Cancel button.
   * @throws  IOException Problems reading the file.
   * @throws NoChangedFilesException No file was changed since the last generation of a milestone file.
   */
  @Override
  public Void doInBackground() throws IOException, StoppedByUserException, NoSuchAlgorithmException, NoChangedFilesException {
    // Clear previous reported errors.
    ResultsManagerUtil.clearResultsPanel();
    if(packAll){
      PackageGeneratorUtil.zipEntireRootMapStructure(rootMap, listeners, zipDestinationDir);
    } else {
      List<URL> filesNotCopied = PackageGeneratorUtil.zipModifiedResources(rootMap, listeners, zipDestinationDir, modifiedResources);
      
      modifiedFilesNumber = modifiedResources != null ? modifiedResources.size() : 0;
      if (!filesNotCopied.isEmpty()) {
        // Avoid errors duplication.
        ResultsManagerUtil.clearResultsPanel();
        for (URL relPath : filesNotCopied) {
          ResultsManagerUtil.showInResultsPanel(DocumentPositionedInfo.SEVERITY_INFO, 
              "Unable to copy: " + relPath, 
              relPath.toExternalForm(), 
              ResultType.GENERIC);
        }
      }
    }
    
    return null;
  }

  /**
   * @return The number of modified files.
   */
  public int getModifiedFilesNumber() {
    return modifiedFilesNumber;
  }
}
