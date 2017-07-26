package com.oxygenxml.translation.progress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import com.oxygenxml.translation.support.util.ArchiveBuilder;

/**
 * Creates a SwingWorker for packing a directory.
 * 
 * @author Bivolan Dalina
 *
 */
public class ZipWorker extends SwingWorker<Void, Void> {
  /**
   *  The file we want to zip.
   */
  private File rootDir;
  /**
   *  Where to put the created package.
   */
  private File zipDir;
  /**
   *  A listener for notifying the changes.
   */
  private ArrayList<ProgressChangeListener> listeners;
  
  
  public ZipWorker(File rootDir, File zipDir, ArrayList<ProgressChangeListener> listeners) {
    this.rootDir = rootDir;
    this.zipDir = zipDir;
    this.listeners = listeners;
  }

  /**
   * Main task. Executed in background thread.
   */
  @Override
  public Void doInBackground() throws IOException, StoppedByUserException {
    ArchiveBuilder archiveBuilder = new ArchiveBuilder();
    for (ProgressChangeListener l : listeners) {
      archiveBuilder.addListener(l);
    }

    archiveBuilder.zipDirectory(rootDir, zipDir);


    return null;
  }

  /**
   * Executed in event dispatching thread
   */
  @Override
  public void done() {
    for(ProgressChangeListener listener : listeners){
      listener.done();
    }
  }
}
