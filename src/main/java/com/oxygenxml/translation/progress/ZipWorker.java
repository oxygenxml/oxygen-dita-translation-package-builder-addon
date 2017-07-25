package com.oxygenxml.translation.progress;

import java.io.File;
import java.io.IOException;

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
  private ProgressChangeListener listener;
  
  
  public ZipWorker(File rootDir, File zipDir, ProgressChangeListener listener) {
    this.rootDir = rootDir;
    this.zipDir = zipDir;
    this.listener = listener;
  }

  /**
   * Main task. Executed in background thread.
   */
  @Override
  public Void doInBackground() throws IOException, StoppedByUserException {
    try{
      new ArchiveBuilder(listener).zipDirectory(rootDir, zipDir);
    }
    catch (IOException e) {
    }

    return null;
  }

  /**
   * Executed in event dispatching thread
   */
  @Override
  public void done() {
    listener.done();
  }
}
