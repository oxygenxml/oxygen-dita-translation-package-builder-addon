package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.io.IOException;
import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
/**
 *  Creates an AbstractWorker for copying the  files from a source directory to a destination directory.
 *  
 * @author Bivolan Dalina
 */
public class CopyDirectoryWorker extends AbstractWorker<Void> {
  /**
   *  The location of the destination directory.
   */
  private File rootDir;
  /**
   *  The location of the source directory.
   */
  private File temDir;

  public CopyDirectoryWorker(File rootDir, File tempDir) {
    this.rootDir = rootDir;
    this.temDir = tempDir;
  }

  /**
   * Main task. Executed in background thread.
   */
  @Override
  public Void doInBackground() throws IOException, StoppedByUserException {
    ArchiveBuilder archiveBuilder = new ArchiveBuilder(listeners);
    archiveBuilder.copyDirectory(temDir, rootDir, 0, false);

    return null;
  }
}
