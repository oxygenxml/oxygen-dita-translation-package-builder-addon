package com.oxygenxml.translation.ui;

import javax.swing.JTextArea;

public class WrappableLabel extends JTextArea{
  
  public WrappableLabel(String message) {
    super(message);
    
    setEditable(false);
    setLineWrap(true);
    setWrapStyleWord(true);
    setHighlighter(null);
  }
}
