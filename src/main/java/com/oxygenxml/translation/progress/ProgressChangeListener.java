package com.oxygenxml.translation.progress;

/**
 * TODO make an adapter class.
 * 
 * A custom listener.
 * 
 * @author Bivolan Dalina
 *
 */
public interface ProgressChangeListener {
  /**
   * 
   * @param progress An object of type ProgressChangeEvent.
   */
  void change(ProgressChangeEvent progress);
  /**
   * 
   * @return True if the user clicked on the Cancel button.
   */
  boolean isCanceled();
  /**
   *  Executed after the task is completed. It's used to perform different operations like closing the ProgressDialog.
   */
  void done();
  
  /**
   * The watched operation has failed.
   * 
   * @param ex The exception that stopped the operation.
   */
  void operationFailed(Exception ex);

}
