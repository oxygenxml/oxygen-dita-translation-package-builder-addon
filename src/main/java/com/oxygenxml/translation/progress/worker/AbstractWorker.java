package com.oxygenxml.translation.progress.worker;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import com.oxygenxml.translation.progress.ProgressChangeListener;

/**
 * Creates a SwingWorker for packing a directory.
 * 
 * @author Bivolan Dalina
 *
 */
public abstract class AbstractWorker extends SwingWorker<Void, Void> {
  /**
   *  A listener for notifying the changes.
   */
  protected ArrayList<ProgressChangeListener> listeners;
  
  
  public AbstractWorker( ArrayList<ProgressChangeListener> listeners) {
    this.listeners = listeners;
  }

  /**
   * Executed in event dispatching thread
   */
  @Override
  public void done() {
    try {
      // The processing has ended. Check if it ended with exception.
      get();
      // The operation finished without exceptions.
      for(ProgressChangeListener listener : listeners){
        listener.done();
      }
    } catch (Exception e) {
      for(ProgressChangeListener listener : listeners){
        listener.operationFailed(e);
      }
    }
    
  }
}
