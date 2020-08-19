package com.oxygenxml.translation.support.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.xml.bind.JAXBException;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.ui.CustomDialogResults;
import com.oxygenxml.translation.ui.ProgressChangeAdapter;
import com.oxygenxml.translation.ui.ProgressDialog;
import com.oxygenxml.translation.ui.Tags;
import com.oxygenxml.translation.ui.worker.GenerateMilestoneWorker;

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
   * @param notifyUserWhenFinished <code>true</code> to present a message when the milestone is generated.
   */
  public static void generateMilestone(
      final StandalonePluginWorkspace pluginWorkspaceAccess,
      final URL rootMap,
      final File milestoneFile,
      final boolean notifyUserWhenFinished) {
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
        if(notifyUserWhenFinished){
          pluginWorkspaceAccess.showInformationMessage(
              MessageFormat.format(resourceBundle.getMessage(Tags.MILESTONE_GENERATED), milestoneFile.getPath()));
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
   * 
   * @return <code>true</code> if a new milestone file shoud be generated.
   * 
   * @throws JAXBException        
   * @throws IOException
   */
  public static boolean askForMilestoneOverrideConfirmation(
      final StandalonePluginWorkspace pluginWorkspaceAccess, 
      URL rootMapLocation) throws JAXBException, IOException {
    // Creation date is written in milestone, thats why the JAXB exception is thrown.
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
    
    return result == CustomDialogResults.YES_OPTION;
  }
}
