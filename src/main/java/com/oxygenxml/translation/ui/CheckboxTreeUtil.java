package com.oxygenxml.translation.ui;

import com.jidesoft.swing.CheckBoxTree;
import com.oxygenxml.translation.support.util.ApplyPackageUtil;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.io.FileUtils;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.images.ImageUtilities;
import ro.sync.ui.hidpi.RetinaDetector;
import ro.sync.util.URLUtil;


/**
 * Utilities class for checkbox tree used in application.
 * 
 * @author adrian_sorop
 */
public class CheckboxTreeUtil {

  /**
   * Private constructor to avoid instantiation.
   */
  private CheckboxTreeUtil() {
    // Empty
  }

  /**
   * Install tree icons.
   * 
   * @param renderer Tree renderer. Can be <code>null</code>.
   */
  public static void installIcons(DefaultTreeCellRenderer renderer) {
    ClassLoader classLoader = PluginWorkspaceProvider.class.getClassLoader();
    ImageUtilities imageUtilities = PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities();

    // Load generic "file" icon
    Icon leafIcon = null;
    URL leafIconURL = classLoader.getResource(Icons.TEXT_ICON);
    if(leafIconURL != null) {
      leafIcon = (Icon) imageUtilities.loadIcon(leafIconURL);
    }

    // Load the "folder" icon
    Icon folderIcon = null;
    URL folderIconURL = classLoader.getResource(Icons.OPEN_DIRECTOR_ICON);
    if(folderIconURL != null) {
      folderIcon = (Icon) imageUtilities.loadIcon(folderIconURL);
    }

    // Apply them if the renderer is not null.
    if (renderer != null) {
      if (leafIcon != null) {
        renderer.setLeafIcon(leafIcon);
      }

      if (folderIcon != null) {
        renderer.setOpenIcon(folderIcon);
        renderer.setClosedIcon(folderIcon);
      }
    }
  }
  
