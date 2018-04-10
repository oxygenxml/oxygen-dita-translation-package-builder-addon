package com.oxygenxml.translation.support;

import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.util.ApplyPackageUtil;
import com.oxygenxml.translation.support.util.MilestoneGeneratorUtil;
import com.oxygenxml.translation.support.util.PackageGeneratorUtil;
import com.oxygenxml.translation.support.util.PathUtil;
import com.oxygenxml.translation.ui.CustomDialogResults;
import com.oxygenxml.translation.ui.PackageAndUpdateMilestoneDialog;
import com.oxygenxml.translation.ui.Tags;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.ditamap.WSDITAMapEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.actions.MenusAndToolbarsContributorCustomizer;
import ro.sync.exml.workspace.api.standalone.ui.Menu;

/**
 * Plug-in extension - workspace access extension.
 */
public class TranslationPackageBuilderExtension implements WorkspaceAccessPluginExtension, Tags {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(TranslationPackageBuilderExtension.class.getName());
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    
    final Action generateMilestoneAction = createMilestoneAction(pluginWorkspaceAccess);
    final Action generateChangedFilesZipAction = createChangedFilesZipAction(pluginWorkspaceAccess);
    final Action applyTranslatedFilesAction = createApplyTranslatedFilesAction(pluginWorkspaceAccess);

    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();

