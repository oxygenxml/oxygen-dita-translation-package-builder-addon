package com.oxygenxml.translation.progress.worker;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.oxygenxml.translation.progress.ProgressChangeListener;

/**
 * Creates an Abstract SwingWorker.
 * 
 * @author Bivolan Dalina
 *
 */
public abstract class AbstractWorker extends SwingWorker<Void, Void> {
  /**
   *  A listener for notifying the changes.
   */
  protected ArrayList<ProgressChangeListener> listeners;
  
  
  public AbstractWorker() {
    this.listeners = new ArrayList<ProgressChangeListener>();
  }
  
  public void addProgressListener(ProgressChangeListener l) {
    listeners.add(l);
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
    } catch (ExecutionException e) {
      for(ProgressChangeListener listener : listeners){
        listener.operationFailed((Exception) e.getCause());
      }
    } catch (Exception e) {
      for(ProgressChangeListener listener : listeners){
        listener.operationFailed(e);
      }
    }
    
  }
}
