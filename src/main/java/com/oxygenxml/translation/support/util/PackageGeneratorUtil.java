package com.oxygenxml.translation.support.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oxygenxml.translation.exceptions.NoChangedFilesException;
import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.ui.CustomDialogResults;
import com.oxygenxml.translation.ui.GenerateArchivePackageDialog;
import com.oxygenxml.translation.ui.ProgressChangeAdapter;
import com.oxygenxml.translation.ui.ProgressChangeListener;
import com.oxygenxml.translation.ui.ProgressDialog;
import com.oxygenxml.translation.ui.Tags;
import com.oxygenxml.translation.ui.worker.GenerateModifiedResourcesWorker;
import com.oxygenxml.translation.ui.worker.ZipWorker;

import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.util.UtilAccess;
import ro.sync.util.URLUtil;

/**
 * Utility class used in the process of creation the archive with the modified files.
 * 
 * @author adrian_sorop
 */
public class PackageGeneratorUtil {
  
  /**
   * Private constructor. Avoid instantiation.
   */
  private PackageGeneratorUtil() {
    // Nothing
  }
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = LoggerFactory.getLogger(PackageGeneratorUtil.class.getName());

  /**
   * Opens the generated xhtml file that contains the packed files.
   * 
   * @param rootMap Currently opened DITA map in DITA Maps Manager.
   */
  public static void openXHTMLReport(final URL rootMap) {
    //Open the report file           
    try {
      File rootMapFile = MilestoneUtil.getFile(rootMap);
      File generatedFile = new File(rootMapFile.getParentFile(), ProjectConstants.getHTMLReportFile(rootMapFile));
      if (generatedFile.exists()) {
        Desktop.getDesktop().open(generatedFile);
      }
    } catch (IOException e) {
      logger.error(String.valueOf(e), e);
    }
  }  
  
  
  /**
   *  
   * Packs the modified files or an entire directory in the specified chosenDir. 
   *  
   * @param rootMap The location of the parent directory of the current ditamap.
   * @param chosenDir Where to save the archive.
   * @param pluginWorkspace Entry point for accessing the DITA Maps area.
   * @param packAll  True if the user wants to pack the entire directory.
   * @param modifiedResources All the modified files.
   * @param generateXHTMLReport  True if the user wants to create a 
   *        report with the modified files.
   * 
   * @return The worker that generates the archive with modified files.
   */
  public static ZipWorker createPackage( 
      final URL rootMap, 
      File chosenDir,
      final StandalonePluginWorkspace pluginWorkspace,
      final boolean packAll,
      final List<ResourceInfo> modifiedResources,
      final boolean generateXHTMLReport) {

    // 1. Start the processing. (the ZIP Worker)
    // 2. Show the dialog. 
    // 3. The ZIP worker notifies the dialog.


    final PluginResourceBundle resourceBundle = pluginWorkspace.getResourceBundle();
    final ZipWorker zipTask;
    if(logger.isDebugEnabled()){
      logger.debug("pack entire dir: " + packAll);
    }
    if(packAll){
      zipTask = new ZipWorker(rootMap, chosenDir, packAll);
    } else { 
      zipTask = new ZipWorker(rootMap, chosenDir, modifiedResources);
    }

    // Install the progress tracker.
    ProgressDialog.install(
        zipTask, 
        (JFrame)pluginWorkspace.getParentFrame(), 
        resourceBundle.getMessage(Tags.CREATE_PACKAGE_ARCHIVE_TITLE));

    // This listener notifies the user about how the operation ended.
    zipTask.addProgressListener(new ProgressChangeAdapter() {     
      @Override
      public void done() { 
        if(packAll) {
          pluginWorkspace.showInformationMessage(resourceBundle.getMessage(Tags.DIRECTORY_WAS_PACKED));
        } else {
          int nrOfFiles = zipTask.getModifiedFilesNumber();
          pluginWorkspace.showInformationMessage(
              MessageFormat.format(resourceBundle.getMessage(Tags.REPORT_NUMBER_OF_MODIFIED_FILES), nrOfFiles));
          if(generateXHTMLReport){
            // If the xhtml report was requested, open it when the action is finished.
            openXHTMLReport(rootMap);
          }
        }
      }
            
      @Override
      public void operationFailed(Exception ex) {  
        //Treat differently Stop by user exceptions and the custom one about nothing to pack.
        if(ex instanceof NoChangedFilesException){
          pluginWorkspace.showInformationMessage(resourceBundle.getMessage(Tags.FAILURE_CREATING_PACKAGE) + "\n " + ex.getMessage());
        } else if(ex instanceof StoppedByUserException) {
          logger.error(String.valueOf(ex), ex);
        } else {
          logger.error(String.valueOf(ex), ex);
        }
      }
    });
    zipTask.execute();

    return zipTask;
  }
  