  /**
   * Builds a tree from a given forward slash delimited string.
   * 
   * @param model The tree model.
   * @param data The string to build the tree from.
   */
  public static void buildTreeFromString(final DefaultTreeModel model, final String data) {
    // Fetch the root node
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    
    // Split the string around the delimiter
    String [] dataStrings = data.split("/");

    // Create a node object to use for traversing down the tree as it 
    // is being created
    DefaultMutableTreeNode node = root;
    // Iterate of the string array
    for (String stringForNode : dataStrings) {
      // Look for the index of a node at the current level that
      // has a value equal to the current string
      int index = childIndex(node, stringForNode);

      // Index less than 0, this is a new node not currently present on the tree
      if (index < 0) {
        stringForNode = URLUtil.decodeURIComponent(stringForNode);
        // Add the new node
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(stringForNode);
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
  private static int childIndex(final DefaultMutableTreeNode node, final String childValue) {
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
   * Installs a mouse listener that opens the diff when double clicking a leaf.
   * 
   * @param tree                Current checkbox tree.
   * @param topLocationDir      The common ancestor of all the DITA resources referred in the DITA map tree. 
   *                            Either the DITA map folder or an ancestor of it.
   * @param translatedFilesDir  The location of the unpacked files.
   */
  public static void installDiffOnMouseClick(final CheckBoxTree tree, final File topLocationDir, final File translatedFilesDir) {
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent me) {
        if(tree.getLastSelectedPathComponent() != null){
          //Double click only on leafs to see the DIFF.
          boolean doubleLeftClick = (me.getClickCount() == 2) && (me.getButton() == MouseEvent.BUTTON1);
          boolean contains = tree.getPathBounds(tree.getSelectionPath()).contains(me.getPoint());
          boolean isLeaf = tree.getModel().isLeaf(tree.getLastSelectedPathComponent());
          if (contains && doubleLeftClick && isLeaf) {
            //Build the selected path
            Object[] selectedPath = tree.getSelectionPath().getPath();
            int length = selectedPath.length;
            StringBuilder relativePath = new StringBuilder();
            for (int i = 1; i < length-1; i++) {
              relativePath.append('/').append(selectedPath[i].toString());
            }
            relativePath.append('/').append(selectedPath[length-1]);

            File localFile = new File(topLocationDir, relativePath.toString());
            File translatedFile = new File(translatedFilesDir, relativePath.toString());
            ApplyPackageUtil.showDiff(localFile, translatedFile);
          }
        }
      }
    });
  }
  
  /**
   * Creates a new checkbox tree.
   * 
   * @param treeModel   Tree model.
   * @param filePaths   The list with the relative paths of the unpacked files.
   * @return            A new tree.
   */
  public static CheckBoxTree createResourcesTree(DefaultTreeModel treeModel, final List<String> filePaths) {
    CheckBoxTree tree = new CheckBoxTree(treeModel); 
    for (String data : filePaths) {
      buildTreeFromString(treeModel, data);
    }

    // Make tree icons retina aware.
    CheckboxTreeUtil.installIcons((DefaultTreeCellRenderer) tree.getActualCellRenderer());

    tree.setEditable(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    tree.setShowsRootHandles(true);
    tree.setRootVisible(true);

    int rowHeight = 20;
    if (RetinaDetector.getInstance().isRetinaNoImplicitSupport()) {
      rowHeight *= RetinaDetector.getInstance().getScalingFactor();
    }
    
    tree.setRowHeight(rowHeight);
    tree.setSelectionRow(0); 
    TreePath rootPath = new TreePath(((DefaultMutableTreeNode)treeModel.getRoot()).getPath());
    tree.getCheckBoxTreeSelectionModel().addSelectionPath(rootPath);
    
    return tree;
  }
  
  /**
   * Checks if a file is the parent of one of the selected files and if it's one of the selected files.
   * 
   * @param selectedFiles A list with all the selected files from the tree view option.
   * @param child A file from the directory that contains the files shown in the tree view option.
   * @return True if "child" isn't among the selected files.
   */
  private static boolean shouldDelete(List<File> selectedFiles, File child) {
    boolean shouldDelete = !selectedFiles.contains(child);
    if (shouldDelete && child.isDirectory()) {
      // Check in the selected files for its descendants.
      for (Iterator<File> iterator = selectedFiles.iterator(); iterator.hasNext();) {
        File file = iterator.next();

        boolean isDescendant = file.getAbsolutePath().startsWith(child.getAbsolutePath());
        if (isDescendant) {
          shouldDelete = false;
          break;
        }
      }
    }
    return shouldDelete;
  }
  
  /**
   * Deletes the unselected files(from the tree view option) from a specific directory.
   * 
   * @param dirPath  The directory that contains the selected files.
   * @param selectedFiles  A list with all the selected files from the tree view option.
   * @throws IOException  Problems reading the file.
   */
  public static void deleteTreeUnselectedFiles(File dirPath, List<File> selectedFiles) throws IOException {
    File[] everythingInThisDir = dirPath.listFiles();
    if (everythingInThisDir != null){
      // for every file in the current directory
      for (int i = 0; i < everythingInThisDir.length; i++) {
        //if current file is a directory
        if (everythingInThisDir[i].isDirectory()){
          //and if the directory should be deleted
          if(shouldDelete(selectedFiles, everythingInThisDir[i])) {
            // delete the directory
            FileUtils.forceDelete(everythingInThisDir[i]);
          } else if (!selectedFiles.contains(everythingInThisDir[i])) {
            // if the directory should NOT be deleted and it's not among the selected files
            // call the method recursively with the current directory
            deleteTreeUnselectedFiles(everythingInThisDir[i], selectedFiles);
          }
        } else if (everythingInThisDir[i].isFile() && shouldDelete(selectedFiles, everythingInThisDir[i])){          
          // delete the current file
          FileUtils.forceDelete(everythingInThisDir[i]);
        }
      }
    } else {
      throw new IOException("Please select a directory.");
    }
  }
  
  /**
   * Process the tree selection and collect "checked" files.
   * 
   * @return Kept files from translated zip package or empty if
   * no file was checked for copy.
   */
  public static List<File> processTreeFiles(CheckBoxTree tree, File translatedFileDir) {

    List<File> selectedTreeFiles = new ArrayList<File>();
    //Get all the selected paths
    TreePath[] treePaths = tree.getCheckBoxTreeSelectionModel().getSelectionPaths();

    for (TreePath treePath : treePaths) {
      //Build the relative path 
      Object[] obj = treePath.getPath();
      int length = obj.length;
      StringBuilder relativePath = new StringBuilder(); 
      if(length >= 2){
        relativePath.append(obj[1].toString());
        for (int i = 2; i < length-1; i++) {
          relativePath.append('/').append(obj[i].toString());
        }
      }

      File selectedFile = new File(translatedFileDir.getPath(), relativePath.toString());
      selectedTreeFiles.add(selectedFile);
    }

    return selectedTreeFiles;
  }
}
