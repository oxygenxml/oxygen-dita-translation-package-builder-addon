package com.oxygenxml.translation.support;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

import com.oxygenxml.translation.progress.PreviewDialog;
import com.oxygenxml.translation.progress.ProgressChangeEvent;
import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.progress.ProgressDialog;
import com.oxygenxml.translation.progress.Tags;
import com.oxygenxml.translation.progress.worker.GenerateMilestoneWorker;
import com.oxygenxml.translation.progress.worker.GenerateModifiedResourcesWorker;
import com.oxygenxml.translation.progress.worker.UnzipWorker;
import com.oxygenxml.translation.progress.worker.ZipWorker;
import com.oxygenxml.translation.support.core.PackageBuilder;

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
   *  True if the user wants to pack the entire directory.
   */
  private static boolean packAll = false;
  public static boolean isPackAll() {
    return packAll;
  } 
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    //You can set or read global options.
    //The "ro.sync.exml.options.APIAccessibleOptionTags" contains all accessible keys.
    //		  pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);
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
        JMenu submenu = new JMenu(resourceBundle.getMessage(Tags.JMENU_TITLE));//resourceBundle.getMessage(Tags.JMENU_TITLE)/*"Translation Package Builder"*/);
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

          final File rootDir = new File(editorLocation.getPath()).getParentFile();

          // Generate the milestone on thread.
          GenerateMilestoneWorker milestoneWorker = new GenerateMilestoneWorker(rootDir);
          
          // Install the progress tracker.
          ProgressDialog.install(
              milestoneWorker, 
              (JFrame) pluginWorkspaceAccess.getParentFrame(), 
              resourceBundle.getMessage(Tags.ACTION1_PROGRESS_TITLE));
          
          // This listener notifies the user about how the operation ended.
          milestoneWorker.addProgressListener(new ProgressChangeListener() {
            public boolean isCanceled() {             
              return false;
            }            
            public void done() { 
              pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION1_INFO_MESSAGE) + rootDir);
            }
            public void change(ProgressChangeEvent progress) { }
            public void operationFailed(Exception ex) {
              // TODO Can't we look if the exception is a StoppedByUserException  instead of looking inside the message?
              // The message can change over time so its a fragile way to check the cancel state.
              if(!ex.getMessage().contains("You pressed the Cancel button.")){
                pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION1_ERROR_MESSAGE) + ex.getMessage());
              }
            }
          });
          
          milestoneWorker.execute();
        } catch (Exception e) {
          // Present the error to the user.
          logger.error(e, e);
          pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION1_ERROR_MESSAGE) + e.getMessage());
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

      final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();
      public void actionPerformed(ActionEvent actionevent) {

        final JFrame frame = (JFrame) pluginWorkspaceAccess.getParentFrame();
        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        URL editorLocation = editor.getEditorLocation();
        // 1. Extract the parent directory of the current map. This is the rootDir
        final File rootDir = new File(editorLocation.getPath()).getParentFile();
        
        try {
          // What to do if the milestone file doesn't exist? 
          // Inform the user and offer the possibility to pack the entire dir
          final File milestoneFile = new File(rootDir , PackageBuilder.getMilestoneFileName());           
          if(!milestoneFile.exists()){

            int buttonId = pluginWorkspaceAccess.showConfirmDialog(resourceBundle.getMessage(Tags.ACTION2_NO_MILESTONE_DIALOG_TITLE),
                resourceBundle.getMessage(Tags.ACTION2_NO_MILESTONE_DIALOG_MESSAGE) +
                rootDir.getPath() +"?", 
                new String[] {resourceBundle.getMessage(Tags.YES_BUTTON), resourceBundle.getMessage(Tags.NO_BUTTON)},
                new int[] {0, 1});
            if(buttonId == 0){     
              packAll = true;
              File chosenDirectory = pluginWorkspaceAccess.chooseFile(resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE),
                  new String[] {"zip"}, resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_DESCRIPTOR), true);
              if(chosenDirectory != null){
                createPackage(frame, rootDir, chosenDirectory, resourceBundle, pluginWorkspaceAccess);
              }             
            }else{
              return;
            }
          } else {               
            packAll = false;           

            final GenerateModifiedResourcesWorker modifiedResourcesWorker = 
                new GenerateModifiedResourcesWorker(rootDir);
            // Install the progress tracker.
            ProgressDialog.install(
                modifiedResourcesWorker, 
                frame , 
                resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_PROGRESS_TITLE));

            // This listener notifies the user about how the operation ended.
            modifiedResourcesWorker.addProgressListener(new ProgressChangeListener() {                                          
              public boolean isCanceled() {                 
                return false;
              }                
              public void done() { 
                GenerateModifiedResourcesWorker.setFromWorker(false);
                System.out.println("Founded modified files : " + modifiedResourcesWorker.getList().size());
                if(!modifiedResourcesWorker.getList().isEmpty()){  

                  File chosenDir = pluginWorkspaceAccess.chooseFile(resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE), new String[] {"zip"}, resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_DESCRIPTOR), true);
                  if(chosenDir != null){
                    createPackage(frame, rootDir, chosenDir, resourceBundle, pluginWorkspaceAccess);
                  }

                } else {           
                  pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION2_INFO_MESSAGE_EXCEPTION) + "\n " +
                      resourceBundle.getMessage(Tags.ACTION2_NO_CHANGED_FILES_EXCEPTION) + new Date(milestoneFile.lastModified()));                  

                }                  
              }                
              public void change(ProgressChangeEvent progress) { }
              public void operationFailed(Exception ex) {
                System.out.println("Operation failed generating modified resources");
                System.out.println(ex.getMessage());

                if(ex.getMessage().contains(resourceBundle.getMessage(Tags.ACTION2_NO_CHANGED_FILES_EXCEPTION))){
                  pluginWorkspaceAccess.showInformationMessage(resourceBundle.getMessage(Tags.ACTION2_INFO_MESSAGE_EXCEPTION) + "\n " + ex.getMessage());                  
                } else if(ex.getMessage().equals("You pressed the Cancel button.")) {
                  //do nothing
                } else {
                  pluginWorkspaceAccess.showInformationMessage("Failed because of : " + ex.getMessage());
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
        final File rootDir = new File(editorLocation.getFile()).getParentFile();

        final File chosenDir = pluginWorkspaceAccess.chooseFile(resourceBundle.getMessage(Tags.ACTION3_CHOOSE_FILE_TITLE), new String[] {"zip"},  null);

        // DIFF FLOW
        // 1. Ask user if he wants a preview.
        // 2. If yes, then extract to temp dir.
        // 3. Present a dialog with the list of unzipped files. Use aJList.
        // 4. When the user double clicks an entry launch the DIFF (left is LOCAL, RIGHT is FROM_PACKAGE).
        // 5. In dialog the user presses Apply to copy all.
        if(chosenDir != null){
          int buttonId = pluginWorkspaceAccess.showConfirmDialog(
              resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_TITLE), 
              resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_MESSAGE),
              new String[] {resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_BUTTON1), resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_BUTTON2), resourceBundle.getMessage(Tags.ACTION3_CONFIRM_DIALOG_BUTTON3)},
              new int[] {0, 1, 2});
          if(buttonId == 0){
            File tempFile = null;
            try {            
              tempFile = File.createTempFile("tempFile", null);
            } catch (IOException e) {
              logger.error(e, e);
              //pluginWorkspaceAccess.showErrorMessage("Failed to create temp file because of: " + e.getMessage());
            }
            if(tempFile.exists()){
              final File tempDir = new File(tempFile.getParentFile(), resourceBundle.getMessage(Tags.ACTION3_TEMPDIR_NAME));

              if (logger.isDebugEnabled()) {
                logger.debug(tempDir.getAbsolutePath());
              }

              final UnzipWorker unzipTask = new UnzipWorker(chosenDir, tempDir);
              ProgressDialog.install(
                  unzipTask, 
                  frame , 
                  resourceBundle.getMessage(Tags.ACTION3_PROGRESS_DIALOG_TITLE));

              unzipTask.addProgressListener(new ProgressChangeListener() {
                public boolean isCanceled() {             
                  return false;
                }            
                public void done() { 
                  new PreviewDialog(frame, resourceBundle.getMessage(Tags.ACTION3_PREVIEW_DIALOG_TITLE), unzipTask.getList(), rootDir, tempDir);
                }

                public void change(ProgressChangeEvent progress) { }
                public void operationFailed(Exception ex) {
                  if(!ex.getMessage().contains("You pressed the Cancel button.")){
                    pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION3_ERROR_MESSAGE) + ex.getMessage());
                  }
                }
              });
              unzipTask.execute();
            }
          }
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
        final UnzipWorker unzipTask = new UnzipWorker(chosenDir, rootDir);
        ProgressDialog.install(unzipTask, frame , resourceBundle.getMessage(Tags.ACTION3_PROGRESS_DIALOG_TITLE));

        unzipTask.addProgressListener(new ProgressChangeListener() {
          public boolean isCanceled() {
            return false;
          }

          public void done() {
            try {
              showReport(pluginWorkspaceAccess, unzipTask.getList());
            } catch (IOException e) {
              logger.error(e, e);
              if(!e.getMessage().contains("You pressed the Cancel button.")){
                pluginWorkspaceAccess.showErrorMessage(resourceBundle.getMessage(Tags.ACTION3_ERROR_MESSAGE) + e.getMessage());
              }
              return;                  
            }
          }

          public void change(ProgressChangeEvent progress) { }

          public void operationFailed(Exception ex) {
            if(!ex.getMessage().contains("You pressed the Cancel button.")){
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
   */
  private void createPackage(final JFrame frame, 
      final File rootDir, 
      File chosenDir,
      final PluginResourceBundle resourceBundle,
      final StandalonePluginWorkspace pluginWorkspace){

    // 1. Start the processing. (the ZIP Worker)
    // 2. Show the dialog. 
    // 3. The ZIP worker notifies the dialog.
    ZipWorker zipTask;
    ArrayList<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();

    System.out.println("packAll is : " + packAll);
    // TODO There is no need for the static field packAll. We can pass it all the way here and 
    // further on to the ZipWorker.
    if(packAll){
      System.out.println("zip entire dir.");
      zipTask = new ZipWorker(rootDir, chosenDir, listeners);
      
      ProgressDialog.install(
          zipTask, 
          frame , 
          resourceBundle.getMessage(Tags.ACTION2_PROGRESS_DIALOG_TITLE));
    } else { 
      // TODO Use logger.debug instead of System.out
      /**
       * 
# Root logger option
log4j.rootLogger=INFO, stdout

log4j.category.com.oxygenxml.translation.support=debug

# Direct log messages to stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

       */
      
      if (logger.isDebugEnabled()) {
        logger.debug("zip only modified files");
      }
      
      System.out.println("zip only modified files`");
      zipTask = new ZipWorker(rootDir, chosenDir, listeners);
      
      ProgressDialog.install(
          zipTask, 
          frame , 
          resourceBundle.getMessage(Tags.ACTION2_PROGRESS_DIALOG_TITLE));
    } 

    // This listener notifies the user about how the operation ended.
    listeners.add(new ProgressChangeListener() {                      
      public boolean isCanceled() {
        return false;
      }                      
      public void done() { 
        if(packAll){
          JOptionPane.showMessageDialog(frame, resourceBundle.getMessage(Tags.ACTION2_PACK_DIR_MESSAGE),
              resourceBundle.getMessage(Tags.ACTION2_PACK_DIR_TITLE), 
              JOptionPane.INFORMATION_MESSAGE);
        } else{
          int nrOfFiles = PackageBuilder.getCounter();
          JOptionPane.showMessageDialog(frame, resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_MESSAGE1) + 
              nrOfFiles +
              resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_MESSAGE2), 
              resourceBundle.getMessage(Tags.ACTION2_PACK_MODIFIED_TITLE),
              JOptionPane.INFORMATION_MESSAGE);
        }
      }                      
      public void change(ProgressChangeEvent progress) { }
      public void operationFailed(Exception ex) {  
        //Treat differently Stop by user exceptions and the custom one about nothing to pack.
        if(ex.getMessage().contains(resourceBundle.getMessage(Tags.ACTION2_NO_CHANGED_FILES_EXCEPTION))){
          pluginWorkspace.showInformationMessage(resourceBundle.getMessage(Tags.ACTION2_INFO_MESSAGE_EXCEPTION) + "\n " + ex.getMessage());
        } else if(ex.getMessage().equals("You pressed the Cancel button.")) {
          //do nothing
        } else {
          pluginWorkspace.showInformationMessage("Failed because of : " + ex.getMessage());
        }

      }
    });
    zipTask.execute();
  }      

}