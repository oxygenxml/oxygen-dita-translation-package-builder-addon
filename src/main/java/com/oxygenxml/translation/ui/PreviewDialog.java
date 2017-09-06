package com.oxygenxml.translation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.jidesoft.swing.CheckBoxTree;
import com.oxygenxml.translation.ui.worker.CopyDirectoryWorker;

import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;
/**
 *  The dialog that shows a preview before applying a package.
 * 
 * @author Bivolan Dalina
 *
 */
public class PreviewDialog extends OKCancelDialog {
  /**
   *  Resource bundle.
   */
  private final static PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(PreviewDialog.class); 
  /**
   *  The table that displays the relative paths of the translated files.
   */
  private JTable resourcesTable;
  /**
   *  The default tree model of the CheckBoxTree.
   */
  private DefaultTreeModel treeModel = null;
  /**
   *  Solves conflict between TableModelListener and ItemListener.
   */
  private boolean conflictFlag = false;
  /**
   *  The root node of the tree.
   */
  private DefaultMutableTreeNode root = null;
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
  private File translatedFiles;
  /**
   *  The custom table model.
   */
  private MyTableModel tableModel;
  /**
   *  The current ditamap parent file.
   */
  private File filesOnDisk;
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
   * @param filesOnDiskDir   Where to copy the unpacked files.
   * @param translatedFilesDir  Where to extract the archive. These files will be copied in rootDir.
   */
  public PreviewDialog(
      final Frame parentFrame, 
      final ArrayList<String> filePaths, 
      final File filesOnDiskDir, 
      final File translatedFilesDir) {
    super(parentFrame, resourceBundle.getMessage(Tags.ACTION3_PREVIEW_DIALOG_TITLE), false);
    this.filesOnDisk = filesOnDiskDir;
    this.translatedFiles = translatedFilesDir;

    final StandalonePluginWorkspace pluginWorkspace = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace());
    
    getOkButton().setText(resourceBundle.getMessage(Tags.APPLY_BUTTON));
    // 1. Start the processing. (the CopyDirectoryWorker)
    // 2. Show the dialog. 
    // 3. The CopyDirectoryWorker notifies the dialog.

    
    ArrayList<CheckboxTableItem> loadPaths = new ArrayList<CheckboxTableItem>();
    for (String data : filePaths) {
      loadPaths.add(new CheckboxTableItem(Boolean.TRUE , data));
    }
    tableModel = new MyTableModel(loadPaths);
   
    
    switchViewButton = new JButton(resourceBundle.getMessage(Tags.SWICH_TO_TREE_VIEW_BUTTON));

