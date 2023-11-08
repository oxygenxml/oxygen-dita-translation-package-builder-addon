package com.oxygenxml.translation.support.util;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.ui.PreviewDialog;
import com.oxygenxml.translation.ui.ProgressChangeAdapter;
import com.oxygenxml.translation.ui.ProgressDialog;
import com.oxygenxml.translation.ui.Tags;
import com.oxygenxml.translation.ui.worker.UnzipWorker;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.util.URLUtil;

/**
 * Utility class used in the process of applying the translated package.
 * 
 * @author adrian_sorop
 */
public class ApplyPackageUtil {
  
  /**
   * Private. Avoid instantiation.
   */
  private ApplyPackageUtil() {
    // Nothing
  }
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = LoggerFactory.getLogger(ApplyPackageUtil.class.getName());

  
  /**
   * Unzipps the archive in a temporary folder and presents a dialog with the modified files. User
   * can compare files with his files and decides what he saves and what not.
   * 
   * @param workspaceAccess   PLugin workspace access.
   * @param unzipingLocation     Where to unzip the files.
   * @param chosenDir         Path to zip file.
   */
  public static void previewTranslatedFiles(
      final StandalonePluginWorkspace workspaceAccess, 
      final File unzipingLocation, 
      final File chosenDir) {

    final PluginResourceBundle resourceBundle = workspaceAccess.getResourceBundle();
    final File tempDir = PathUtil.createTempDirectory();
    if(tempDir != null && tempDir.exists()){
      if (logger.isDebugEnabled()) {
        logger.debug(tempDir.getAbsolutePath());
      }
      // Unzip the chosen package into a temporary directory on thread.
      final UnzipWorker unzipTask = new UnzipWorker(chosenDir, tempDir);
      // Install the progress tracker.
      ProgressDialog.install(
          unzipTask, 
          (JFrame)workspaceAccess.getParentFrame(), 
          resourceBundle.getMessage(Tags.OPENING_PACKAGE));
      // This listener notifies the user about how the operation ended.
      unzipTask.addProgressListener(new ProgressChangeAdapter() {
        @Override
        public void done() { 
          if (tempDir != null && unzipingLocation != null) {
            new PreviewDialog((JFrame)workspaceAccess.getParentFrame(), 
                unzipTask.getUnpackedFiles(), 
                unzipingLocation, 
                tempDir);
          }
        }
        @Override
        public void operationFailed(Throwable ex) {
          if(!(ex instanceof StoppedByUserException)){
            workspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.FAILED_TO_APPLY_PACKAGE) + ex.getMessage());
          }
        }
      });
      unzipTask.execute();
    }
  }
  
  /**
   * Overrides the files contained in a chosen archive.
   * 
   * @param pluginWorkspaceAccess  Entry point for accessing the DITA Maps area.
   * @param unzippingLocation Where to unzip the archive.
   * @param archiveLocation Archive location
   */
  public static UnzipWorker overrideTranslatedFiles(final StandalonePluginWorkspace pluginWorkspaceAccess,
      File unzippingLocation, final File archiveLocation) {
    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
    
    UnzipWorker unzipTask = null;
    if(archiveLocation != null) {  
      try { 
        unzipTask = unZippit(pluginWorkspaceAccess, unzippingLocation, archiveLocation);
      } catch (Exception e) {
        logger.error(String.valueOf(e), e);
        // Preset error to user.
        pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.FAILED_TO_APPLY_PACKAGE) + e.getMessage());
      }
    }
    return unzipTask;
  }
  
  /**
   * Execute the UnZipping process.
   * 
   * @param pluginWorkspaceAccess   Current plugin workspace.
   * @param unzippingLocation       Where to unzip files.
   * @param archiveLocation         Archive location.
   * 
   * @return  The unZipping swing worker.
   */
  private static UnzipWorker unZippit(final StandalonePluginWorkspace pluginWorkspaceAccess,
      File unzippingLocation, final File archiveLocation) {
    // Unzip the chosen package over the parent  directory of the current ditamap on thread.
    UnzipWorker unzipTask = new UnzipWorker(archiveLocation, unzippingLocation);
    // Install the progress tracker.
    JFrame parentFrame = (JFrame) pluginWorkspaceAccess.getParentFrame();
    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
    ProgressDialog.install(unzipTask, parentFrame, resourceBundle.getMessage(Tags.OPENING_PACKAGE));
    
    // This listener notifies the user about how the operation ended.
    final UnzipWorker[] taks = new UnzipWorker[] {unzipTask};
    unzipTask.addProgressListener(new ProgressChangeAdapter() {
      @Override
      public void done() {
        try {
          showUnZippedFilesReport(pluginWorkspaceAccess, taks[0].getUnpackedFiles());
        } catch (Exception e) {
          if (logger.isDebugEnabled()) {
            logger.debug(String.valueOf(e), e);
          }
        }
      }
      @Override
      public void operationFailed(Throwable ex) {
        if(!(ex instanceof StoppedByUserException)){
          pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.FAILED_TO_APPLY_PACKAGE) + ex.getMessage());
        }
      }
    });
    unzipTask.execute();
    return unzipTask;
  }
  
  /**
   *  Shows a message dialog with the unpacked/overriden files.
   * 
   * @param pluginWorkspaceAccess  Entry point for accessing the DITA Maps area.
   * @param list  The relative paths of the unzipped files.
   * @throws IOException  Problems reading the files.
   */
  private static void showUnZippedFilesReport(final StandalonePluginWorkspace pluginWorkspaceAccess,
      List<String> list) throws IOException {
    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();

    // Present a log with the overridden files.
    if(list != null && !list.isEmpty()){
      JTextArea text = new JTextArea(10, 40);

      // Iterate with an index and put a new line
      //    for all lines except the first one. 
      text.append(list.get(0));
      for(int i = 1; i < list.size(); i++){
        text.append("\n");
        text.append(list.get(i));
      }
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);

      JScrollPane scroll = new JScrollPane(text);
      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

      JPanel panel = new JPanel(new GridBagLayout());

      GridBagConstraints gbcLabel = new GridBagConstraints();
      gbcLabel.gridx = 0;
      gbcLabel.gridy = 0;
      gbcLabel.gridwidth = 1;
      gbcLabel.gridheight = 1;
      gbcLabel.weightx = 0;
      gbcLabel.weighty = 0;
      gbcLabel.fill = GridBagConstraints.HORIZONTAL;
      gbcLabel.anchor = GridBagConstraints.NORTH;

      panel.add(new JLabel(resourceBundle.getMessage(Tags.APPLY_PACKAGE_REPORT)), gbcLabel);

      GridBagConstraints gbcScroll = new GridBagConstraints();
      gbcScroll.gridx = 0;
      gbcScroll.gridy = 1;
      gbcScroll.gridwidth = 1;
      gbcScroll.gridheight = 1;
      gbcScroll.weightx = 0;
      gbcScroll.weighty = 0;
      gbcScroll.fill = GridBagConstraints.BOTH;
      gbcScroll.anchor = GridBagConstraints.LINE_START;
      panel.add(scroll , gbcScroll);

      JOptionPane.showMessageDialog(
          (JFrame) pluginWorkspaceAccess.getParentFrame(), 
          panel, 
          resourceBundle.getMessage(Tags.UPDATED_FILES), 
          JOptionPane.INFORMATION_MESSAGE);
    } else {
      throw new IOException(resourceBundle.getMessage(Tags.NO_FILES_IN_PACKAGE));
    }
  }
  
  /**
   * 
   *  Open the diff files tool with initial left and right URLs to compare. 
   *  The comparison will begin automatically and the content types for the URLs will be auto-detected.
   * 
   * @param localFile The location of the current file on disk.
   * @param translatedFile The location of the unpacked file. The file from the chosen archive.
   */
  public static void showDiff(File localFile, File translatedFile) {
    try {
      URL leftURL = URLUtil.correct(localFile.toURI().toURL());
      URL rightURL = URLUtil.correct(translatedFile.toURI().toURL());
      
      //Check if the url it's a supported Oxygen file
      final StandalonePluginWorkspace pluginWorkspace = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace());
      if(!pluginWorkspace.getUtilAccess().isUnhandledBinaryResourceURL(rightURL)){
        pluginWorkspace.openDiffFilesApplication(leftURL, rightURL);
      } else {
        pluginWorkspace.showInformationMessage(
            pluginWorkspace.getResourceBundle().getMessage(Tags.FILE_TYPE_NOT_SUPPORTED));
      }
    } catch (MalformedURLException e2) {
      // Shouldn't happen.
      logger.error(String.valueOf(e2), e2);
    }
  }
  
}
