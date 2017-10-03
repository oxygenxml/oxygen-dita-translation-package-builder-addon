package com.oxygenxml.translation.ui;

/**
 *  An exception thrown when the user clicks on the Cancel button. 
 * 
 * @author Bivolan Dalina
 */
public class StoppedByUserException extends Exception{
  
  public StoppedByUserException () {
      super ("Operation canceled by user");
  }
}
