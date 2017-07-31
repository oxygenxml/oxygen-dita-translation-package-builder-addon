package com.oxygenxml.translation.support;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

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
import com.oxygenxml.translation.progress.worker.ModifiedFilesWorker;
import com.oxygenxml.translation.progress.worker.UnzipWorker;
import com.oxygenxml.translation.progress.worker.ZipWorker;
import com.oxygenxml.translation.support.core.PackageBuilder;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.ditamap.WSDITAMapEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.actions.MenusAndToolbarsContributorCustomizer;

/**
 * Plugin extension - workspace access extension.
 */
public class TranslationPackageBuilderExtension implements WorkspaceAccessPluginExtension {
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
    //		  pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);
    // Check In action

    //You can access the submenu Translation Package Builder only from the DITA Maps area.
    // The submenu contains these 3 actions.
    final Action generateMilestoneAction = createMilestoneAction(pluginWorkspaceAccess);
    final Action generateChangedFilesZipAction = createChangedFilesZipAction(pluginWorkspaceAccess);
    final Action applyTranslatedFilesAction = createApplyTranslatedFilesAction(pluginWorkspaceAccess);

    //Mount the action on the contextual menus for the Text and Author modes.
    pluginWorkspaceAccess.addMenusAndToolbarsContributorCustomizer(new MenusAndToolbarsContributorCustomizer() {

      @Override
      public void customizeDITAMapPopUpMenu(JPopupMenu popUp, WSDITAMapEditorPage ditaMapEditorPage) {
        //Create a submenu "Translation Package Builder" for the 3 actions.
        // Tooltips for all actions.
        JMenu submenu = new JMenu("Translation Package Builder");
        submenu.setMnemonic(KeyEvent.VK_S);

        // Action 1: Generate Milestone
        JMenuItem menuItemMilestone = new JMenuItem("Generate Milestone");
        menuItemMilestone.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_2, ActionEvent.ALT_MASK));
        menuItemMilestone.addActionListener(generateMilestoneAction);
        menuItemMilestone.setToolTipText("Generates a predefined file called \"milestone.xml\" in the root directory of the selected"
            + " ditamap which contains an unique hash and the relative path of every file in that directory.");

        // Action 2: Create Changed Files Package
        JMenuItem menuItemPakage = new JMenuItem("Create Modified Files Package");
        menuItemPakage.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_3, ActionEvent.ALT_MASK));
        menuItemPakage.addActionListener(generateChangedFilesZipAction);
        menuItemPakage.setToolTipText("Creates a package with all the files that were modified (since the last generation of a milestone.xml file) at a chosen location.");

        // Action 3: Unzip package that came from translation.
        JMenuItem menuItemApply = new JMenuItem("Apply Package");
        menuItemApply.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_4, ActionEvent.ALT_MASK));
        menuItemApply.addActionListener(applyTranslatedFilesAction);
        menuItemApply.setToolTipText("Applies a chosen archive over the root directory of the current ditamap.");

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

      public void actionPerformed(ActionEvent actionevent) {

        // 1. Extract the parent directory of the current map.
        // 2. Generate the milestone file in the dir

        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);

        try {
          URL editorLocation = editor.getEditorLocation();

          File rootDir = new File(editorLocation.getPath()).getParentFile();

          PackageBuilder.generateChangeMilestone(rootDir);

          pluginWorkspaceAccess.showInformationMessage("Milestone created at: " + rootDir);
        } catch (Exception e) {
          // Present the error to the user.
          logger.error(e, e);
          pluginWorkspaceAccess.showErrorMessage("Milestone creation failed because of: " + e.getMessage());
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
        final JFrame frame = (JFrame) pluginWorkspaceAccess.getParentFrame();
        WSEditor editor = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.DITA_MAPS_EDITING_AREA);
        URL editorLocation = editor.getEditorLocation();
        // 1. Extract the parent directory of the current map. This is the rootDir
        final File rootDir = new File(editorLocation.getPath()).getParentFile();

        final File chosenDir = pluginWorkspaceAccess.chooseFile("Package location", new String[] {"zip"}, "Zip files", true);

        if(chosenDir != null){              
         
          try {
            // What to do if the milestone file doesn't exist? 
            // Inform the user and offer the possibility to pack the entire dir
            File milestoneFile = new File(rootDir , PackageBuilder.getMilestoneFileName());
            if(!milestoneFile.exists()){

              int buttonId = pluginWorkspaceAccess.showConfirmDialog("Didn't find \"milestone.xml\"", "The milestone file doesn't exist."
                  + " Do you want to pack the entire directory?", new String[] {"Yes", "No"}, new int[] {0, 1});

              if(buttonId == 0){
                final File chosenDirectory = pluginWorkspaceAccess.chooseFile("Package location", new String[] {"zip"}, "Zip files", true);


                // 1. Start the processing. (the ZIP Worker)
                // 2. Show the dialog. 
                // 3. The ZIP worker notifies the dialog.
               
                // The ProgressDialog is a ProgressChangeListener
                final ProgressDialog dialog = new ProgressDialog(frame , "Zipping directory");

                ArrayList<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
                listeners.add(dialog);
                
                final ZipWorker zipTask = new ZipWorker(rootDir, chosenDirectory, listeners);
                
                listeners.add(new ProgressChangeListener() {                      
                  public boolean isCanceled() {
                    return false;
                  }                      
                  public void done() {
                    JOptionPane.showMessageDialog(frame, "The directory was packed.", "Applied files", JOptionPane.INFORMATION_MESSAGE);
                  }                      
                  public void change(ProgressChangeEvent progress) { }
                  public void operationFailed(Exception ex) {
                    pluginWorkspaceAccess.showErrorMessage("Package creation failed because of: " + ex.getMessage());
                  }
                });

                zipTask.execute();
              }

            } else{
              /**
               * TODO only one worker for zipWorker and ModifiedFilesWorker
               */
              
              // 1. Start the processing. (the ModifiedFiles Worker)
              // 2. Show the dialog. 
              // 3. The ModifiedFilesWorker notifies the dialog.
             
              // The ProgressDialog is a ProgressChangeListener
              final ProgressDialog dialog = new ProgressDialog(frame , "Pack modified files");
              ArrayList<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
              listeners.add(dialog);
              
              final ModifiedFilesWorker packModifiedFilesTask = new ModifiedFilesWorker(rootDir, chosenDir, listeners);
              
              listeners.add(new ProgressChangeListener() {                      
                public boolean isCanceled() {
                  return false;
                }                      
                public void done() {
                  JOptionPane.showMessageDialog(frame, "The modified files were packed.", "Applied files", JOptionPane.INFORMATION_MESSAGE);
                }                      
                public void change(ProgressChangeEvent progress) { }
                public void operationFailed(Exception ex) {
                  pluginWorkspaceAccess.showErrorMessage("Package creation failed because of: " + ex.getMessage());
                }
              });
          
               packModifiedFilesTask.execute();
            }
          } catch (Exception e) {
            //  Preset error to user.
            logger.error(e, e);
            pluginWorkspaceAccess.showErrorMessage("Package creation failed because of: " + e.getMessage());
          }
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

        final File chosenDir = pluginWorkspaceAccess.chooseFile("Choose the translated package", new String[] {"zip"},  null);
        
        // DIFF FLOW
        // 1. Ask user if he wants a preview.
        // 2. If yes, then extract to temp dir.
        // 3. Present a dialog with the list of unzipped files. Use aJList.
        // 4. When the user double clicks an entry launch the DIFF (left is LOCAL, RIGHT is FROM_PACKAGE).
        // 5. In dialog the user presses Apply to copy all.
        if(chosenDir != null){
          int buttonId = pluginWorkspaceAccess.showConfirmDialog("Show preview", "Do you want to see a preview? ",
                                                      new String[] {"Yes", "No"}, new int[] {0, 1});
          if(buttonId == 0){
            File tempFile = null;
            try {            
              tempFile = File.createTempFile("tempFile", null);
            } catch (IOException e) {
              logger.error(e, e);
              pluginWorkspaceAccess.showErrorMessage("Failed to create temp file because of: " + e.getMessage());
            }
            final File tempDir = new File(tempFile.getParentFile(), "TranslatedPackage");

            System.out.println(tempDir.getAbsolutePath());

            final ProgressDialog dialog = new ProgressDialog(frame , "Unzipping package");
            ArrayList<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
            listeners.add(dialog);

            final UnzipWorker unzipTask = new UnzipWorker(chosenDir, tempDir, listeners);

            listeners.add(new ProgressChangeListener() {
              public boolean isCanceled() {             
                return false;
              }            
              public void done() { 
                new PreviewDialog(frame, "Show preview", unzipTask.getList(), rootDir, tempDir);
              }

              public void change(ProgressChangeEvent progress) { }
              public void operationFailed(Exception ex) {
                pluginWorkspaceAccess.showErrorMessage("Failed to apply package because of: " + ex.getMessage());
              }
            });
            unzipTask.execute();
          }
          else{
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

      panel.add(new JLabel("Package applied over the current map. The overridden files are : "), gbcLabel);

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


      JOptionPane.showMessageDialog((JFrame) pluginWorkspaceAccess.getParentFrame(), panel, "Applied files", JOptionPane.INFORMATION_MESSAGE);
    }
    else{
      throw new IOException("The list containing the unzipped files is empty or null.");
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
    if(chosenDir != null) {        

      try {                      

        final ProgressDialog dialog = new ProgressDialog(frame , "Unzipping package");

        ArrayList<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
        listeners.add(dialog);

        final UnzipWorker unzipTask = new UnzipWorker(chosenDir, rootDir, listeners);

        listeners.add(new ProgressChangeListener() {

          public boolean isCanceled() {
            return false;
          }

          public void done() {
            try {
              showReport(pluginWorkspaceAccess, unzipTask.getList());
            } catch (IOException e) {
              logger.error(e, e);
              pluginWorkspaceAccess.showErrorMessage("Failed to apply package because of: " + e.getMessage());
              return;                  
            }
          }

          public void change(ProgressChangeEvent progress) { }

          public void operationFailed(Exception ex) {
            pluginWorkspaceAccess.showErrorMessage("Failed to apply package because of: " + ex.getMessage());
          }
        });
        unzipTask.execute();

      } catch (Exception e) {
        // Preset error to user.
        logger.error(e, e);
        pluginWorkspaceAccess.showErrorMessage("Failed to apply package because of: " + e.getMessage());
      }
    }
  }
}