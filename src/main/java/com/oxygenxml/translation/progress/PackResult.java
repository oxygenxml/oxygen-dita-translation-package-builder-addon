package com.oxygenxml.translation.progress;
/**
 * An object for holding the number of modified files.
 * 
 * @author Bivolan Dalina
 *
 */
public class PackResult {
  /**
   * How many file were modified.
   */
  private int modifiedFilesNumber;
  
  public PackResult(){
  }

  public void setModifiedFilesNumber(int number){
    this.modifiedFilesNumber = number;
  }
  
  public int getNumber(){
    return modifiedFilesNumber;
  }

}
