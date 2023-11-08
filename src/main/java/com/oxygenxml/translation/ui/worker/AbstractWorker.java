package com.oxygenxml.translation.ui.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oxygenxml.translation.ui.ProgressChangeListener;

/**
 * Creates an Abstract SwingWorker.
 * @author Bivolan Dalina
 */
public abstract class AbstractWorker<T> extends SwingWorker<T, Void> {
  /**
   *  A listener for notifying the changes.
   */
  protected List<ProgressChangeListener> listeners;
  /**
   * Logger for logging.
   */
  private static Logger logger = LoggerFactory.getLogger(AbstractWorker.class); 
  
  /**
   * Constructor. Creates an empty list with listeners.
   */
  public AbstractWorker() {
    this.listeners = new ArrayList<>();
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
      logger.error(String.valueOf(e), e);
      for(ProgressChangeListener listener : listeners){
        
        
        
        listener.operationFailed(e.getCause());
      }
    } catch (Exception e) {
      logger.debug("Catch exception in abstract worker : " + e.getMessage());
      logger.error(String.valueOf(e), e);
      for(ProgressChangeListener listener : listeners){
        listener.operationFailed(e);
      }
    }
    
  }
}
