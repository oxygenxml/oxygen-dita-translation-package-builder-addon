package com.oxygenxml.translation.support.table;
/**
 * This type of objects are stored in the JTable.
 * 
 * @author Bivolan Dalina
 */
class CheckboxTableItem {
  /**
   *  The relative path on the second column.
   */
  private String relativePath;
  /**
   *  The checkbox on the first column.
   */
  private boolean isSelected = false;

  /**
   * Constructs a new item that will be added in the checkbox table.
   * 
   * @param isSelected   <code>true</code> if the item will be selected in the table.
   * @param text          Value to present on the table.
   */
  public CheckboxTableItem(boolean isSelected, String text){
    this.isSelected = isSelected;
    this.relativePath = text;
  }

  public boolean isSelected() {
     return isSelected;
  }

  public void setSelected(boolean isSelected) {
     this.isSelected = isSelected;
  }

  public String toString() {
     return relativePath;
  }
}
