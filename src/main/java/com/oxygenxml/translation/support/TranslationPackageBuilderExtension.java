                                                                                                                                                                                                                                                                                                                                                        package com.oxygenxml.translation.support;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.core.PackageBuilder;
import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.ui.NoChangedFilesException;
import com.oxygenxml.translation.ui.PreviewDialog;
import com.oxygenxml.translation.ui.ProgressChangeAdapter;
import com.oxygenxml.translation.ui.ProgressDialog;
import com.oxygenxml.translation.ui.ReportDialog;
import com.oxygenxml.translation.ui.StoppedByUserException;
import com.oxygenxml.translation.ui.Tags;
import com.oxygenxml.translation.ui.worker.GenerateMilestoneWorker;
import com.oxygenxml.translation.ui.worker.GenerateModifiedResourcesWorker;
import com.oxygenxml.translation.ui.worker.UnzipWorker;
import com.oxygenxml.translation.ui.worker.ZipWorker;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.ditamap.WSDITAMapEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.actions.MenusAndToolbarsContributorCustomizer;

/**
 * Plugin extension - workspace access extension.
 */
public class TranslationPackageBuilderExtension implements WorkspaceAccessPluginExtension, Tags {
  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(TranslationPackageBuilderExtension.class); 
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    //You can set or read global options.
    //The "ro.sync.exml.options.APIAccessibleOptionTags" contains all accessible keys.
    //      pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);
    // Check In action
    //You can access the submenu Translation Package Builder only from the DITA Maps area.
    // The submenu contains these 3 actions.
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
        JMenu submenu = new JMenu(resourceBundle.getMessage(Tags.JMENU_TITLE));
        submenu.setMnemonic(KeyEvent.VK_S);

