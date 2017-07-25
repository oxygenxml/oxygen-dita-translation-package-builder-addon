package com.oxygenxml.translation.progress;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.swing.SwingWorker;
import javax.xml.bind.JAXBException;

import com.oxygenxml.translation.support.core.PackageBuilder;

/**
 * Creates a SwingWorker for packing all the modified files inside a directory.
 * 
 * @author Bivolan Dalina
 *
 */
public class ModifiedFilesWorker extends SwingWorker<Void, Void>{
  /**
   *  The directory were we want to see what files were changed.
   */
  private File rootDir;
  /**
   * Were to make the archive with the modified files.
   */
  private File packageLocation;
  /**
   *  A listener for notifying the changes.
   */
  private ProgressChangeListener listener;
  
  public ModifiedFilesWorker(File rootDir, File zipDir, ProgressChangeListener listener) {
    this.rootDir = rootDir;
    this.packageLocation = zipDir;
    this.listener = listener;
  }

  /**
   * Main task. Executed in background thread.
   */
  @Override
  public Void doInBackground() throws IOException, NoSuchAlgorithmException, JAXBException, StoppedByUserException {
    try{
      PackageBuilder.generateChangedFilesPackage(rootDir, packageLocation, listener);
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
