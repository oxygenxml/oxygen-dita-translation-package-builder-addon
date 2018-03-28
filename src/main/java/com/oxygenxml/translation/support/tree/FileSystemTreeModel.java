package com.oxygenxml.translation.support.tree;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A tree model over the file system.
 * 
 * @author adrian_sorop
 */
public class FileSystemTreeModel implements TreeModel {
  
  /**
   * Root File.
   */
  private File root;
  
  /**
   * Constructor.
   * 
   * @param rootDirectory The top directory.
   */
  public FileSystemTreeModel(File rootDirectory) {
    root = rootDirectory;
  }
  
  /**
   * Returns root file.
   */
  @Override
  public Object getRoot() {
    return root;
  }
  
  @Override
  public Object getChild(Object parent, int index) {
    File directory = (File) parent;
    String[] children = directory.list();
    
    Arrays.sort(children, new Comparator<String>() {

      @Override
      public int compare(String o1, String o2) {
        
        return o1.compareTo(o2);
      }
    });
    
    return new File(directory, children[index]);
  }
  
  @Override
  public int getChildCount(Object parent) {
    File file = (File) parent;
    if (file.isDirectory()) {
      String[] fileList = file.list();
      if (fileList != null)
        return file.list().length;
    }
    return 0;
  }
  
  /**
   * @return <code>true</code> if current tree node is a leaf.
   */
  @Override
  public boolean isLeaf(Object node) {
    File file = (File) node;
    return file.isFile();
  }
  
  @Override
  public int getIndexOfChild(Object parent, Object child) {
    File directory = (File) parent;
    File file = (File) child;
    String[] children = directory.list();
    for (int i = 0; i < children.length; i++) {
      if (file.getName().equals(children[i])) {
        return i;
      }
    }
    return -1;
  }
  
  @Override
  public void valueForPathChanged(TreePath path, Object value) {
    // Nothing is changed in the model.
  }
  
  @Override
  public void addTreeModelListener(TreeModelListener listener) {
    // Nothing is changed in the model.
  }
  
  @Override
  public void removeTreeModelListener(TreeModelListener listener) {
    // Nothing is changed in the model.
  }
}
