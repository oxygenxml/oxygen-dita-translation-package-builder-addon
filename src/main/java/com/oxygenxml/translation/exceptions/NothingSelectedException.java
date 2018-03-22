package com.oxygenxml.translation.exceptions;

public class NothingSelectedException extends Exception{
  
  /**
   * "There are no resources marked to be copied."
   */
  public NothingSelectedException() {
    super("Nothing Selected");
  }
}
