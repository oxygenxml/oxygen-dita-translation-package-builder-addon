package com.oxygenxml.translation.ui;

/**
 * Creates an object used to notify the listener about the progress of the task.
 * 
 * @author Bivolan Dalina
 *
 */
public class ProgressChangeEvent {
  /**
   *  Retains the number of files that were processed.
   */
  private int counter;
  /**
   *  A message displayed in the dialog.
   */
  private String message;
  /**
   *  The number of files that are about to be processed.
   */
  private int totalFiles = -1;
  
  public int getCounter() {
    return counter;
  }
  public void setCounter(int counter) {
    this.counter = counter;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public int getTotalFiles() {
    return totalFiles;
  }
  public void setTotalFiles(int totalFiles) {
    this.totalFiles = totalFiles;
  }

  public ProgressChangeEvent(int counter, String message, int totalFiles){
    this.counter = counter;
    this.message = message;
    this.totalFiles = totalFiles;
  }
  
  public ProgressChangeEvent(int counter, String message){
    this.counter = counter;
    this.message = message;
  }
  
  public ProgressChangeEvent(String message){
    this.message = message;
  }
  
  public ProgressChangeEvent(){
    
  }
  
}
