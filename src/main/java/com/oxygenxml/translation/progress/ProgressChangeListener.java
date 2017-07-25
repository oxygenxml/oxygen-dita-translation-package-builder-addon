package com.oxygenxml.translation.progress;

/**
 * A custom listener
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
   *  Closes the ProgressDialog.
   */
  void done();

}
