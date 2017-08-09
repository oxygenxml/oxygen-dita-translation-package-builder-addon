package com.oxygenxml.translation.progress;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import com.oxygenxml.translation.progress.worker.CopyDirectoryWorker;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;
/**
 *  The dialog that shows the preview before applying a package.
 * 
 * @author Bivolan Dalina
 *
 */
public class PreviewDialog extends OKCancelDialog {
  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(PreviewDialog.class); 
  /**
   *  The table that displays the relative paths of the translated files.
   */
  private JTable relativePaths;
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
   * A dialog that shows a preview of the files that are about to be applied.
   * 
   * @param parentFrame   The parent frame of the dialog.
   * @param title   The title of the dialog.
   * @param filePaths    The relative paths of all the unpacked files.
   * @param filesOnDisk   Where to copy the unpacked files.
   * @param translatedFiles  Where to extract the archive. These files will be copied in rootDir.
   */
  public PreviewDialog(final Frame parentFrame, String title, final ArrayList<String> filePaths, final File filesOnDisk, final File translatedFiles) {
    super(parentFrame, title, false);

    final StandalonePluginWorkspace plugin = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace());
    
    JButton cancel = getCancelButton();
    // Delete the translatedFiles directory if the user presses the Cancel button.
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        try {
          FileUtils.deleteDirectory(translatedFiles);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
    
    JButton apply = getOkButton();
    apply.setText("Apply");
    // 1. Start the processing. (the CopyDirectoryWorker)
    // 2. Show the dialog. 
    // 3. The CopyDirectoryWorker notifies the dialog.

    
    ArrayList<CheckboxTableItem> loadPaths = new ArrayList<CheckboxTableItem>();
    for (String data : filePaths) {
      loadPaths.add(new CheckboxTableItem(Boolean.TRUE , data));
    }
    final MyTableModel tableModel = new MyTableModel(loadPaths);
   
    
    final JButton treeButton = new JButton("Switch to Tree View");
    
    apply.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        
        if(treeButton.getText() == "Switch to List View"){
          System.out.println("TREE selection");
            TreePath[] treePaths = tree.getCheckBoxTreeSelectionModel().getSelectionPaths();
            ArrayList<File> selectedFiles = new ArrayList<File>();
            for (TreePath treePath : treePaths) {
//                 System.out.println(treePath.toString());
                 Object[] obj = treePath.getPath();
                 int length = obj.length;
                 String relativePath = "";   
                 if(length > 2){
                   relativePath = relativePath + obj[1].toString();
                 }
                 for (int i = 2; i < length-1; i++) {
                   relativePath = relativePath + "/" + obj[i].toString();
                 }
                 relativePath = relativePath + "/" + obj[length-1];
                 
//                 System.out.println(relativePath);
                 File selectedFile = new File(translatedFiles.getPath(), relativePath);
                 selectedFiles.add(selectedFile);
            }
            try {
              deleteUnselectedFileFromDir(translatedFiles, selectedFiles);
            } catch (IOException e1) { 
              e1.printStackTrace();
              logger.error(e1, e1);
            }
            
        }
        else{
          System.out.println("LIST selection");
          for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean value = (Boolean) tableModel.getValueAt(i, 0);
            if (!value) {
              File unselectedFile = new File(translatedFiles.getPath(), (String)tableModel.getValueAt(i, 1));
              
              if (logger.isDebugEnabled()) {
                logger.debug(unselectedFile.getAbsolutePath());
              }
              try {
                FileUtils.forceDelete(unselectedFile);
                System.out.println("deleted : " + unselectedFile.getAbsolutePath());
              } catch (IOException e1) {
                e1.printStackTrace();
                logger.error(e1, e1);
              }
            }
          }
        }

          setVisible(false);
          ProgressDialog dialog = new ProgressDialog(parentFrame, "Applying selected files");
          ArrayList<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
          listeners.add(dialog);
          final CopyDirectoryWorker copyDirTask = new CopyDirectoryWorker(filesOnDisk, translatedFiles, listeners);

