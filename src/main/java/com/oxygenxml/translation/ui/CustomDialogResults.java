package com.oxygenxml.translation.ui;

/**
 * A class with custom result values returned by dialogs. 
 * 
 * @author adrian_sorop
 */
public class CustomDialogResults {
  
  /**
   * Avoid instantiation.
   */
  private CustomDialogResults() {
    // Nothing
  }
  
  /** 
   * Return value from class method if YES is chosen. 
   */
  public static final int         YES_OPTION = 0;
  
  /** 
   * Return value form class method if OK is chosen. 
   */
  public static final int         OK_OPTION = 0;
  
  /** 
   * Return value from class method if NO is chosen.
   */
  public static final int         NO_OPTION = 1;
  
  /** 
   * Return value from class method if CANCEL is chosen. 
   */
  public static final int         CANCEL_OPTION = 2;
  
  /**
   * Pack all
   */
  public static final int         PACK_ALL_OPTION = 3;
  
  /**
   * APPLY ALL
   */
  public static final int         APPLY_ALL_OPTION = 3;
  
  /**
   * PREVIEW (equivalent to YES and OK)
   */
  public static final int         PREVIEW_OPTION = 0;
}
