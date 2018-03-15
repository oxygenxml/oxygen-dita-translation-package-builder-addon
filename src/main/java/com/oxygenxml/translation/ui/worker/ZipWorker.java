package com.oxygenxml.translation.ui.worker;

import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.support.util.MessagePresenter;
import com.oxygenxml.translation.support.util.PathUtil;
import com.oxygenxml.translation.ui.NoChangedFilesException;
import com.oxygenxml.translation.ui.PackResult;
import com.oxygenxml.translation.ui.StoppedByUserException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.results.ResultsManager.ResultType;

/**
 * Creates an AbstractWorker for packing a directory.
 * @author Bivolan Dalina
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
   * XXX
   * @param rootMap
   * @param chosenDir
   * @param packAll
   * @param modifiedResources
   */
  public ZipWorker(URL rootMap, File chosenDir, boolean packAll, List<ResourceInfo> modifiedResources) {
    this.rootMap = rootMap;
    this.rootDir = MilestoneUtil.getFile(rootMap).getParentFile();
    this.packAll = packAll;
    this.zipDir = chosenDir;
    this.modifiedResources = modifiedResources;
  }
  
  /**
   * XXX
   * @param rootDir
   * @param chosenDir
   * @param packAll
   */
  public ZipWorker(File rootDir, File chosenDir, boolean packAll) {
    this.rootDir = rootDir;
    this.zipDir = chosenDir;
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
    if(packAll){
      ArchiveBuilder archiveBuilder = new ArchiveBuilder(listeners);
      archiveBuilder.zipDirectory(rootDir, zipDir);
    } else{
      ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
      String calculateTopLocation = PathUtil.calculateTopLocation(rootMap, packageBuilder);
      modifiedFilesNumber = packageBuilder.generateChangedFilesPackage(
          rootDir,
          zipDir, 
          modifiedResources, 
          false, 
          calculateTopLocation);

      List<String> filesNotCopied = packageBuilder.getFilesNotCopied();

      if (!filesNotCopied.isEmpty()) {
        // Avoid errors duplication.
        MessagePresenter.clearResultsPanel();
        for (String relPath : filesNotCopied) {
          String systemId = new URL(rootMap, relPath).toExternalForm();
          MessagePresenter.showInResultsPanel(DocumentPositionedInfo.SEVERITY_INFO, 
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
