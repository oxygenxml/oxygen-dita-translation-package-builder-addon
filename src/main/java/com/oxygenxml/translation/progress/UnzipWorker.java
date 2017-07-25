package com.oxygenxml.translation.progress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import com.oxygenxml.translation.support.util.ArchiveBuilder;

/**
 * Creates a SwingWorker for unpacking a zip file.
 * 
 * @author Bivolan Dalina
 *
 */
public class UnzipWorker extends SwingWorker<Void, Void> {
  /**
   *  Where to put the extracted files.
   */
  private File rootDir;
  /**
   *  The location of the archive we want to unpack.
   */
  private File zipDir;
  /**
   *  A list containig the relative paths of all files that were extracted from the archive.
   */
  private ArrayList<String> list;
  /**
   *  A listener for notifying the changes.
   */
  private ProgressChangeListener listener;
  
  public ArrayList<String> getList() {
    return list;
  }

  public UnzipWorker(File zipDir, File rootDir, ProgressChangeListener listener) {
    this.rootDir = rootDir;
    this.zipDir = zipDir;
    this.listener = listener;
  }
  
  public UnzipWorker() {
    
  }

  /**
   * Main task. Executed in background thread.
   * 
   */
  @Override
  public Void doInBackground() throws IOException, StoppedByUserException {
    
    list = new ArchiveBuilder(listener).unzipDirectory(zipDir, rootDir);

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
