package com.oxygenxml.translation.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
/**
 *  A custom Table Model.
 *  
 * @author Bivolan Dalina
 */
public class ResourcesTableModel extends AbstractTableModel {
  
  /**
   * Column index of checkboxes
   */
  public static final int CHECK_BOX = 0;
  
  /**
   * Column index of the paths added to the table.
   */
  public static final int RELATIVE_PATH = 1;
  
  /**
   * The list with all the table content.
   */
  private transient List<CheckboxTableItem> loadPaths;
  
  /**
   * The list with all the columns names.
   */
  private transient List<String> colNames = new ArrayList<String>();
  
  /**
   * Constructor.
   * @param loadPaths
   */
  public ResourcesTableModel(List<CheckboxTableItem> loadPaths) {
    this.loadPaths = loadPaths;
  
    colNames.add("File status");
    colNames.add("File relative path");
  }
  
  /**
   * Returns the number of rows.
   */
  public int getRowCount() {
    return loadPaths.size();
  }
  /**
   * Returns the number of columns.
   */
  public int getColumnCount() {
    return colNames.size();
  }

  /**
   * Returns the name of the specified column index.
   */
  @Override
  public String getColumnName(int column) {
    return colNames.get(column);
  }

  /**
   * Adds a row with the specified content.
   * 
   * @param path A CheckboxListItem object.
   */
  public void addRow(CheckboxTableItem path) {
    loadPaths.add(path);
    fireTableDataChanged();
  }

  /**
   * Deletes the specified row index from the table.
   * 
   * @param modelIndex The row you want to delete.
   */
  public void removeRow(int modelIndex) {
    loadPaths.remove(modelIndex);
    fireTableDataChanged();
  }

  /**
   * The user can not edit the content of the second column.
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == CHECK_BOX;
  }

  /**
   * Gets the values stored at the specified row and column in the table.
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    CheckboxTableItem item = loadPaths.get(rowIndex);
    if(columnIndex == CHECK_BOX){
      return item.isSelected();
    }else{
      return item.toString();
    }
    
  }
  /**
   *  Sets a specified value at a specified row and column in the table.
   */
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    CheckboxTableItem item = loadPaths.get(rowIndex);
    if(columnIndex == CHECK_BOX){
      item.setSelected((Boolean)aValue);
    }
    
    fireTableCellUpdated(rowIndex, columnIndex);
  }
  /**
   *  Sets a type for every column.
   */
  @Override
  public Class<?> getColumnClass(int columnIndex) {
      Class<?> type = String.class;
      
      if(columnIndex == CHECK_BOX) {
        type = Boolean.class;
      } 
      return type;
  }
  
  /**
   *  Checks if all the checkboxes in the table are selected.
   *  
   * @return True if all the checkboxes on the first column are selected, false otherwise.
   */
  public boolean isEverythingSelected(){
    for(int i = 0; i < loadPaths.size(); i++){
      CheckboxTableItem item = loadPaths.get(i);
      if(item.isSelected() == Boolean.FALSE){
        return false;
      }
    }
    return true;
  }
  /**
   *   Checks if all the checkboxes in the table are deselected.
   * 
   * @return  True if all the checkboxes on the first column are deselected, false otherwise.
   */
  public boolean isEverythingDeselected(){
    for(int i = 0; i < loadPaths.size(); i++){
      CheckboxTableItem item = loadPaths.get(i);
      if(item.isSelected() == Boolean.TRUE){
        return false;
      }
    }
    return true;
  }
  
}