        // Action 1: Generate Milestone
        JMenuItem menuItemMilestone = new JMenuItem(resourceBundle.getMessage(Tags.JMENU_ITEM1));
        menuItemMilestone.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_2, ActionEvent.ALT_MASK));
        menuItemMilestone.addActionListener(generateMilestoneAction);
        menuItemMilestone.setToolTipText(resourceBundle.getMessage(Tags.JMENU_TOOLTIP_ITEM1));

        // Action 2: Create Changed Files Package
        JMenuItem menuItemPakage = new JMenuItem(resourceBundle.getMessage(Tags.JMENU_ITEM2));
        menuItemPakage.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_3, ActionEvent.ALT_MASK));
        menuItemPakage.addActionListener(generateChangedFilesZipAction);
        menuItemPakage.setToolTipText(resourceBundle.getMessage(Tags.JMENU_TOOLTIP_ITEM2));

        // Action 3: Unzip package that came from translation.
        JMenuItem menuItemApply = new JMenuItem(resourceBundle.getMessage(Tags.JMENU_ITEM3));
        menuItemApply.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_4, ActionEvent.ALT_MASK));
        menuItemApply.addActionListener(applyTranslatedFilesAction);
        menuItemApply.setToolTipText(resourceBundle.getMessage(Tags.JMENU_TOOLTIP_ITEM3));

        submenu.add(menuItemMilestone);
        submenu.add(menuItemPakage);
        submenu.add(menuItemApply);

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
      final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
      public void actionPerformed(ActionEvent actionevent) {

        // 1. Extract the parent directory of the current map.
        // 2. Generate the milestone file in the dir
        
        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        try {
          URL editorLocation = editor.getEditorLocation();

          File fileOnDisk = pluginWorkspaceAccess.getUtilAccess().locateFile(editorLocation);
          final File rootDir = fileOnDisk.getParentFile();
          logger.debug("The root dir path is : " + rootDir.getPath());
          
          final File milestoneFile = new File(rootDir, PackageBuilder.getMilestoneFileName());
          //Ask the user if he wants to override the milestone in case it was already created.
          if(milestoneFile.exists()){
            int buttonId = pluginWorkspaceAccess.showConfirmDialog("Override milestone",
                "The milestone file is already there. Do you want to override it?", 
                new String[] {"Yes", "No"}, 
                new int[] {0, 1});
            if(buttonId == 0){
              generateMilestone(pluginWorkspaceAccess, rootDir, milestoneFile);
            }
          } else {
            generateMilestone(pluginWorkspaceAccess, rootDir, milestoneFile);
          }
        } catch (Exception e) {
          // Present the error to the user.
          logger.error(e, e);
          pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION1_ERROR_MESSAGE) + e.getMessage());
        }
      }
      /**
       * Generates the milestone file in the specified rootDir.
       * 
       * @param pluginWorkspaceAccess Entry point for accessing the DITA Maps area.
       * @param rootDir The parent directory of the current ditamap.
       * @param milestoneFile The predefined location of the milestone file.
       */
      private void generateMilestone(final StandalonePluginWorkspace pluginWorkspaceAccess,
          final File rootDir,
          final File milestoneFile) {
        // Generate the milestone on thread.
        GenerateMilestoneWorker milestoneWorker = new GenerateMilestoneWorker(rootDir);

        // Install the progress tracker.
        ProgressDialog.install(
            milestoneWorker, 
            (JFrame) pluginWorkspaceAccess.getParentFrame(), 
            resourceBundle.getMessage(Tags.ACTION1_PROGRESS_TITLE));

        // This listener notifies the user about how the operation ended.
        milestoneWorker.addProgressListener(new ProgressChangeAdapter() {
          public void done() { 
            
            pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION1_INFO_MESSAGE) + milestoneFile.getPath());
          }
          public void operationFailed(Exception ex) {
            if(!(ex instanceof StoppedByUserException)){
              pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION1_ERROR_MESSAGE) + ex.getMessage());
            }
          }
        });
        milestoneWorker.execute();
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

      final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
      public void actionPerformed(ActionEvent actionevent) {

        final JFrame frame = (JFrame) pluginWorkspaceAccess.getParentFrame();
        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        URL editorLocation = editor.getEditorLocation();
        // 1. Extract the parent directory of the current map. This is the rootDir
        File fileOnDisk = pluginWorkspaceAccess.getUtilAccess().locateFile(editorLocation);
        final File rootDir = fileOnDisk.getParentFile();
        logger.debug("The root dir id : " + rootDir.getAbsolutePath());

        try {
          final File milestoneFile = new File(rootDir , PackageBuilder.getMilestoneFileName());      
          // What to do if the milestone file doesn't exist? 
          // Inform the user and offer the possibility to pack the entire dir
          if(!milestoneFile.exists()){
            int buttonId = pluginWorkspaceAccess.showConfirmDialog(resourceBundle.getMessage(Tags.ACTION2_NO_MILESTONE_DIALOG_TITLE),
                resourceBundle.getMessage(Tags.ACTION2_NO_MILESTONE_DIALOG_MESSAGE) +
                rootDir.getPath() +"?", 
                new String[] {resourceBundle.getMessage(Tags.YES_BUTTON), resourceBundle.getMessage(Tags.NO_BUTTON)},
                new int[] {0, 1});
            // If the user wants to pack the entire directory show a file chooser and create package.
            if(buttonId == 0){     
              File chosenDirectory = pluginWorkspaceAccess.chooseFile(resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE),
                  new String[] {"zip"}, resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_DESCRIPTOR), true);
              if(chosenDirectory != null){
                createPackage(frame, rootDir, chosenDirectory, resourceBundle, pluginWorkspaceAccess, true, null, false);
              }             
            }else{
              return;
            }
          } else {  
            // Find the number of modified resources on thread.
            final GenerateModifiedResourcesWorker modifiedResourcesWorker = 
                new GenerateModifiedResourcesWorker(rootDir);
            // Install the progress tracker.
            ProgressDialog.install(
                modifiedResourcesWorker, 
                frame , 
                resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_PROGRESS_TITLE));

            // This listener notifies the user about how the operation ended.
            modifiedResourcesWorker.addProgressListener(new ProgressChangeAdapter() {                                          
              public void done() { 
                if(logger.isDebugEnabled()){
                  logger.debug(resourceBundle.getMessage(Tags.CREATE_PACKAGE_LOGGER_MESSAGE5) + modifiedResourcesWorker.getModifiedResources().size());
                }
                // If the number of modified files is grater than 0 show the report dialog and create package.
                if(!modifiedResourcesWorker.getModifiedResources().isEmpty()){  
                  ReportDialog.setParentFrame(frame);
                  ReportDialog.setRootDir(rootDir);
                  ReportDialog.setModifiedResources(modifiedResourcesWorker.getModifiedResources());
                  
                  ReportDialog report = ReportDialog.getInstance();
                  
                  logger.debug("The Save button is : " + report.isSaveButtonPressed());
                  //Create report and package only if the user pressed the "Save" button.
                  if(report.isSaveButtonPressed()){
                    File chosenDir = report.getChoosedLocation();
                    if(chosenDir != null){
                      createPackage(frame,
                          rootDir, 
                          chosenDir,
                          resourceBundle,
                          pluginWorkspaceAccess, 
                          false, 
                          modifiedResourcesWorker.getModifiedResources(),
                          report.isShouldCreateReport());
                    }
                  }
                  report.setSaveButtonPressed(false);
                } else {  
                  // Inform the user that no resources were modified.
                  pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION2_INFO_MESSAGE_EXCEPTION) + "\n " +
                      resourceBundle.getMessage(Tags.ACTION2_NO_CHANGED_FILES_EXCEPTION) + new Date(milestoneFile.lastModified()));                  
                }                  
              }                
              public void operationFailed(Exception ex) {
                if(ex instanceof NoChangedFilesException){
                  pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION2_INFO_MESSAGE_EXCEPTION) + "\n " + ex.getMessage());                  
                } else if(ex instanceof StoppedByUserException) {
                  logger.error(ex, ex);
                } else {
                  pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION2_ERROR_MESSAGE) + ex.getMessage());
                }               
              } 
            });
            modifiedResourcesWorker.execute();      
          }
        } catch (Exception e) {
          //  Preset error to user.
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
      final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent actionevent) {

        final JFrame frame = (JFrame)pluginWorkspaceAccess.getParentFrame();
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
//        final File rootDir = new File(editorLocation.getFile()).getParentFile();
        File fileOnDisk = pluginWorkspaceAccess.getUtilAccess().locateFile(editorLocation);
        final File rootDir = fileOnDisk.getParentFile();
        logger.debug("The root dir id : " + rootDir.getAbsolutePath());
        final File chosenDir = pluginWorkspaceAccess.chooseFile(resourceBundle.getMessage(Tags.ACTION3_CHOOSE_FILE_TITLE), new String[] {"zip"},  null);

        // DIFF FLOW
        // 1. Ask user if he wants a preview.
        // 2. If yes, then extract to temp dir.
        // 3. Present a dialog with the list of unzipped files. Use aJList.
        // 4. When the user double clicks an entry launch the DIFF (left is LOCAL, RIGHT is FROM_PACKAGE).
        // 5. In dialog the user presses Apply to copy all selected files.
        if(chosenDir != null){
          int buttonId = pluginWorkspaceAccess.showConfirmDialog(
              resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_TITLE), 
              resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_MESSAGE),
              new String[] {resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_BUTTON1), resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_BUTTON2), resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_BUTTON3)},
              new int[] {0, 1, 2});
          // If user wants a preview
          if(buttonId == 0){
            File tempFile = null;
            try {            
              tempFile = File.createTempFile("tempFile", null);
            } catch (IOException e) {
              logger.error(e, e);
            }
            if(tempFile.exists()){
              final File tempDir = new File(tempFile.getParentFile(), resourceBundle.getMessage(Tags.ACTION3_TEMPDIR_NAME));

              if (logger.isDebugEnabled()) {
                logger.debug(tempDir.getAbsolutePath());
              }
              // Unzip the chosen package into a temprary directory on thread.
              final UnzipWorker unzipTask = new UnzipWorker(chosenDir, tempDir);
              // Install the progress tracker.
              ProgressDialog.install(
                  unzipTask, 
                  frame , 
                  resourceBundle.getMessage(Tags.ACTION3_PROGRESS_DIALOG_TITLE));
              // This listener notifies the user about how the operation ended.
              unzipTask.addProgressListener(new ProgressChangeAdapter() {
                public void done() { 
                  new PreviewDialog(frame, unzipTask.getUnpackedFiles(), rootDir, tempDir);
                }
                public void operationFailed(Exception ex) {
                  if(!(ex instanceof StoppedByUserException)){
                    pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION3_ERROR_MESSAGE) + ex.getMessage());
                  }
                }
              });
              unzipTask.execute();
            }
          }
          // If the user doesn't want a preview and pressed "Apply All"
          else if (buttonId == 1){
            //Unpack the chosen archive over the root directory of the DITA map.
            overrideTranslatedFiles(pluginWorkspaceAccess, frame, rootDir, chosenDir); 
          }
        }
      }
    };
  }
  /**
   *  Shows a message dialog with the unpacked/overriden files.
   * 
   * @param pluginWorkspaceAccess  Entry point for accessing the DITA Maps area.
   * @param list  The relative paths of the unzipped files.
   * @throws IOException  Problems reading the files.
   */
  private void showReport(final StandalonePluginWorkspace pluginWorkspaceAccess,
      ArrayList<String> list) throws IOException {
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

      panel.add(new JLabel(resourceBundle.getMessage(Tags.SHOW_REPORT_LABEL)), gbcLabel);

      GridBagConstraints gbcScroll = new GridBagConstraints();
      gbcScroll.gridx = 0;
      gbcScroll.gridy = 1;
      gbcLabel.gridwidth = 1;
      gbcLabel.gridheight = 1;
      gbcLabel.weightx = 0;
      gbcLabel.weighty = 0;
      gbcScroll.fill = GridBagConstraints.BOTH;
      gbcScroll.anchor = GridBagConstraints.LINE_START;
      panel.add(scroll , gbcScroll);


      JOptionPane.showMessageDialog((JFrame) pluginWorkspaceAccess.getParentFrame(), panel, resourceBundle.getMessage(Tags.SHOW_REPORT_TITLE), JOptionPane.INFORMATION_MESSAGE);
    }
    else{
      throw new IOException(resourceBundle.getMessage(Tags.SHOW_REPORT_EXCEPTION_MESSAGE));
    }
  }
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  public boolean applicationClosing() {
    //You can reject the application closing here
    return true;
  }

  /**
   * Overrides the files contained in a chosen archive.
   * 
   * @param pluginWorkspaceAccess  Entry point for accessing the DITA Maps area.
   * @param frame  The parent frame component used by the Progress Dialog.
   * @param rootDir Where to unzip the archive.
   * @param chosenDir The location of the chosen package.
   */
  public void overrideTranslatedFiles(final StandalonePluginWorkspace pluginWorkspaceAccess, final JFrame frame,
      final File rootDir, final File chosenDir) {
    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();

    if(chosenDir != null) {        

      try { 
        // Unzip the chosen package over the parent  directory of the current ditamap on thread.
        final UnzipWorker unzipTask = new UnzipWorker(chosenDir, rootDir);
        // Install the progress tracker.
        ProgressDialog.install(unzipTask, frame , resourceBundle.getMessage(Tags.ACTION3_PROGRESS_DIALOG_TITLE));
        // This listener notifies the user about how the operation ended.
        unzipTask.addProgressListener(new ProgressChangeAdapter() {
          public void done() {
            try {
              showReport(pluginWorkspaceAccess, unzipTask.getUnpackedFiles());
            } catch (Exception e) {
              logger.error(e, e);
              if(!(e instanceof StoppedByUserException)){
                pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION3_ERROR_MESSAGE) + e.getMessage());
              }
              return;                  
            }
          }
          public void operationFailed(Exception ex) {
            if(!(ex instanceof StoppedByUserException)){
              pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION3_ERROR_MESSAGE) + ex.getMessage());
            }
          }
        });
        unzipTask.execute();

      } catch (Exception e) {
        // Preset error to user.
        logger.error(e, e);
        pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION3_ERROR_MESSAGE) + e.getMessage());
      }
    }
  }
  /**
   *  
   * Packs the modified files or an entire directory in the specified chosenDir. 
   *  
   * @param frame  The parent frame used by the progress dialog.
   * @param rootDir The location of the parent directory of the current ditamap.
   * @param chosenDir Where to save the archive.
   * @param resourceBundle  The message bundle used to get the translation of messages used in the plugin.
   * @param pluginWorkspace Entry point for accessing the DITA Maps area.
   * @param packAll  True if the user wants to pack the entire directory.
   * @param modifiedResources All the modified files.
   * @param shouldCreateReport  True if the user wants to create a report.
   */
  private void createPackage(final JFrame frame, 
      final File rootDir, 
      File chosenDir,
      final PluginResourceBundle resourceBundle,
      final StandalonePluginWorkspace pluginWorkspace,
      final boolean packAll,
      final ArrayList<ResourceInfo> modifiedResources,
      final boolean shouldCreateReport){

    // 1. Start the processing. (the ZIP Worker)
    // 2. Show the dialog. 
    // 3. The ZIP worker notifies the dialog.
    final ZipWorker zipTask;
    if(logger.isDebugEnabled()){
      logger.debug(resourceBundle.getMessage(Tags.CREATE_PACKAGE_LOGGER_MESSAGE1) + packAll);
    }

    if(packAll){
      if(logger.isDebugEnabled()){
        logger.debug(resourceBundle.getMessage(Tags.CREATE_PACKAGE_LOGGER_MESSAGE2));
      }
      zipTask = new ZipWorker(rootDir, chosenDir, packAll);
    } else { 
      if (logger.isDebugEnabled()) {
        logger.debug(resourceBundle.getMessage(Tags.CREATE_PACKAGE_LOGGER_MESSAGE3));
      }
      zipTask = new ZipWorker(rootDir, chosenDir, packAll, modifiedResources);
    }
    // Install the progress tracker.
    ProgressDialog.install(
        zipTask, 
        frame , 
        resourceBundle.getMessage(Tags.ACTION2_PROGRESS_DIALOG_TITLE));

    // This listener notifies the user about how the operation ended.
    zipTask.addProgressListener(new ProgressChangeAdapter() {                      
      public void done() { 
        if(packAll){
          JOptionPane.showMessageDialog(frame, resourceBundle.getMessage(Tags.ACTION2_PACK_DIR_MESSAGE),
              resourceBundle.getMessage(Tags.ACTION2_PACK_DIR_TITLE), 
              JOptionPane.INFORMATION_MESSAGE);
        } else{
          int nrOfFiles = zipTask.getModifiedFilesNumber().getNumber();
          JOptionPane.showMessageDialog(frame, resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_MESSAGE1) + 
              nrOfFiles +
              resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_MESSAGE2), 
              resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_TITLE),
              JOptionPane.INFORMATION_MESSAGE);
          if(shouldCreateReport){
            if (logger.isDebugEnabled()) {
              logger.debug(resourceBundle.getMessage(Tags.CREATE_PACKAGE_LOGGER_MESSAGE4) + shouldCreateReport);
            }
            //Open the report file           
            try {
              Desktop.getDesktop().open(new File(rootDir, ReportDialog.getReportFileName()));
            } catch (IOException e1) {
              logger.error(e1, e1);
            }
          }
        }
      }                      
      public void operationFailed(Exception ex) {  
        //Treat differently Stop by user exceptions and the custom one about nothing to pack.
        if(ex instanceof NoChangedFilesException){
          pluginWorkspace.showInformationMessage(resourceBundle.getMessage(Tags.ACTION2_INFO_MESSAGE_EXCEPTION) + "\n " + ex.getMessage());
        } else if(ex instanceof StoppedByUserException) {
          logger.error(ex, ex);
        } else {
          logger.error(ex, ex);
        }
      }
    });
    zipTask.execute();
  }

  public File locateFile(URL url) {
    String result = null;
    try {
      result = URLDecoder.decode(url.toString(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error(e, e);
    }
    return new File(result).getParentFile();
  }
}