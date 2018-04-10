package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.oxygenxml.translation.exceptions.NoChangedFilesException;
import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.support.util.PathUtil;
import com.oxygenxml.translation.support.util.ResultsManagerUtil;

import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.results.ResultsManager.ResultType;
import ro.sync.exml.workspace.api.util.UtilAccess;

/**
 * Creates an AbstractWorker for packing a directory.
 * @author Bivolan Dalina
 */
public class ZipWorker extends AbstractWorker {
  
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
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
    URL topLocationURL = PathUtil.calculateTopLocationURL(rootMap, packageBuilder);
    // Clear previous reported errors.
    ResultsManagerUtil.clearResultsPanel();
    if(packAll){
      ArchiveBuilder archiveBuilder = new ArchiveBuilder(listeners);
      PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
      if (pluginWorkspace != null) {
        UtilAccess utilAccess = pluginWorkspace.getUtilAccess();
        if (utilAccess != null) {
          File locateFile = utilAccess.locateFile(topLocationURL);
          archiveBuilder.zipDirectory(locateFile, zipDestinationDir);
        }
      }
    } else {
      if (modifiedResources != null) {
        List<URL> collect = 
            modifiedResources.stream().map(t -> resolve(rootMap, t.getRelativePath())).collect(Collectors.toList());
        modifiedFilesNumber = packageBuilder.generateChangedFilesPackage(
            zipDestinationDir, 
            collect, 
            topLocationURL);
      }
      List<URL> filesNotCopied = packageBuilder.getFilesNotCopied();
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
   * Resolves the relative path.
   * 
   * @param baseURL The base URL. 
   * @param relativePath Relative path to resolve.
   *  
   * @return Resolved URL.
   */
  private URL resolve(final URL baseURL, String relativePath) {
    /*
     * #15 - the relative paths can be path/to.file.dita#ID
     * We have to remove the anchors to allow file copy.
     */
    int indexOf = relativePath.indexOf('#');
    if (indexOf != -1){
      relativePath = relativePath.substring(0, indexOf);
    }
    
    try {
      return new URL(baseURL, relativePath);
    } catch (MalformedURLException e) {
      NoSuchElementException noSuchElementException = new NoSuchElementException();
      noSuchElementException.initCause(e);
      throw noSuchElementException;
    }
  }
  
  /**
   * @return The number of modified files.
   */
  public int getModifiedFilesNumber() {
    return modifiedFilesNumber;
  }
}
