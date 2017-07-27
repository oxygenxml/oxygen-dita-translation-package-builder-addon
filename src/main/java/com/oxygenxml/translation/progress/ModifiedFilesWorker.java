package com.oxygenxml.translation.progress;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
  private ArrayList<ProgressChangeListener> listeners;
  
  public ModifiedFilesWorker(File rootDir, File zipDir, ArrayList<ProgressChangeListener> listeners) {
    this.rootDir = rootDir;
    this.packageLocation = zipDir;
    this.listeners = listeners;
  }

  /**
   * Main task. Executed in background thread.
   * @throws IOException 
   */
  @Override
  public Void doInBackground() throws NoSuchAlgorithmException, JAXBException, StoppedByUserException, IOException {
    PackageBuilder packageBuilder = new PackageBuilder();
    for (ProgressChangeListener l : listeners) {
      packageBuilder.addListener(l);
    }
   
    packageBuilder.generateChangedFilesPackage(rootDir, packageLocation);
  

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
