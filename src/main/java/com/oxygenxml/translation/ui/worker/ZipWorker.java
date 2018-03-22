package com.oxygenxml.translation.ui.worker;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.support.util.PathUtil;
import com.oxygenxml.translation.support.util.ResultsManagerUtil;
import com.oxygenxml.translation.ui.NoChangedFilesException;
import com.oxygenxml.translation.ui.PackResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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
  private PackResult modifiedFilesNumber;
  
  /**
   * True if the user wants to pack the entire directory.
   */
  private boolean packAll;
  
  /**
   * The list containing all the modified files.
   */
  private List<ResourceInfo> modifiedResources = new ArrayList<ResourceInfo>();
  
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
      File rootDir = MilestoneUtil.getFile(rootMap).getParentFile();
      modifiedFilesNumber = packageBuilder.generateChangedFilesPackage(
          rootDir,
          zipDestinationDir, 
          modifiedResources, 
          topLocationURL);

      List<String> filesNotCopied = packageBuilder.getFilesNotCopied();
      if (!filesNotCopied.isEmpty()) {
        // Avoid errors duplication.
        ResultsManagerUtil.clearResultsPanel();
        for (String relPath : filesNotCopied) {
          String systemId = new URL(rootMap, relPath).toExternalForm();
          ResultsManagerUtil.showInResultsPanel(DocumentPositionedInfo.SEVERITY_INFO, 
              "Unable to copy: " + relPath, 
              systemId, 
              ResultType.GENERIC);
        }
      }
    }
    return null;
  }
  
  /**
   * @return The number of modified files.
   */
  public PackResult getModifiedFilesNumber() {
    return modifiedFilesNumber;
  }
}
