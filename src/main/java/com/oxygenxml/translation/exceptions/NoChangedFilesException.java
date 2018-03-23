package com.oxygenxml.translation.exceptions;
/**
 * No file was changed since the last generation of a milestone file.
 * 
 * @author Bivolan Dalina
 *
 */
public class NoChangedFilesException extends Exception{
  
  public NoChangedFilesException() {  }

  public NoChangedFilesException (String message) {
      super (message);
  }

  public NoChangedFilesException (Throwable cause) {
      super (cause);
  }

  public NoChangedFilesException (String message, Throwable cause) {
      super (message, cause);
  }
}
