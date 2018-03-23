package com.oxygenxml.translation.ui;

import com.oxygenxml.translation.support.util.ApplyPackageUtil;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import ro.sync.util.URLUtil;

/**
 * Utility class for checkbox tables.
 * 
 * @author adrian_sorop
 */
public class CheckboxTableUtil {
  
  /**
   * Private constructor to avoid instantiation.
   */
  private CheckboxTableUtil() {
    // Nope
  }
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(CheckboxTableUtil.class.getName());
  
  /**
   * Create a new table. Try to load API table. If fail, use a normal Java Table.
   */
  public static JTable createResourcesTable(ResourcesTableModel model) {
    JTable resourcesTable = null;
    try {
      Class<?> tableClass = CheckboxTableUtil.class.getClassLoader().loadClass("ro.sync.exml.workspace.api.standalone.ui.Table");
      resourcesTable = (JTable) tableClass.newInstance();
    } catch (Exception e) {
      logger.debug(e, e);
    }
    if (resourcesTable == null) {
      resourcesTable = new JTable();  
    }
    
    if (model != null) {
      resourcesTable.setModel(model);
    }
    
    resourcesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);     
    resourcesTable.setTableHeader(null);
    resourcesTable.setShowGrid(false);
    // TODO setMaxWidth(calculateMaxLength)
    resourcesTable.getColumnModel().getColumn(0).setMaxWidth(40);
    resourcesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    return resourcesTable;
  } 
  
  /**
   * @return  The table model with all resources selected.
   */
  public static ResourcesTableModel createTableModel(final List<String> filePaths) {
    
    List<CheckboxTableItem> loadPaths = new ArrayList<>();
    for (String data : filePaths) {
      data = URLUtil.decodeURIComponent(data);
      loadPaths.add(new CheckboxTableItem(Boolean.TRUE , data));
    }
    return new ResourcesTableModel(loadPaths);
  }
  
  /**
   * Adds a mouse listener in table and performs a difference between the zip selected file and 
   * it's correspondent in file system.
   * 
   * @param resourcesTable The table with modified resources.
   */
  public static void installDiffOnMouseClick(final JTable resourcesTable, final File originalDir, final File tratranslatedDir) {
    resourcesTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        // Show a DIFF if the user double clicks on a file.
        if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
          int selectedColumn = resourcesTable.getSelectedColumn();
          int selectedRow = resourcesTable.getSelectedRow();

          Rectangle goodCell = resourcesTable.getCellRect(selectedRow, 1, true);
          // show DIFF only if the user double clicks on the second column cells
          if (goodCell.contains(event.getPoint())) {
            String selectedPath = resourcesTable.getModel().getValueAt(selectedRow, selectedColumn).toString();
            File localFile = new File(originalDir, selectedPath);
            File translatedFile = new File(tratranslatedDir, selectedPath);
            ApplyPackageUtil.showDiff(localFile, translatedFile);
          }
        }
      }
    });
  }
  
  /**
   * Process the table selection and collect kept files and delete the others.
   * 
   * @return Kept files from translated zip package.
   */
  public static List<File> processTableFiles(ResourcesTableModel model, File translatedFileDir) {
    
    List<File> selectedFiles = new ArrayList<File>();
    
    for (int i = 0; i < model.getRowCount(); i++) {
      Boolean checked = (Boolean) model.getValueAt(i, ResourcesTableModel.CHECK_BOX);
      if(checked) {
        File selected = new File(translatedFileDir.getPath(), (String)model.getValueAt(i, ResourcesTableModel.RELATIVE_PATH));
        selectedFiles.add(selected);
      }
    }
    
    return selectedFiles;
  }
  
  /**
   * Removed the files that user does not want to copy.
   */
  public static void deleteTableUnselectedFiles(ResourcesTableModel tableModel, File translatedFileDir) {
    if (tableModel == null || translatedFileDir == null) {
      return;
    }
    
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      Boolean checked = (Boolean) tableModel.getValueAt(i, ResourcesTableModel.CHECK_BOX);
      if(!checked) {
        File unselected = new File(translatedFileDir.getPath(), (String)tableModel.getValueAt(i, ResourcesTableModel.RELATIVE_PATH));
        try {
          FileUtils.forceDelete(unselected);
        } catch (IOException e1) {
          logger.error(e1, e1);
        }
      }
    }
  }
  
}