    //Mount the action on the contextual menus for the Text and Author modes.
    pluginWorkspaceAccess.addMenusAndToolbarsContributorCustomizer(new MenusAndToolbarsContributorCustomizer() {

      @Override
      public void customizeDITAMapPopUpMenu(JPopupMenu popUp, WSDITAMapEditorPage ditaMapEditorPage) {
        //Create a submenu "Translation Package Builder" for the 3 actions.
        // Tooltips for all actions.
        JMenu submenu = new Menu(resourceBundle.getMessage(Tags.TRANSLATION_PACKAGE_BUILDER_PLUIGIN_NAME));
        submenu.setMnemonic(KeyEvent.VK_S);

        // Action 1: Generate Milestone
        String generateMilestone = resourceBundle.getMessage(Tags.JMENU_ITEM1);
        JMenuItem menuItemMilestone = new JMenuItem(generateMilestone);
        menuItemMilestone.addActionListener(generateMilestoneAction);
        menuItemMilestone.setToolTipText(resourceBundle.getMessage(Tags.JMENU_TOOLTIP_ITEM1));

        // Action 2: Create Changed Files Package
        JMenuItem menuItemPakage = new JMenuItem(resourceBundle.getMessage(Tags.CREATE_MODIFIED_FILES_PACKAGE));
        menuItemPakage.addActionListener(generateChangedFilesZipAction);
        menuItemPakage.setToolTipText(resourceBundle.getMessage(Tags.JMENU_TOOLTIP_ITEM2));

        // Action 3: Unzip package that came from translation.
        JMenuItem menuItemApply = new JMenuItem(resourceBundle.getMessage(Tags.JMENU_ITEM3));
        menuItemApply.addActionListener(applyTranslatedFilesAction);
        menuItemApply.setToolTipText(resourceBundle.getMessage(Tags.JMENU_TOOLTIP_ITEM3));
        
        JMenuItem generateMilestoneWithLocation = new JMenuItem(generateMilestone + "...");
        generateMilestoneWithLocation.setToolTipText("Choose place where to generate milstone");
        generateMilestoneWithLocation.addActionListener(
            new AbstractAction("Generate Milestone To Location") {
              @Override
              public void actionPerformed(ActionEvent e) {
                File chooseFile = pluginWorkspaceAccess.chooseFile("Save Milestone", new String[] {"xml"}, "XML Documents", true);
                if (chooseFile != null) {
                  WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
                  if (editor != null) {
                    URL editorLocation = editor.getEditorLocation();
                    MilestoneGeneratorUtil.generateMilestone(pluginWorkspaceAccess, editorLocation, chooseFile, true);
                  }
                }
              }
            }
          );
        
        JMenuItem pack = new JMenuItem("Generate package/milesone");
        pack.addActionListener(new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
            if (editor != null) {
              
              URL rootMapLocation = editor.getEditorLocation();
              File fileOnDisk = pluginWorkspaceAccess.getUtilAccess().locateFile(rootMapLocation);
              final File milestoneFile = MilestoneUtil.getMilestoneFile(fileOnDisk);
              if (milestoneFile.exists()) {
                PackageGeneratorUtil.createModifiedFilesPackage(pluginWorkspaceAccess, rootMapLocation, null);
              } else {
                PackageAndUpdateMilestoneDialog instance = PackageAndUpdateMilestoneDialog.getInstance();
                instance.showDialog(pluginWorkspaceAccess, editor.getEditorLocation());
              }
              
            }
          }
        });
        
        
        submenu.add(pack);
        submenu.add(menuItemApply);
//        submenu.add(menuItemMilestone);
//        submenu.add(menuItemPakage);
//        submenu.add(generateMilestoneWithLocation);

        popUp.add(submenu);
      }

    });
  }

  /**
   * Creates an action that stores the state of the files.
   * 
   * @param pluginWorkspaceAccess  Entry point for accessing the DITA Maps area.
   * @return A new action called "Generate Milestone".
   */
  private AbstractAction createMilestoneAction(
      final StandalonePluginWorkspace pluginWorkspaceAccess) {
    return new AbstractAction("Generate Milestone") {
      public void actionPerformed(ActionEvent actionevent) {
        // 1. Extract the parent directory of the current map.
        // 2. Generate the milestone file in the dir
        
        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        try {
          URL rootMapLocation = editor.getEditorLocation();
          File fileOnDisk = pluginWorkspaceAccess.getUtilAccess().locateFile(rootMapLocation);
          if (logger.isDebugEnabled()) {
            logger.debug("The current DITA MAP is : " + fileOnDisk.getPath());
          }
          
          final File milestoneFile = MilestoneUtil.getMilestoneFile(fileOnDisk);
          //Ask the user if he wants to override the milestone in case it was already created.
          if(milestoneFile.exists()){
            MilestoneGeneratorUtil.askForMilestoneOverrideConfirmation(pluginWorkspaceAccess, rootMapLocation, milestoneFile);
          } else {
            MilestoneGeneratorUtil.generateMilestone(pluginWorkspaceAccess, rootMapLocation, milestoneFile, true);
          }
        } catch (Exception e) {
          // Present the error to the user.
          final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
          pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.MILESTONE_CREATION_FAILED_BECAUSE) + e.getMessage());
        }
      }
      
    };
  }

  /**
   * Creates an action that packs the changed files into an archive.
   * 
   * @param pluginWorkspaceAccess  Entry point for accessing the DITA Maps area.
   * @return  A new action called "Create Modified Files Package".
   */
  private AbstractAction createChangedFilesZipAction(
      final StandalonePluginWorkspace pluginWorkspaceAccess) {
    return new AbstractAction("Create Modified Files Package") {
      public void actionPerformed(ActionEvent actionevent) {
        final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
        
        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        URL editorLocation = editor.getEditorLocation();
        // 1. Extract the parent directory of the current map. This is the rootDir
        File fileOnDisk = pluginWorkspaceAccess.getUtilAccess().locateFile(editorLocation);
        if (logger.isDebugEnabled()) {
          logger.debug("The current ditaMAP is : " + fileOnDisk.getAbsolutePath());
        }

        try {
          //The milestone file is stored in the root dir of the current ditaMAP
          final File milestoneFile = new File(fileOnDisk.getParentFile() , MilestoneUtil.getMilestoneFileName(fileOnDisk));      
          // What to do if the milestone file doesn't exist? 
          // Inform the user and offer the possibility to pack the entire dir
          if(!milestoneFile.exists()){
            PackageGeneratorUtil.modifiedFilespackageAlternatives(pluginWorkspaceAccess, editorLocation, milestoneFile);
          } else {  
            PackageGeneratorUtil.createModifiedFilesPackage(pluginWorkspaceAccess, editorLocation, null);      
          }
        } catch (Exception e) {
          // Preset error to user.
          logger.error(e, e);
          pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION2_ERROR_MESSAGE) + e.getMessage());
        }
      }
    };
  }
  
  /**
   * Creates the action that applies the translated files.
   * 
   * @param pluginWorkspaceAccess Entry point for accessing the DITA Maps area.
   * 
   * @return  A new action called "Apply Package".
   */
  private AbstractAction createApplyTranslatedFilesAction(
      final StandalonePluginWorkspace pluginWorkspaceAccess) {
    return new AbstractAction("Apply Package") {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent actionevent) {
        /**
         * Flow.
         * 
         * 1. The user opens for example : map-french.ditamap.
         * 2. From the contextual menu chooses "Apply package".
         * 3. The action presents a chooser so that the user can select the ZIP.
         * 4. Unzip the package in the root directory of the map.
         */
        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        URL editorLocation = editor.getEditorLocation();
        // The parent directory of the current ditamap.
        File fileOnDisk = pluginWorkspaceAccess.getUtilAccess().locateFile(editorLocation);
        final File rootDir = fileOnDisk.getParentFile();
        final File unzipLocation = PathUtil.calculateTopLocationFile(editorLocation);
        
        final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
        final File chosenDir = pluginWorkspaceAccess.chooseFile(resourceBundle.getMessage(Tags.ACTION3_CHOOSE_FILE_TITLE), new String[] {"zip"},  null);

        // DIFF FLOW
        // 1. Ask user if he wants a preview.
        // 2. If yes, then extract to temp dir.
        // 3. Present a dialog with the list of unzipped files. Use aJList.
        // 4. When the user double clicks an entry launch the DIFF (left is LOCAL, RIGHT is FROM_PACKAGE).
        // 5. In dialog the user presses Apply to copy all selected files.
        if(chosenDir != null){
          int buttonId = pluginWorkspaceAccess.showConfirmDialog(
              resourceBundle.getMessage(Tags.PREVIEW_CHANGES), 
              resourceBundle.getMessage(Tags.PREVIEW_CHANGES_USER_QUESTION),
              new String[] {
                  resourceBundle.getMessage(Tags.PREVIEW), 
                  resourceBundle.getMessage(Tags.APPLY_ALL), 
                  resourceBundle.getMessage(Tags.CANCEL)},
              new int[] {
                  CustomDialogResults.PREVIEW_OPTION, 
                  CustomDialogResults.APPLY_ALL_OPTION, 
                  CustomDialogResults.CANCEL_OPTION});
          
          if(buttonId == CustomDialogResults.PREVIEW_OPTION){
            // If user wants a preview
            ApplyPackageUtil.previewTranslatedFiles(pluginWorkspaceAccess, unzipLocation, chosenDir);
            
          } else if (buttonId == CustomDialogResults.APPLY_ALL_OPTION) {
            //Unpack the chosen archive over the root directory of the DITA map.
            ApplyPackageUtil.overrideTranslatedFiles(pluginWorkspaceAccess,
                unzipLocation == null ? rootDir : unzipLocation, 
                chosenDir); 
          }
        }
      }
    };
  }
  
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  public boolean applicationClosing() {
    //You can reject the application closing here
    return true;
  }
}