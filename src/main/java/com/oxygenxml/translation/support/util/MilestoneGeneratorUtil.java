package com.oxygenxml.translation.support.util;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.ui.CustomDialogResults;
import com.oxygenxml.translation.ui.ProgressChangeAdapter;
import com.oxygenxml.translation.ui.ProgressDialog;
import com.oxygenxml.translation.ui.Tags;
import com.oxygenxml.translation.ui.worker.GenerateMilestoneWorker;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import javax.swing.JFrame;
import javax.xml.bind.JAXBException;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Utility class used in the process of creating milestones.
 * 
 * @author adrian_sorop
 */
public class MilestoneGeneratorUtil {
  
  /**
   * Private constructor. Avoid instantiation.
   */
  private MilestoneGeneratorUtil() {
    // Nothing
  }
  
  /**
   * Generates the milestone file in the specified rootDir.
   * 
   * @param pluginWorkspaceAccess Entry point for accessing the DITA Maps area.
   * @param rootMap The parent directory of the current ditamap.
   * @param milestoneFile The predefined location of the milestone file.
   */
  public static void generateMilestone(final StandalonePluginWorkspace pluginWorkspaceAccess,
      final URL rootMap,
      final File milestoneFile,
      final boolean isFromAction1) {
    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
    
    // Generate the milestone on thread.
    GenerateMilestoneWorker milestoneWorker = new GenerateMilestoneWorker(rootMap);

    // Install the progress tracker.
    ProgressDialog.install(
        milestoneWorker, 
        (JFrame) pluginWorkspaceAccess.getParentFrame(), 
        resourceBundle.getMessage(Tags.GENERATING_MILESTONE));

    // This listener notifies the user about how the operation ended.
    milestoneWorker.addProgressListener(new ProgressChangeAdapter() {
      @Override
      public void done() { 
        if(isFromAction1){
          pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION1_INFO_MESSAGE) + milestoneFile.getPath());
        } else {
          // TODO Adrian THis will never put anything in the ZIP. The milestone is created for the current file states.
          PackageGeneratorUtil.createModifiedFilesPackage(pluginWorkspaceAccess, rootMap);
        }
      }
      @Override
      public void operationFailed(Exception ex) {
        if(!(ex instanceof StoppedByUserException)){
          pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.MILESTONE_CREATION_FAILED_BECAUSE) + ex.getMessage());
        }
      }
    });
    milestoneWorker.execute();
  }
  
  /**
   * Shows a dialog and asks user for milestone overriding confirmation. 
   * 
   * @param pluginWorkspaceAccess Workspace access.
   * @param rootMapLocation       Location of the DITA map opened in DITA Maps Manager.
   * @param milestoneFile         Milestone file.
   * 
   * @throws JAXBException        
   * @throws IOException
   */
  public static void askForMilestoneOverrideConfirmation(final StandalonePluginWorkspace pluginWorkspaceAccess, URL rootMapLocation,
      final File milestoneFile) throws JAXBException, IOException {
    // TODO Adrian - getMilestoneCreationDate throws JAXB Exception!! Not cool!!
    Date milestoneLastModified = MilestoneUtil.getMilestoneCreationDate(rootMapLocation);
    PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
    
    // TODO Adrian i18n this.
    int result = pluginWorkspaceAccess.showConfirmDialog(
        "Override milestone",
        "An older milestone was created at: " + milestoneLastModified + ". Do you want to override it?",
        new String[] {
            resourceBundle.getMessage(Tags.YES_BUTTON), 
            resourceBundle.getMessage(Tags.NO_BUTTON)}, 
        new int[] {
            CustomDialogResults.YES_OPTION,
            CustomDialogResults.NO_OPTION});
    
    if(result == CustomDialogResults.YES_OPTION){
      generateMilestone(pluginWorkspaceAccess, rootMapLocation, milestoneFile, true);
    }
  }
}
