package com.oxygenxml.translation.ui.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import com.oxygenxml.translation.ui.ProgressChangeListener;

/**
 * Creates an Abstract SwingWorker.
 * @author Bivolan Dalina
 */
public abstract class AbstractWorker extends SwingWorker<Void, Void> {
  /**
   *  A listener for notifying the changes.
   */
  protected List<ProgressChangeListener> listeners;
  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(AbstractWorker.class); 
  
  /**
   * Constructor. Creates an empty list with listeners.
   */
  public AbstractWorker() {
    this.listeners = new ArrayList<ProgressChangeListener>();
  }
  
  /**
   * Adds a listener in the listeners list.
   */
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
      logger.debug("Catch execution exception in abstract worker : " + e.getMessage());
      logger.error(e, e);
      for(ProgressChangeListener listener : listeners){
        listener.operationFailed((Exception) e.getCause());
      }
    } catch (Exception e) {
      logger.debug("Catch exception in abstract worker : " + e.getMessage());
      logger.error(e, e);
      for(ProgressChangeListener listener : listeners){
        listener.operationFailed(e);
      }
    }
    
  }
}
