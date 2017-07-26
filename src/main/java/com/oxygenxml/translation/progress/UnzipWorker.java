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
  private ArrayList<ProgressChangeListener> listeners;

  public ArrayList<String> getList() {
    return list;
  }

  public UnzipWorker(File zipDir, File rootDir, ArrayList<ProgressChangeListener> listeners) {
    this.rootDir = rootDir;
    this.zipDir = zipDir;
    this.listeners = listeners;
  }

  /**
   * Main task. Executed in background thread.
   * 
   */
  @Override
  public Void doInBackground() throws IOException, StoppedByUserException {
    ArchiveBuilder archiveBuilder = new ArchiveBuilder();
    for (ProgressChangeListener l : listeners) {
      archiveBuilder.addListener(l);
    }

    list = archiveBuilder.unzipDirectory(zipDir, rootDir);

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
