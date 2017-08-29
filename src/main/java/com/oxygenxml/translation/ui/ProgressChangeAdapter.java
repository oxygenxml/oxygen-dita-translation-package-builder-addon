package com.oxygenxml.translation.ui;
/**
 * An adapter class for the custom ProgressChangeListener.
 * 
 * @author Bivolan Dalina
 *
 */
public class ProgressChangeAdapter implements ProgressChangeListener {
  /**
   * Makes all the changes in the dialog as long as it receives events.
   * 
   * @param progress An object of type ProgressChangeEvent.
   */
  public void change(ProgressChangeEvent progress) {  }
  /**
   * The user canceled the dialog.
   * 
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