  /**
   * Creates the archive with the modified files.
   * 
   * @param pluginWorkspaceAccess Workspace access.
   * @param rootMap               Current file opened in DMM.
   */
  public static void createModifiedFilesPackage(
      final StandalonePluginWorkspace pluginWorkspaceAccess,
      final URL rootMap) {
    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
    // Find the number of modified resources on thread.
    final GenerateModifiedResourcesWorker modifiedResourcesWorker = new GenerateModifiedResourcesWorker(rootMap);
    // Install the progress tracker.
    ProgressDialog.install(
        modifiedResourcesWorker, 
        (JFrame)pluginWorkspaceAccess.getParentFrame(), 
        resourceBundle.getMessage(Tags.COLLECT_MODIFIED_RESOURCES));

    // This listener notifies the user about how the operation ended.
    modifiedResourcesWorker.addProgressListener(new ProgressChangeAdapter() {
      @Override
      public void done() {
        zipResources(pluginWorkspaceAccess, rootMap, modifiedResourcesWorker.getModifiedResources());                  
      }
      
      @Override
      public void operationFailed(Exception ex) {
        if(ex instanceof NoChangedFilesException){
          pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.FAILURE_CREATING_PACKAGE) + "\n " + ex.getMessage());                  
        } else if(ex instanceof StoppedByUserException) {
          logger.error(String.valueOf(ex), ex);
        } else {
          pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION_FAILED) + ex.getMessage());
        }               
      } 
    });
    modifiedResourcesWorker.execute();
  }
  
  
  /**
   * If modified files are present, a dialog that asks user where to save the archive is shown and zipps
   * the modified content.
   * 
   * @param pluginWorkspaceAccess The plugin workspace access.
   * @param rootMap               DITA map opened it DITA Maps Managed.
   * @param modifiedResources     A list with the files modified and ready for zipping.
   */
  private static void zipResources(
      final StandalonePluginWorkspace pluginWorkspaceAccess, 
      final URL rootMap, 
      List<ResourceInfo> modifiedResources) {
    // If the number of modified files is grater than 0 show the report dialog and create package.
    if(!modifiedResources.isEmpty()){
      // The resources were collected. Ask some user input and create the package.

      GenerateArchivePackageDialog createPackageDialog = GenerateArchivePackageDialog.getInstance();
      File correctedFile = URLUtil.getAbsoluteFileFromFileUrl(rootMap);
      createPackageDialog.showDialog(
          /*
           * The list with the modified resources.
           */
          modifiedResources,
          /*
           * The root map
           */
          correctedFile
          );
      //Create report and package only if the user pressed the "Save" button.
      if (createPackageDialog.getResult() == OKCancelDialog.RESULT_OK) {
        File chosenDir = createPackageDialog.getChoosedLocation();
        if(chosenDir != null){
          PackageGeneratorUtil.createPackage(
              rootMap, 
              chosenDir,
              pluginWorkspaceAccess, 
              false, 
              modifiedResources, 
              createPackageDialog.generateXHTMLReport());
        }
      }
    } else {  
      try {
        PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
        // Inform the user that no resources were modified.
        Date milestoneLastModified = MilestoneUtil.getMilestoneCreationDate(rootMap);
        pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.FAILURE_CREATING_PACKAGE) + "\n " +
            MessageFormat.format(resourceBundle.getMessage(Tags.NO_CHANGED_FILES), milestoneLastModified));                  
      } catch (JAXBException | IOException e) {
        logger.error(String.valueOf(e), e);
      }                
    }
  }
  
  /**
   * If a milestone is not found, we present alternatives to user: 
   * either to generate a new milestone(of course, this will not create a zip..no modifed files) or
   * to archive all the files and folders referred in DITA map.
   * 
   * @param pluginWorkspaceAccess The workspace access.
   * @param editorLocation        Currently opened file in DMM.
   * @param milestoneFile         The milestone file.
   */
  public static void modifiedFilespackageAlternatives(final StandalonePluginWorkspace pluginWorkspaceAccess,
      URL editorLocation, final File milestoneFile) {
    PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
    int result = pluginWorkspaceAccess.showConfirmDialog(
        // Title
        resourceBundle.getMessage(Tags.MILESTONE_MISSING),
        // Message
        MessageFormat.format(resourceBundle.getMessage(Tags.CREATE_NEW_MILESTONE), milestoneFile.getAbsolutePath()),
        //Buttons
        new String[] {
            resourceBundle.getMessage(Tags.YES_BUTTON), 
            resourceBundle.getMessage(Tags.NO_BUTTON), 
            resourceBundle.getMessage(Tags.PACK_ENTIRE_DIR)},
        // Button ids
        new int[] {
            CustomDialogResults.YES_OPTION, 
            CustomDialogResults.NO_OPTION, 
            CustomDialogResults.PACK_ALL_OPTION});
    
    //Generate the first milestone.
    if(result == CustomDialogResults.YES_OPTION) {
      // Adrian -- Something smells in this method. Add new issue to investigate how it works
      //and what is this "isFromAction1" parameter?????
      MilestoneGeneratorUtil.generateMilestone(pluginWorkspaceAccess, editorLocation, milestoneFile, false);
      
    } else if(result == CustomDialogResults.PACK_ALL_OPTION) {
      //If the user wants to pack the entire directory show a file chooser and create package.
      File chosenDirectory = pluginWorkspaceAccess.chooseFile(
          resourceBundle.getMessage(Tags.PACKAGE_LOCATION),
          new String[] {"zip"}, 
          resourceBundle.getMessage(Tags.ZIP_FILES), 
          true);
      
      if(chosenDirectory != null){
        PackageGeneratorUtil.createPackage(editorLocation, chosenDirectory, pluginWorkspaceAccess, true, null, false);
      }
      
    }
  }
  
  /**
   * Creates a ZIP Package with the given resources.
   * 
   * @param rootMap Main map from which all resources are referred.
   * @param listeners Listeners to be notified.
   * @param zipDestinationDir File where to save the zip.
   * @param modifiedResources Modified resources to put in the archive.
   * 
   * @return A list with the the files that for some reasons didn't make it in the archive.
   * 
   * @throws IOException problems creating the archive.
   * @throws StoppedByUserException The user canceled the operation though the progress listener.
   */
  public static List<URL> zipModifiedResources(
      URL rootMap, 
      List<ProgressChangeListener> listeners, 
      File zipDestinationDir,
      List<ResourceInfo> modifiedResources) throws IOException, StoppedByUserException {
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
    URL topLocationURL = PathUtil.calculateTopLocationURL(rootMap, packageBuilder);
    
    List<URL> collect = 
        modifiedResources.stream().map(t -> resolve(rootMap, t.getRelativePath())).collect(Collectors.toList());
    
    packageBuilder.generateChangedFilesPackage(
        zipDestinationDir, 
        collect, 
        topLocationURL);

    return packageBuilder.getFilesNotCopied();
  }

  /**
   * Detects and zips the entire root map top directory (the ancestor of all resources referred in the map.)
   * 
   * @param rootMap Main map from which all resources are referred.
   * @param listeners Listeners to be notified.
   * @param zipDestinationDir File where to save the zip.
   * 
   * @throws IOException problems creating the archive.
   * @throws StoppedByUserException The user canceled the operation though the progress listener.
   */
  public static void zipEntireRootMapStructure(
      URL rootMap, 
      List<ProgressChangeListener> listeners, 
      File zipDestinationDir)
      throws IOException, StoppedByUserException {
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
    URL topLocationURL = PathUtil.calculateTopLocationURL(rootMap, packageBuilder);
    
    ArchiveBuilder archiveBuilder = new ArchiveBuilder(listeners);
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    if (pluginWorkspace != null) {
      UtilAccess utilAccess = pluginWorkspace.getUtilAccess();
      if (utilAccess != null) {
        File locateFile = utilAccess.locateFile(topLocationURL);
        archiveBuilder.zipDirectory(locateFile, zipDestinationDir);
      }
    }
  }
  
  /**
   * Resolves the relative path.
   * 
   * @param baseURL The base URL. 
   * @param relativePath Relative path to resolve.
   *  
   * @return Resolved URL.
   */
  private static URL resolve(final URL baseURL, String relativePath) {
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
  
}