          listeners.add(new ProgressChangeListener() {
            public boolean isCanceled() {
              return false;
            }
            public void done() {
              plugin.showInformationMessage("The translated files have been applied.");
              try {
                FileUtils.deleteDirectory(translatedFiles);
              } catch (IOException e) {
                logger.error(e, e);
              }
            }

            public void change(ProgressChangeEvent progress) { }
            // Show an error message and delete the translatedFiles directory when the watched operation has failed.
            public void operationFailed(Exception ex) {
              plugin.showErrorMessage("Couldn't apply files because of: " + ex.getMessage());

              try {
                FileUtils.deleteDirectory(translatedFiles);
              } catch (IOException e) {
                logger.error(e, e);
              }
            }
          });
          copyDirTask.execute();
      }
    });

    relativePaths = new JTable(tableModel);   
    relativePaths.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);     
    relativePaths.setTableHeader(null);
    
    //relativePaths.getColumnModel().getColumn(0).setHeaderRenderer(new CheckBoxRenderer(new MyItemListener()));
    //relativePaths.getColumnModel().getColumn(0).setCellEditor(new CheckBoxCellEditor());
    
    relativePaths.setShowGrid(false);
    relativePaths.getColumnModel().getColumn(0).setMaxWidth(40);
    relativePaths.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    
    relativePaths.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        
        if (event.getClickCount() == 2) {
          int selectedColumn = relativePaths.getSelectedColumn();
          int selectedRow = relativePaths.getSelectedRow();

          Rectangle goodCell = relativePaths.getCellRect(selectedRow, 1, true);

          if (goodCell.contains(event.getPoint())) {
            String selectedPath = relativePaths.getModel().getValueAt(selectedRow, selectedColumn).toString();

            if (logger.isDebugEnabled()) {
              logger.debug(selectedPath);
            }

            URL leftURL = null;
            try {
              leftURL = new File(filesOnDisk, selectedPath).toURI().toURL();
            } catch (MalformedURLException e2) {
              e2.printStackTrace();
            }

            URL rightURL = null;
            try {
              rightURL = new File(translatedFiles, selectedPath).toURI().toURL();
            } catch (MalformedURLException e2) {
              e2.printStackTrace();
            }
            //Check if the url it's a supported Oxygen file
            if(!plugin.getUtilAccess().isUnhandledBinaryResourceURL(rightURL)){
              plugin.openDiffFilesApplication(leftURL, rightURL);
            } else {
              plugin.showErrorMessage("The selected file is not supported by Oxygen XML Editor.");
            }
          }
        }
         
         
      }
   });
    
    
    final JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(relativePaths);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setPreferredSize(new Dimension(500, 200));

    JLabel label = new JLabel("Double click on an Oxygen supported FILE to see the diferences.");
    final JCheckBox selectAll = new JCheckBox("Select all files");
               
    
    
    final JPanel panel = new JPanel(new GridBagLayout());
    /**
     * TODO don't double click on folders
     * TODO double click only on the text area(the file name)
     */
   
    treeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(treeButton.getText().equals("Switch to List View")){
          scrollPane.setViewportView(relativePaths);
          treeButton.setText("Switch to Tree View");
          selectAll.setVisible(true);
        }else{
          if(root == null){
            root = new DefaultMutableTreeNode(filesOnDisk.getName());
            treeModel = new DefaultTreeModel(root);
            tree = new CheckBoxTree(treeModel); 
            
            for (String data : filePaths) {
              buildTreeFromString(treeModel, data);
            }
            
            tree.setEditable(false);
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            tree.setShowsRootHandles(true);
            
            
            tree.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent me) {
                
                  if (me.getClickCount() == 2) {
                    Object[] selectedPath = tree.getSelectionPath().getPath();
                    int length = selectedPath.length;
                    String relativePath = "";
                    //String rootPath = translatedFiles.getAbsolutePath();
                    for (int i = 1; i < length-1; i++) {
                      relativePath = relativePath + selectedPath[i].toString();
                    }
                    relativePath = relativePath + "/" + selectedPath[length-1];

                    if (logger.isDebugEnabled()) {
                      logger.debug(tree.getSelectionPath());
                    }

                    URL leftURL = null;
                    try {
                      leftURL = new File(filesOnDisk, relativePath).toURI().toURL();
                    } catch (MalformedURLException e2) {
                      e2.printStackTrace();
                    }

                    URL rightURL = null;
                    try {
                      rightURL = new File(translatedFiles, relativePath).toURI().toURL();
                    } catch (MalformedURLException e2) {
                      e2.printStackTrace();
                    }
                    //Check if the url it's a supported Oxygen file
                    if(!plugin.getUtilAccess().isUnhandledBinaryResourceURL(rightURL)){
                      plugin.openDiffFilesApplication(leftURL, rightURL);
                    } else {
                      plugin.showErrorMessage("The selected file is not supported by Oxygen XML Editor.");
                    }
                  }
                }
            });
          }
          treeButton.setText("Switch to List View");
          scrollPane.setViewportView(tree);
          selectAll.setVisible(false);
        }
        
      }
    });
    
    relativePaths.getModel().addTableModelListener(new TableModelListener() {
      
      public void tableChanged(TableModelEvent e) {
        conflictFlag = true;
        int row = e.getFirstRow();
        int column = e.getColumn();
        //if(column == 0 && data.equals(Boolean.FALSE)){
        //   selectAll.setSelected(false);
        //}
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
    selectAll.setSelected(true);  
    
    selectAll.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        if(conflictFlag == false){
         if (e.getStateChange() == ItemEvent.SELECTED) {
          for(int i = 0; i < tableModel.getRowCount(); i++){
            tableModel.setValueAt(Boolean.TRUE, i, 0);
          }
          relativePaths.repaint();
        } else {
          for(int i = 0; i < tableModel.getRowCount(); i++){
            tableModel.setValueAt(Boolean.FALSE, i, 0);
          }
          relativePaths.repaint();
        }
      }
      }
    });
    
    panel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(selectAll, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(scrollPane, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(treeButton, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
   
    
    getContentPane().add(panel, BorderLayout.CENTER);

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
  public static void main(String[] args) {
    File filesOnDisk = new File("C:\\Users\\intern1\\Documents\\userguide-hotfixes-19.0\\userguide-hotfixes-19.0\\DITA");
    File file = new File("");
    ArrayList<String> list = new ArrayList<String>();
    list.add("rules/descriptions/url.html");
    list.add("rules/descriptions/urls.html");
    list.add("rules/descriptions/userinput.html");
    list.add("rules/descriptions/ux-window.html");
    list.add("rules/descriptions/var.html");
    list.add("topics/track-changes-behaviour.dita");
    list.add("topics/track-changes-format.dita");
    list.add("dtopics/track-changes-limitations.dita");
    list.add("topics/transform-xquery-advanced-saxon-options.dita");
    list.add("topics/transform-xquery-sequence-view.dita");
    list.add("topics/transformation-scenario.dita");
    list.add("blogPosts/annotations.png");
    list.add("blogPosts/author-document-type-extension-sharing-custom-new-file-templates.dita");
    list.add("blogPosts/author-document-type-extension-sharing.dita");
    list.add("blogPosts/beforeCombined.png");
    list.add("blogPosts/blogNextBestDITAOT.dita");
    new PreviewDialog(new JFrame(), "TEST", list, filesOnDisk, file);
  }
  /**
   * Deletes the unselected files(from the tree view option) from a specific directory.
   * It doesn't work if you select a tree node(a directory).
   * 
   * @param dirPath  The directory that contains the selected files.
   * @param selectedFiles  A list with all the selected files from the tree view option.
   * @throws IOException  Problems reading the file.
   */
  private void deleteUnselectedFileFromDir(File dirPath, ArrayList<File> selectedFiles) throws IOException{
    File[] everythingInThisDir = dirPath.listFiles();
    if (everythingInThisDir != null){
      for (int i = 0; i < everythingInThisDir.length; i++) {
        if (everythingInThisDir[i].isDirectory()){ 
          if(shouldDelete(selectedFiles, everythingInThisDir[i])){
            try {
              FileUtils.forceDelete(everythingInThisDir[i]);
            } catch (IOException e1) {
              e1.printStackTrace();
              logger.error(e1, e1);
            }
            System.out.println("deleted if dir : " + everythingInThisDir[i].getPath());
          }
          else if(!selectedFiles.contains(everythingInThisDir[i])){
            deleteUnselectedFileFromDir(everythingInThisDir[i], selectedFiles);
          }
        }
          else if (everythingInThisDir[i].isFile() && shouldDelete(selectedFiles, everythingInThisDir[i])){          
            try {
              FileUtils.forceDelete(everythingInThisDir[i]);
            } catch (IOException e1) {
              e1.printStackTrace();
              logger.error(e1, e1);
            }
            System.out.println("deleted if file: " + everythingInThisDir[i].getPath());
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
}
