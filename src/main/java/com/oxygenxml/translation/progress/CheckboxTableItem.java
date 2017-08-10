package com.oxygenxml.translation.progress;
/**
 * This type of objects are stored in the JTable.
 * 
 * @author Bivolan Dalina
 *
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
