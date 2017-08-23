package com.oxygenxml.translation.progress;
/**
 * An adapter class for the custom ProgressChangeListener.
 * 
 * @author Bivolan Dalina
 *
 */
public class ProgressChangeAdapter implements ProgressChangeListener {
  /**
   * @param progress An object of type ProgressChangeEvent.
   */
  public void change(ProgressChangeEvent progress) {  }
  /**
   * @return True if the user clicked on the Cancel button.
   */
  public boolean isCanceled() {
    return false;
  }
  /**
   *  Executed after the task is completed. It's used to perform different operations like closing the ProgressDialog.
   */
  public void done() {  }
  /**
   * The watched operation has failed.
   * 
   * @param ex The exception that stopped the operation.
   */
  public void operationFailed(Exception ex) {  }

}
