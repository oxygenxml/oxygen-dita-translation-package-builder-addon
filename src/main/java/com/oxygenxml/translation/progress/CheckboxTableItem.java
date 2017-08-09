package com.oxygenxml.translation.progress;
/**
 * This type of objects are stored in the JTable.
 * 
 * @author Bivolan Dalina
 *
 */
class CheckboxTableItem {
  /**
   *  The relative paths on the second column.
   */
  private String text;
  /**
   *  The checkbox on the first column.
   */
  private boolean isSelected = false;

  public CheckboxTableItem(boolean isSelected, String text){
    this.isSelected = isSelected;
    this.text = text;
  }

  public boolean isSelected() {
     return isSelected;
  }

  public void setSelected(boolean isSelected) {
     this.isSelected = isSelected;
  }

  public String toString() {
     return text;
  }
}
