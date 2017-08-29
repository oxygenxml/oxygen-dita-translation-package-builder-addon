package com.oxygenxml.translation.ui;

/**
 *  An exception thrown when the user clicks on the Cancel button. 
 * 
 * @author Bivolan Dalina
 *
 */

public class StoppedByUserException extends Exception{
  
  public StoppedByUserException() {  } 

  public StoppedByUserException (String message) {
      super (message);
  }

  public StoppedByUserException (Throwable cause) {
      super (cause);
  }

  public StoppedByUserException (String message, Throwable cause) {
      super (message, cause);
  }
}
