package com.oxygenxml.translation.cmd;

import java.io.PrintStream;

import com.oxygenxml.translation.ui.ProgressChangeEvent;
import com.oxygenxml.translation.ui.ProgressChangeListener;

/**
 * Delegates events to the an output stream.
 * 
 * @author alex_jitianu
 */
public class OutputStreamProgressChangeListener implements ProgressChangeListener {
  /**
   * Stream where to output data.
   */
  private PrintStream stream;
  
  /**
   * Constructor.
   * 
   * @param stream Stream where to output data.
   */
  public OutputStreamProgressChangeListener(PrintStream stream) {
    this.stream = stream;
  }

  @Override
  public void change(ProgressChangeEvent progress) {
    stream.print(progress.getMessage());
    
    if (progress.getCounter() % 10 == 0) {
      int percent = progress.getCounter() * 100 / progress.getTotalFiles();
      stream.print(" ");
      stream.print(percent);
      stream.println("%");
    }
  }

  @Override
  public boolean isCanceled() {
    return false;
  }

  @Override
  public void done() {
    stream.println("Operation done");
  }

  @Override
  public void operationFailed(Exception ex) {
    ex.printStackTrace(stream);
  }

}
