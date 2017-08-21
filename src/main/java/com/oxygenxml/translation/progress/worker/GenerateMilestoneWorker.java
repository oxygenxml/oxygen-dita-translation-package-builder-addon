package com.oxygenxml.translation.progress.worker;

import java.io.File;

import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.support.core.PackageBuilder;
/**
 * Creates an AbstractWorker for generating the milestone file.
 * 
 * @author Bivolan Dalina
 *
 */
public class GenerateMilestoneWorker extends AbstractWorker{
  /**
   * The location of the parent directory of the current ditamap.
   */
  private File rootDir;
  
  public GenerateMilestoneWorker(File rootDir) {
    this.rootDir = rootDir;
  }
  /**
   * Main task. Executed in background thread.
   */
  @Override
  protected Void doInBackground() throws Exception {
    PackageBuilder packageBuilder = new PackageBuilder();
    for (ProgressChangeListener l : listeners) {
      packageBuilder.addListener(l);
    }
    packageBuilder.generateChangeMilestone(rootDir);
    
    return null;
  }

}
