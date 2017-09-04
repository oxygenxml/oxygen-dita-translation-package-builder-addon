package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.ui.ProgressChangeListener;
import com.oxygenxml.translation.ui.StoppedByUserException;

/**
 * Creates an AbstractWorker for unpacking a zip file.
 * 
 * @author Bivolan Dalina
 *
 */
public class UnzipWorker extends AbstractWorker {
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
  private ArrayList<String> unpackedFiles;

  public ArrayList<String> getUnpackedFiles() {
    return unpackedFiles;
  }

  public UnzipWorker(File zipDir, File rootDir) {
    this.rootDir = rootDir;
    this.zipDir = zipDir;
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

    unpackedFiles = archiveBuilder.unzipDirectory(zipDir, rootDir, false);

    return null;
  }
  
  @Override
  public void done() {
    super.done();
  }
}
