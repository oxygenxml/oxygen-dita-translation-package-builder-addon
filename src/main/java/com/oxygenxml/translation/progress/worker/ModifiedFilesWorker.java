package com.oxygenxml.translation.progress.worker;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.progress.StoppedByUserException;
import com.oxygenxml.translation.support.core.PackageBuilder;

/**
 * Creates a SwingWorker for packing all the modified files inside a directory.
 * 
 * @author Bivolan Dalina
 *
 */
public class ModifiedFilesWorker extends AbstractWorker {
  
  /**
   *  The directory were we want to see what files were changed.
   */
  private File rootDir;
  /**
   * Were to make the archive with the modified files.
   */
  private File packageLocation;
  
  public ModifiedFilesWorker(File rootDir, File zipDir, ArrayList<ProgressChangeListener> listeners) {
    super(listeners);
    this.rootDir = rootDir;
    this.packageLocation = zipDir;
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
}