    resourcesTable = new JTable(tableModel);   
    resourcesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);     
    resourcesTable.setTableHeader(null);
    resourcesTable.setShowGrid(false);
    resourcesTable.getColumnModel().getColumn(0).setMaxWidth(40);
    resourcesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    
    resourcesTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        // Show a DIFF if the user double clicks on a file.
        if (event.getClickCount() == 2) {
          int selectedColumn = resourcesTable.getSelectedColumn();
          int selectedRow = resourcesTable.getSelectedRow();
          
          Rectangle goodCell = resourcesTable.getCellRect(selectedRow, 1, true);
          // show DIFF only if the user double clicks on the second column cells
          if (goodCell.contains(event.getPoint())) {
            String selectedPath = resourcesTable.getModel().getValueAt(selectedRow, selectedColumn).toString();

            if (logger.isDebugEnabled()) {
              logger.debug(selectedPath);
            }

            File localFile = new File(filesOnDiskDir, selectedPath);
            File translatedFile = new File(translatedFilesDir, selectedPath);
            showDiff(pluginWorkspace, localFile, translatedFile);
          }
        }
      }
   });
    
    final JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(resourcesTable);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setPreferredSize(new Dimension(500, 200));

    final JCheckBox selectAll = new JCheckBox(resourceBundle.getMessage(Tags.PREVIEW_DIALOG_CHECKBOX));
    final JPanel panel = new JPanel(new GridBagLayout());
    // Switch the users view on switchViewButton click
    switchViewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isListViewShowing = !isListViewShowing;
        
        if (isListViewShowing) { 
          scrollPane.setViewportView(resourcesTable);
          switchViewButton.setText(resourceBundle.getMessage(Tags.SWICH_TO_TREE_VIEW_BUTTON));
          selectAll.setVisible(true);
        } else {
          if(root == null) {
            createTreeView(filePaths, filesOnDiskDir, translatedFilesDir, pluginWorkspace);
          }
          
          switchViewButton.setText(resourceBundle.getMessage(Tags.SWICH_TO_LIST_VIEW_BUTTON));
          scrollPane.setViewportView(tree);
          selectAll.setVisible(false);
        }
      }
    });
   
    resourcesTable.getModel().addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        conflictFlag = true;
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column == 0) {
          MyTableModel model = (MyTableModel)e.getSource();
          Boolean checked = (Boolean) model.getValueAt(row, column);
          if (!checked) {
            selectAll.setSelected(false);
          } else {
            selectAll.setSelected(model.isEverythingSelected());
          }
        }
        conflictFlag = false;
      }
    });
    
    // By default all entries are selected.
    selectAll.setSelected(true);  
   
    selectAll.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(conflictFlag == false){
          // Select all table entries if the "Select all" checkbox is selected
          if (e.getStateChange() == ItemEvent.SELECTED) {
            for(int i = 0; i < tableModel.getRowCount(); i++){
              tableModel.setValueAt(Boolean.TRUE, i, 0);
            }
            resourcesTable.repaint();
          } else {
            // otherwise deselect them.
            for(int i = 0; i < tableModel.getRowCount(); i++){
              tableModel.setValueAt(Boolean.FALSE, i, 0);
            }
            resourcesTable.repaint();
          }
        }
      }
    });
    
    JTextArea infoText = new JTextArea();
    infoText.setText("Double click on an Oxygen supported file to see the differences.");
    infoText.setLineWrap(true);
    infoText.setWrapStyleWord(true);
    
    panel.add(selectAll, new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(infoText, new GridBagConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(scrollPane, new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(switchViewButton, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    
    getContentPane().add(panel, BorderLayout.CENTER);
    
    setMinimumSize(new Dimension(300, 200));
    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
    setVisible(true);
  }
  
  /**
   * Builds a tree from a given forward slash delimited string.
   * 
   * @param model The tree model.
   * @param data The string to build the tree from.
   */
  private void buildTreeFromString(final DefaultTreeModel model, final String data) {
    // Fetch the root node
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

    // Split the string around the delimiter
    String [] strings = data.split("/");

    // Create a node object to use for traversing down the tree as it 
    // is being created
    DefaultMutableTreeNode node = root;
    // Iterate of the string array
    for (String s: strings) {
      // Look for the index of a node at the current level that
      // has a value equal to the current string
      int index = childIndex(node, s);

      // Index less than 0, this is a new node not currently present on the tree
      if (index < 0) {
        // Add the new node
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(s);
        node.insert(newChild, node.getChildCount());
        node = newChild;
      }
      // Else, existing node, skip to the next string
      else {
        node = (DefaultMutableTreeNode) node.getChildAt(index);
      }
    }
  }
  /**
   * Returns the index of a child of a given node, provided its string value.
   * 
   * @param node The node to search its children.
   * @param childValue The value of the child to compare with.
   * @return The index.
   */
  private int childIndex(final DefaultMutableTreeNode node, final String childValue) {
    @SuppressWarnings("unchecked")
    Enumeration<DefaultMutableTreeNode> children = node.children();
    DefaultMutableTreeNode child = null;
    int index = -1;

    while (children.hasMoreElements() && index < 0) {
      child = children.nextElement();

      if (child.getUserObject() != null && childValue.equals(child.getUserObject())) {
        index = node.getIndex(child);
      }
    }

    return index;
  }
  
  /**
   * Deletes the unselected files(from the tree view option) from a specific directory.
   * 
   * @param dirPath  The directory that contains the selected files.
   * @param selectedFiles  A list with all the selected files from the tree view option.
   * @throws IOException  Problems reading the file.
   */
  private void deleteUnselectedFileFromDir(File dirPath, ArrayList<File> selectedFiles) throws IOException{
    File[] everythingInThisDir = dirPath.listFiles();
    if (everythingInThisDir != null){
      // for every file in the current directory
      for (int i = 0; i < everythingInThisDir.length; i++) {
        //if current file is a directory
        if (everythingInThisDir[i].isDirectory()){ 
          //and if the directory should be deleted
          if(shouldDelete(selectedFiles, everythingInThisDir[i])){
            try {
              // delete the directory
              FileUtils.forceDelete(everythingInThisDir[i]);
            } catch (IOException e1) {
              e1.printStackTrace();
              logger.error(e1, e1);
            }
            if (logger.isDebugEnabled()) {
              logger.debug("Deleted if dir : " + everythingInThisDir[i].getPath());
            }
          }
          // if the directory should NOT be deleted and it's not among the selected files
          else if(!selectedFiles.contains(everythingInThisDir[i])){
            // call the method recursively with the current directory
            deleteUnselectedFileFromDir(everythingInThisDir[i], selectedFiles);
          }
        } // if current file is a file and it should be deleted
          else if (everythingInThisDir[i].isFile() && shouldDelete(selectedFiles, everythingInThisDir[i])){          
            try {
              // delete the current file
              FileUtils.forceDelete(everythingInThisDir[i]);
            } catch (IOException e1) {
              e1.printStackTrace();
              logger.error(e1, e1);
            }
            if (logger.isDebugEnabled()) {
              logger.debug("Deleted if file: " + everythingInThisDir[i].getPath());
            }
          }
        }
      } 
    else{
      throw new IOException("Please select a directory.");
    }
  }
  /**
   * Checks if a file is the parent of one of the selected files and if it's one of the selected files.
   * 
   * @param selectedFiles A list with all the selected files from the tree view option.
   * @param child A file from the directory that contains the files shown in the tree view option.
   * @return True if "child" isn't among the selected files.
   */
  private boolean shouldDelete(ArrayList<File> selectedFiles, File child) {
    boolean shouldDelete = !selectedFiles.contains(child);
    if (shouldDelete && child.isDirectory()) {
      // Check in the selected files for its descendents.
      for (Iterator<File> iterator = selectedFiles.iterator(); iterator.hasNext();) {
        File file = (File) iterator.next();
        
        boolean isDescendant = file.getAbsolutePath().startsWith(child.getAbsolutePath());
        if (isDescendant) {
          shouldDelete = false;
          break;
        }
      }
    }
    return shouldDelete;
  }
  
  @Override
  protected void doOK() {
    
    applyChanges();
    
    super.doOK();
  }
  /**
   * Override the selected files from both,list and tree, views in the parent directory of the current ditamap.
   */
  private void applyChanges() {
    
    final StandalonePluginWorkspace pluginWorkspace = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace());
    // List of selected files from the tree view
    ArrayList<File> selectedTreeFiles = new ArrayList<File>();
    // List of selected files from the list view
    ArrayList<File> selectedListFiles = new ArrayList<File>();
    // List of unselected files from list view
    ArrayList<File> unSelectedListFiles = new ArrayList<File>();
    
    if(switchViewButton.getText() == resourceBundle.getMessage(Tags.SWICH_TO_LIST_VIEW_BUTTON)){
      //Get all the selected paths
        TreePath[] treePaths = tree.getCheckBoxTreeSelectionModel().getSelectionPaths();
       
        for (TreePath treePath : treePaths) {
            //Build the relative path 
             Object[] obj = treePath.getPath();
             int length = obj.length;
             String relativePath = ""; 
             if(length >= 2){
               relativePath = relativePath + obj[1].toString();
               for (int i = 2; i < length-1; i++) {
                 relativePath = relativePath + "/" + obj[i].toString();
               }
             }
             
             if (logger.isDebugEnabled()) {
               logger.debug(new File(translatedFiles.getPath(), relativePath));
             }
             File selectedFile = new File(translatedFiles.getPath(), relativePath);
             selectedTreeFiles.add(selectedFile);
        }
        if(!selectedTreeFiles.isEmpty() && !selectedTreeFiles.get(0).equals(new File(translatedFiles.getPath()))){
          try {
            deleteUnselectedFileFromDir(translatedFiles, selectedTreeFiles);
          } catch (IOException e1) { 
            e1.printStackTrace();
            logger.error(e1, e1);
          }
        }
    }
    else{
      for (int i = 0; i < tableModel.getRowCount(); i++) {
        Boolean value = (Boolean) tableModel.getValueAt(i, 0);
        if(value) {
          File selected = new File(translatedFiles.getPath(), (String)tableModel.getValueAt(i, 1));
          selectedListFiles.add(selected);
        } else {
          File unselected = new File(translatedFiles.getPath(), (String)tableModel.getValueAt(i, 1));
          unSelectedListFiles.add(unselected);
        }
      }
      if (!selectedListFiles.isEmpty()) {
        for (File unselectedFile : unSelectedListFiles) {

          if (logger.isDebugEnabled()) {
            logger.debug(unselectedFile.getAbsolutePath());
          }
          try {
            FileUtils.forceDelete(unselectedFile);
            if (logger.isDebugEnabled()) {
              logger.debug("Deleted : " + unselectedFile.getAbsolutePath());
            }
          } catch (IOException e1) {
            e1.printStackTrace();
            logger.error(e1, e1);
          }
        } 
      }
    }

    if((selectedTreeFiles.isEmpty() && switchViewButton.getText() == resourceBundle.getMessage(Tags.SWICH_TO_LIST_VIEW_BUTTON)) || 
        (selectedListFiles.isEmpty() && switchViewButton.getText() == resourceBundle.getMessage(Tags.SWICH_TO_TREE_VIEW_BUTTON))){
      pluginWorkspace.showErrorMessage(resourceBundle.getMessage(Tags.PREVIEW_DIALOG_ERROR_MESSAGE));
    } else {
      setVisible(false);
      //Copy the files on thread.
      final CopyDirectoryWorker copyDirTask = new CopyDirectoryWorker(filesOnDisk, translatedFiles);
      //Install the tracker.
      ProgressDialog.install(
          copyDirTask, 
          (JFrame) pluginWorkspace.getParentFrame(), 
          resourceBundle.getMessage(Tags.PREVIEW_DIALOG_PROGRESS_TITLE));
      // This listener notifies the user about how the operation ended.
      copyDirTask.addProgressListener(new ProgressChangeAdapter() {
        public void done() {
          pluginWorkspace.showInformationMessage(resourceBundle.getMessage(Tags.PREVIEW_DIALOG_PROGRESS_INFOMESSAGE));
          try {
            FileUtils.deleteDirectory(translatedFiles);
          } catch (IOException e) {
            logger.error(e, e);
          }
        }
        // Show an error message and delete the translatedFiles directory when the watched operation has failed.
        public void operationFailed(Exception ex) {
          logger.error(ex, ex);
          if(!(ex instanceof StoppedByUserException)){
            pluginWorkspace.showErrorMessage(resourceBundle.getMessage(Tags.PREVIEW_DIALOG_PROGRESS_ERRORMESSAGE) + ex.getMessage());
          }

          try {
            FileUtils.deleteDirectory(translatedFiles);
          } catch (IOException e) {
            logger.error(e, e);
          }
        }
      });
      copyDirTask.execute();
    }
  }
  
  @Override
  protected void doCancel() {
    super.doCancel();

    try {
      FileUtils.deleteDirectory(translatedFiles);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
  /**
   * 
   *  Open the diff files tool with initial left and right URLs to compare. 
   *  The comparison will begin automatically and the content types for the URLs will be auto-detected.
   * 
   * @param pluginWorkspace  Entry point for accessing the DITA Maps area.
   * @param localFile The location of the current file on disk.
   * @param translatedFile The location of the unpacked file. The file from the chosen archive.
   */
  private void showDiff(final StandalonePluginWorkspace pluginWorkspace, File localFile, File translatedFile) {
    try {
      URL leftURL = localFile.toURI().toURL();
      URL rightURL = translatedFile.toURI().toURL();
      
      //Check if the url it's a supported Oxygen file
      if(!pluginWorkspace.getUtilAccess().isUnhandledBinaryResourceURL(rightURL)){
        pluginWorkspace.openDiffFilesApplication(leftURL, rightURL);
      } else {
        pluginWorkspace.showInformationMessage(resourceBundle.getMessage(Tags.PREVIEW_DIALOG_SUPPORTED_OXYFILE));
      }
    } catch (MalformedURLException e2) {
      // Shouldn't happen.
      logger.error(e2, e2);
    }
  }
  /**
   * Creates the tree view.
   * 
   * @param filePaths The list with the relative paths of the unpacked files.
   * @param filesOnDiskDir The location of the current files on disk.
   * @param translatedFilesDir  The location of the unpacked files. The files from the chosen archive.
   * @param pluginWorkspace  Entry point for accessing the DITA Maps area.
   */
  private void createTreeView(
      final ArrayList<String> filePaths, 
      final File filesOnDiskDir,
      final File translatedFilesDir, 
      final StandalonePluginWorkspace pluginWorkspace) {
    // Lazy create the tree view.
    root = new DefaultMutableTreeNode(filesOnDiskDir.getName());
    treeModel = new DefaultTreeModel(root);
    tree = new CheckBoxTree(treeModel); 
  
    for (String data : filePaths) {
      buildTreeFromString(treeModel, data);
    }
    
    tree.setEditable(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    tree.setShowsRootHandles(true);
    tree.setRootVisible(true);
    tree.setRowHeight(20);
    tree.setSelectionRow(0); 
    tree.getCheckBoxTreeSelectionModel().addSelectionPath(new TreePath(root.getPath()));

    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if(tree.getLastSelectedPathComponent() != null){
          //Double click only on leafs to see the DIFF.
          if (tree.getPathBounds(tree.getSelectionPath()).contains(me.getPoint()) && 
              me.getClickCount() == 2 &&
              tree.getModel().isLeaf(tree.getLastSelectedPathComponent())) {
            //Build the selected path
            Object[] selectedPath = tree.getSelectionPath().getPath();
            int length = selectedPath.length;
            String relativePath = "";
            for (int i = 1; i < length-1; i++) {
              relativePath = relativePath + "/" + selectedPath[i].toString();
            }
            relativePath = relativePath + "/" + selectedPath[length-1];

            if (logger.isDebugEnabled()) {
              logger.debug(tree.getSelectionPath());
              logger.debug(new File(filesOnDiskDir, relativePath));
            }
            
            File localFile = new File(filesOnDiskDir, relativePath);
            File translatedFile = new File(translatedFilesDir, relativePath);
            
            showDiff(pluginWorkspace, localFile, translatedFile);
          }
        }
      }
    });
  }
}
