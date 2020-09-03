package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.ui.ProgressChangeListener;

/**
 * Creates an AbstractWorker for unpacking a zip file.
 * @author Bivolan Dalina
 */
public class UnzipWorker extends AbstractWorker<List<String>> {
  
  /**
   *  Where to put the extracted files.
   */
  private File rootDir;
  
  /**
   *  The location of the archive we want to unpack.
   */
  private File zipDir;
  
  /**
   *  A list containing the relative paths of all files that were extracted from the archive.
   */
  private List<String> unpackedFiles;
  
  /**
   * @return  The list with unzipped files. 
   */
  public List<String> getUnpackedFiles() {
    return unpackedFiles;
  }
  
  /**
   * Constructor.
   * 
   * @param zipDir  Archive location.
   * @param unZipLocation It represents the common ancestor of all the DITA 
   * resources referred in the DITA map tree.Either the DITA map folder or an ancestor of it.
   */
  public UnzipWorker(File zipDir, File unZipLocation) {
    this.rootDir = unZipLocation;
    this.zipDir = zipDir;
  }

  /**
   * Main task. Executed in background thread.
   */
  @Override
  public List<String> doInBackground() throws StoppedByUserException {
    ArchiveBuilder archiveBuilder = new ArchiveBuilder(listeners);
    try {
      unpackedFiles = archiveBuilder.unzipDirectory(zipDir, rootDir);
    } catch (IOException e) {
      for (ProgressChangeListener progressChangeListener : listeners) {
        progressChangeListener.operationFailed(e);
      }
    }

    return unpackedFiles;
  }
}
