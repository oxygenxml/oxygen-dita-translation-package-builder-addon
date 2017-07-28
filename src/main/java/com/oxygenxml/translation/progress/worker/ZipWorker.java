package com.oxygenxml.translation.progress.worker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.progress.StoppedByUserException;
import com.oxygenxml.translation.support.util.ArchiveBuilder;

/**
 * Creates a SwingWorker for packing a directory.
 * 
 * @author Bivolan Dalina
 *
 */
public class ZipWorker extends AbstractWorker {
  /**
   *  The file we want to zip.
   */
  private File rootDir;
  /**
   *  Where to put the created package.
   */
  private File zipDir;
  
  public ZipWorker(File rootDir, File zipDir, ArrayList<ProgressChangeListener> listeners) {
    super(listeners);
    this.rootDir = rootDir;
    this.zipDir = zipDir;
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
}
