package com.oxygenxml.translation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelListener;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.swing.CheckBoxTree;
import com.oxygenxml.translation.exceptions.NothingSelectedException;
import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.table.CheckboxTableUtil;
import com.oxygenxml.translation.support.table.ResourcesTableModel;
import com.oxygenxml.translation.support.tree.CheckboxTreeUtil;
import com.oxygenxml.translation.support.tree.FileSystemTreeModel;
import com.oxygenxml.translation.ui.worker.CopyDirectoryWorker;

import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;
/**
 *  The dialog that shows a preview before applying a package.
 * 
 * @author Bivolan Dalina
 */
public class PreviewDialog extends OKCancelDialog { //NOSONAR
  
  /**
   * Plugin workspace
   */
  static final StandalonePluginWorkspace pluginWorkspace = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace());
  
  /**
   *  Resource bundle.
   */
  private static final PluginResourceBundle messages = pluginWorkspace.getResourceBundle();
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = LoggerFactory.getLogger(PreviewDialog.class.getName());

  /**
   *  The tree that displays the translated files in a set of hierarchical data.
   */
  private CheckBoxTree tree;
  
  /**
   *  The button that allows you to switch between the list view and the tree view.
   */
  private JButton switchViewButton;
  
  /**
   *  The location of the archive we want to apply over the current ditamap.
   */
  private File translatedFileDir;
  
  /**
   *  The custom table model.
   */
  private ResourcesTableModel tableModel;
  
  /**
   *  The common ancestor of all the DITA resources referred in the DITA map tree. 
   *  Either the DITA map folder or an ancestor of it.
   */
  private File topLocation;
  
  /**
   *  True if the list view preview is displayed. 
   */
  private boolean isListViewShowing = true;

  /**
   * A dialog that shows a preview of the files that are about to be applied. It compares the files from 
   * filesOnDisk with their counterparts from translatedFiles.
   * 
   * @param parentFrame   The parent frame of the dialog.
   * @param title   The title of the dialog.
   * @param filePaths    The relative paths of all the unpacked files.
   * @param topLocationDir   Where to copy the unpacked files.
   * @param translatedFilesDir  Where to extract the archive. These files will be copied in rootDir.
   */
  public PreviewDialog(
      final Frame parentFrame, 
      final List<String> filePaths, 
      final File topLocationDir, 
      final File translatedFilesDir) {
    super(parentFrame, messages.getMessage(Tags.PREVIEW), false);
    setModal(false);
    
    this.topLocation = topLocationDir;
    this.translatedFileDir = translatedFilesDir;

    switchViewButton = new JButton(messages.getMessage(Tags.SWICH_TO_TREE_VIEW));
    getOkButton().setText(messages.getMessage(Tags.APPLY_BUTTON));
    // 1. Start the processing. (the CopyDirectoryWorker)
    // 2. Show the dialog. 
    // 3. The CopyDirectoryWorker notifies the dialog.

    tableModel = CheckboxTableUtil.createTableModel(filePaths);
    final JTable resourcesTable = CheckboxTableUtil.createResourcesTable(tableModel);
    CheckboxTableUtil.installDiffOnMouseClick(resourcesTable, topLocation, translatedFileDir);
    
    final JScrollPane modifiedResourcesPanel = createModifiedResourcesPanel(resourcesTable);
    final JCheckBox selectAll = new JCheckBox(messages.getMessage(Tags.SELECT_ALL_FILES));
    
    // By default all entries are selected.
    selectAll.setSelected(true); 
    
    toggleTableTreeView(resourcesTable, modifiedResourcesPanel, selectAll);
    
    installTableItemSelectionModelListener(resourcesTable, selectAll);

    
    final JPanel panel = createPanel(modifiedResourcesPanel, selectAll, switchViewButton);
    getContentPane().add(panel, BorderLayout.CENTER);

    setMinimumSize(new Dimension(300, 200));
    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
    setVisible(true);
  }
  
  /**
   * Install selection listeners over table that enables or disables the "Select All" CheckBox.
   * 
   * @param resourcesTable  Checkbox table.
   * @param selectAll       Select all checkbox.
   */
  private void installTableItemSelectionModelListener(final JTable resourcesTable, final JCheckBox selectAll) /*NOSONAR*/ {
    
    //when <code>true</code> the "Select All" checkbox does not notify table anymore.
    final boolean[] inhibitSelectAll = new boolean[] {false};
    final ResourcesTableModel model = (ResourcesTableModel) resourcesTable.getModel();
    
    TableModelListener tableModelListener = e -> {
      inhibitSelectAll[0] = true;
      int row = e.getFirstRow();
      int column = e.getColumn();
      if (column == ResourcesTableModel.CHECK_BOX) {
        Boolean checked = (Boolean) model.getValueAt(row, column);
        if (!checked) {
          selectAll.setSelected(false);
        } else {
          selectAll.setSelected(model.isEverythingSelected());
        }
      }
      inhibitSelectAll[0] = false;
    };
    
    ItemListener itemListener = e -> {
      if(!inhibitSelectAll[0]){
        // Select all table entries if the "Select all" checkbox is selected
        if (e.getStateChange() == ItemEvent.SELECTED) {
          for(int i1 = 0; i1 < model.getRowCount(); i1++){
            model.setValueAt(Boolean.TRUE, i1, 0);
          }
        } else {
          // otherwise deselect them.
          for(int i2 = 0; i2 < model.getRowCount(); i2++){
            model.setValueAt(Boolean.FALSE, i2, 0);
          }
        }
      }
    };
    
    // Add the newly created listener.
    selectAll.addItemListener(itemListener);
    model.addTableModelListener(tableModelListener);
  }
  
  /**
   * Switches between tree view and table view.
   */
  private void toggleTableTreeView(
      final JTable resourcesTable, 
      final JScrollPane modifiedResourcesPanel, 
      final JCheckBox selectAll) {
    // Switch the users view on switchViewButton click
    switchViewButton.addActionListener(e -> {
      isListViewShowing = !isListViewShowing;

      if (isListViewShowing) { 
        modifiedResourcesPanel.setViewportView(resourcesTable);
        switchViewButton.setText(messages.getMessage(Tags.SWICH_TO_TREE_VIEW));
        selectAll.setVisible(true);
      } else {
        if(tree == null) {
          tree = createTreeView(topLocation, translatedFileDir);
        }

        switchViewButton.setText(messages.getMessage(Tags.SWICH_TO_LIST_VIEW));
        modifiedResourcesPanel.setViewportView(tree);
        selectAll.setVisible(false);
      }
    });
  }

  /**
   * 
   * @param resourcesTable
   * @return
   */
  private JScrollPane createModifiedResourcesPanel(final JTable resourcesTable) {
    final JScrollPane modifiedResourcesPanelHolder = new JScrollPane();
    modifiedResourcesPanelHolder.setViewportView(resourcesTable);
    modifiedResourcesPanelHolder.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    modifiedResourcesPanelHolder.setPreferredSize(new Dimension(500, 200));
    return modifiedResourcesPanelHolder;
  }

  private JPanel createPanel(final JScrollPane scrollPane, final JCheckBox selectAll, JButton switchViewButton) {
    final JPanel panel = new JPanel(new GridBagLayout());
    Insets insets = new Insets(2, 2, 2, 2);
    
    JTextArea infoText = new JTextArea();
    infoText.setText("Double click on an Oxygen supported file to see the differences.");
    infoText.setLineWrap(true);
    infoText.setWrapStyleWord(true);
    
    panel.add(selectAll, new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 1, 1));
    panel.add(infoText, new GridBagConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 1, 1));
    panel.add(scrollPane, new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 1, 1));
    panel.add(switchViewButton, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 1, 1));
    return panel;
  }

  @Override
  protected void doOK() {
    try {
      applyChanges();
      super.doOK();
    } catch (NothingSelectedException e) {
      pluginWorkspace.showErrorMessage(messages.getMessage(Tags.NO_SELECTED_FILES_TO_APPLY));
    }
  }
  /**
   * Override the selected files from both,list and tree, views in the parent directory of the current ditamap.
   * @throws Exception 
   */
  private void applyChanges() throws NothingSelectedException {
    List<File> filesToCopy = null;
    if(!isListViewShowing) {
      // Collect "checked" files from tree view.
      filesToCopy = CheckboxTreeUtil.processTreeFiles(tree);
      if (!filesToCopy.isEmpty()) {
        try {
          CheckboxTreeUtil.deleteTreeUnselectedFiles(translatedFileDir, filesToCopy);
        } catch (IOException e1) { 
          logger.error(String.valueOf(e1), e1);
        }
      }
    } else {
      filesToCopy = CheckboxTableUtil.processTableFiles(tableModel, translatedFileDir);
      if (!filesToCopy.isEmpty()) {
        CheckboxTableUtil.deleteTableUnselectedFiles(tableModel, translatedFileDir); 
      }
    }

    if (!filesToCopy.isEmpty()) {
      copyTranslatedFiles();
    } else {
      throw new NothingSelectedException();
    }
  }
  
  /**
   * Starts a new thread that copies the files. 
   */
  private void copyTranslatedFiles() {
    //Copy the files on thread.
    final CopyDirectoryWorker copyDirTask = new CopyDirectoryWorker(topLocation, translatedFileDir);
    //Install the tracker.
    ProgressDialog.install(
        copyDirTask, 
        (JFrame) pluginWorkspace.getParentFrame(), 
        messages.getMessage(Tags.APPLYING_SELECTED_FILES));
    // This listener notifies the user about how the operation ended.
    copyDirTask.addProgressListener(
        new ProgressChangeAdapter() {
          @Override
          public void done() {
            pluginWorkspace.showInformationMessage(messages.getMessage(Tags.TRANSLATED_FILES_APPLIED));
            try {
              FileUtils.deleteDirectory(translatedFileDir);
            } catch (IOException e) {
              logger.error(String.valueOf(e), e);
            }
          }
          // Show an error message and delete the translatedFiles directory when the watched operation has failed.
          @Override
          public void operationFailed(Throwable ex) {
            logger.error(String.valueOf(ex), ex);
            if(!(ex instanceof StoppedByUserException)){
              pluginWorkspace.showErrorMessage(messages.getMessage(Tags.COPY_TRANSLATED_FILES_ERROR_MESSAGE) + ex.getMessage());
            }
            try {
              FileUtils.deleteDirectory(translatedFileDir);
            } catch (IOException e) {
              logger.error(String.valueOf(e), e);
            }
          }
        });
    copyDirTask.execute();
  }

  /**
   * Cancel was pressed.
   */
  @Override
  protected void doCancel() {
    super.doCancel();
    try {
      FileUtils.deleteDirectory(translatedFileDir);
    } catch (IOException e1) {
      logger.warn(String.valueOf(e1), e1);
    }
  }

  /**
   * Creates the tree view.
   * 
   * @param filePaths       The list with the relative paths of the unpacked files.
   * @param topLocationDir  The common ancestor of all the DITA resources referred in the DITA map tree. 
   *                        Either the DITA map folder or an ancestor of it.
   * @param translatedFilesDir  The location of the unpacked files.
   * @param pluginWorkspace  Entry point for accessing the DITA Maps area.
   */
  private CheckBoxTree createTreeView(
      final File topLocationDir,
      final File translatedFilesDir) {
    // Lazy create the tree view.
    FileSystemTreeModel treeModel = new FileSystemTreeModel(translatedFilesDir);
    CheckBoxTree cbTree = CheckboxTreeUtil.createFileSystemTree(treeModel, topLocationDir.getName());
    CheckboxTreeUtil.installDiffOnMouseClick(cbTree, topLocationDir, translatedFilesDir);
    
    return cbTree;
  }

}
