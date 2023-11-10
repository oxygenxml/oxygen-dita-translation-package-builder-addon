package com.oxygenxml.translation.support.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;

import com.jidesoft.swing.CheckBoxTree;
import com.oxygenxml.translation.support.tree.CheckboxTreeUtil;
import com.oxygenxml.translation.support.tree.FileSystemTreeModel;

import junit.framework.TestCase;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class CheckBoxTreeTest extends TestCase{
  
  /**
   * <p><b>Description:</b> CheckBox selection model for tree works and 
   * only checked files are collected.</p>
   * <p><b>Bug ID:</b> EXM-41307</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testTreeCkecking() throws Exception {
    
    File testDir = TestUtil.getPath("issue-9");
    
    final StandalonePluginWorkspace saPluginWorkspaceMock = Mockito.mock(StandalonePluginWorkspace.class);
    PluginWorkspaceProvider.setPluginWorkspace(saPluginWorkspaceMock);
    
    
    FileSystemTreeModel treeModel = new FileSystemTreeModel(testDir);
    String name = "DOC ROOT";
    CheckBoxTree tree = CheckboxTreeUtil.createFileSystemTree(treeModel, name);
    List<File> processTreeFiles = CheckboxTreeUtil.processTreeFiles(tree);
    
    List<String> checked = new ArrayList<>();
    for (File file : processTreeFiles) {
      String absolutePath = file.getAbsolutePath();
      if (absolutePath.startsWith(testDir.getAbsolutePath())) {
        String replace = absolutePath.replace(testDir.getAbsolutePath(), "").substring(1);
        if (!replace.equalsIgnoreCase("rootMap_translation_milestone.xml")) {
          checked.add(replace);
        }
      }
    }
    
    Collections.sort(checked, String.CASE_INSENSITIVE_ORDER);
    
    String property = System.getProperty("os.name");
    
    boolean isWindows = property.toLowerCase().startsWith("win");
    if (isWindows) {
      assertEquals("WINDOWS RUN!! ", 
          "[rootMap.ditamap, "
          + "topics\\add-terms-list.dita, "
          + "topics\\refFile.txt, "
          + "topics\\topic1.dita, "
          + "topics\\topic2.dita, "
          + "topics\\topic3.dita" +
          "]", 
          checked.toString());
    } else {
      assertEquals("LINUX RUN!! ", 
          "[rootMap.ditamap, "
          + "topics/add-terms-list.dita, "
          + "topics/refFile.txt, "
          + "topics/topic1.dita, "
          + "topics/topic2.dita, "
          + "topics/topic3.dita" +
          "]", 
          checked.toString());
    }
    
    processTreeFiles.clear();
    
    // Deselect all
    tree.getCheckBoxTreeSelectionModel().removeSelectionPath(tree.getPathForRow(0));
    processTreeFiles = CheckboxTreeUtil.processTreeFiles(tree);
    assertEquals("[]", processTreeFiles.toString());
    
    // Select a single file
    TreePath pathForRow = tree.getPathForRow(2);
    tree.expandPath(pathForRow);
    pathForRow = tree.getPathForRow(6);
    tree.getCheckBoxTreeSelectionModel().addSelectionPath(pathForRow);
    processTreeFiles = CheckboxTreeUtil.processTreeFiles(tree);
    
    processTreeFiles.sort(new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
      }
    });
    
    
    assertTrue("ONE FILE but was: " + processTreeFiles.size(), processTreeFiles.size() == 1);
    assertTrue("topic2.dita but was: " + processTreeFiles.get(0).getName(),
        processTreeFiles.get(0).getName().equals("topic2.dita"));
  }
  
  /**
   * <p><b>Description:</b> Pass a folder and a list of files. The files 
   * from the list will be kept. The rest will be deleted.</p>
   * <p><b>Bug ID:</b> EXM-41307</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testDeleteFiles() throws Exception {
    
    List<File> filesToKeep = new ArrayList<>();
    File testDir = TestUtil.getPath("delete-files");
    
    Collection<File> allFiles = FileUtils.listFiles(testDir, null, true);
    assertEquals(4, allFiles.size());
    filesToKeep.addAll(allFiles);
    
    filesToKeep.sort(new Comparator<File>() {

      @Override
      public int compare(File o1, File o2) {
        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
      }
    });
    
    // keep all except last file - topic3.dita
    File remove = filesToKeep.remove(3);
    assertTrue("REMOVE FROM LIST 'topic3.dita'", remove.getName().equals("topic3.dita"));
    
    CheckboxTreeUtil.deleteTreeUnselectedFiles(testDir, filesToKeep);
    
    allFiles.clear();
    allFiles = FileUtils.listFiles(testDir, null, true);
    assertEquals(3, allFiles.size());
    for (File file : allFiles) {
      if (file.getName().equals("topic3.dita")) {
        fail("FILE NOT DELETED FROM FILE SYSTEM");
      }
    }
  }

  
}
