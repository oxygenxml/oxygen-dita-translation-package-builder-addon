package com.oxygenxml.translation.support.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.jidesoft.swing.CheckBoxTree;
import com.oxygenxml.translation.support.util.ApplyPackageUtil;
import com.oxygenxml.translation.ui.Icons;

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
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(CheckboxTreeUtil.class.getName());

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
    
    if (imageUtilities == null) {
      // If the image utilities is not mocked, an NPE will occur.
      return;
    }
    
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
   * Installs a mouse listener that opens the diff when double clicking a leaf.
   * 
   * @param tree                Current checkbox tree.
   * @param topLocationDir      The common ancestor of all the DITA resources referred in the DITA map tree. 
   *                            Either the DITA map folder or an ancestor of it.
   * @param translatedFilesDir  The location of the unpacked files.
   * @param name 
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
            File selectedPath = (File) tree.getLastSelectedPathComponent();

            try {
              URL top = translatedFilesDir.toURI().toURL();
              URL url = selectedPath.toURI().toURL();
              String relative = URLUtil.makeRelative(top, url);

              File localFile = new File(topLocationDir, relative);
              localFile = new File(URLUtil.decodeURIComponent(localFile.getAbsolutePath()));
              File translatedFile = new File(URLUtil.decodeURIComponent(selectedPath.getAbsolutePath()));

              ApplyPackageUtil.showDiff(localFile, translatedFile);
            } catch (MalformedURLException e) {
              logger.error(e, e);
            }
          }
        }
      }
    });
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
  public static List<File> processTreeFiles(CheckBoxTree tree) {

    List<File> selectedTreeFiles = new ArrayList<>();
    //Get all the checked paths
    TreePath[] treePaths = tree.getCheckBoxTreeSelectionModel().getSelectionPaths();
    
    for (TreePath treePath : treePaths) {
      Object location = treePath.getLastPathComponent();
      FileSystemTreeModel model = (FileSystemTreeModel) tree.getModel();
      File selectedFile = new File(treePath.getLastPathComponent().toString());
      
      boolean leaf = model.isLeaf(location);
      if (leaf) {
        selectedTreeFiles.add(selectedFile);
      } else {
        // Folder
        @SuppressWarnings("unchecked")
        Collection<File> allFiles = FileUtils.listFiles(selectedFile, null, true);
        selectedTreeFiles.addAll(allFiles);
      }
    }
    
    return selectedTreeFiles;
  }

  /**
   * Creates a new checkbox tree.
   * 
   * @param treeModel   Tree model.
   * @param rootLabel   Custom value to render for root element.
   * 
   * @return            A new tree.
   */
  public static CheckBoxTree createFileSystemTree(FileSystemTreeModel treeModel,final String rootLabel) {
    CheckBoxTree tree = new CheckBoxTree(treeModel)/*NOSONAR*/ {
      @Override
      public String convertValueToText(Object value, boolean selected,
          boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value == treeModel.getRoot()) {
          return rootLabel;
        }
        return ((File)value).getName();
      }
    
    }; 
  
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
    
    // Check root
    tree.getCheckBoxTreeSelectionModel().addSelectionPath(tree.getPathForRow(0));
    
    return tree;
  }
}
