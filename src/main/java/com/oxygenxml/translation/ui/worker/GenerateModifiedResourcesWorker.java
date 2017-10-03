package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.util.ArrayList;

import com.oxygenxml.translation.support.core.PackageBuilder;
import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.FileResourceBuilder;
/**
 * Creates an AbstractWorker for generating the modified resources in a ditamap.
 * 
 * @author Bivolan Dalina
 */
public class GenerateModifiedResourcesWorker extends AbstractWorker{
  /**
   * The location of the parent directory of the current ditamap.
   */
  private File rootDir;
  /**
   * The list with all the modified resources.
   */
  private ArrayList<ResourceInfo> modifiedResources = new ArrayList<ResourceInfo>();
  /**
   * True if the method : generateModifiedResources(rootDir); is called by this worker.
   */
  private boolean isFromWorker = false;

  public GenerateModifiedResourcesWorker(File rootDir) {
    this.rootDir = rootDir;
  }
  /**
   * Main task. Executed in background thread.
   */
  @Override
  protected Void doInBackground() throws Exception {
    isFromWorker = true;
    PackageBuilder packageBuilder = new PackageBuilder(listeners);
    modifiedResources = packageBuilder.generateModifiedResources(
        FileResourceBuilder.wrap(rootDir), 
        isFromWorker);
    
    return null;
  }
  
  /**
   * @return The resources that had modifications.
   */
  public ArrayList<ResourceInfo> getModifiedResources() {
    return modifiedResources;
  }
}
