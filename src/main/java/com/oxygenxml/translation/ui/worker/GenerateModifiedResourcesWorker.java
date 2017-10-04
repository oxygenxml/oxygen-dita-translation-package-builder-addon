package com.oxygenxml.translation.ui.worker;

import java.net.URL;
import java.util.ArrayList;

import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.storage.ResourceInfo;
/**
 * Creates an AbstractWorker for generating the modified resources in a ditamap.
 * 
 * @author Bivolan Dalina
 */
public class GenerateModifiedResourcesWorker extends AbstractWorker {
  /**
   * The root map.
   */
  private URL rootMap;
  /**
   * The list with all the modified resources.
   */
  private ArrayList<ResourceInfo> modifiedResources = new ArrayList<ResourceInfo>();
  /**
   * True if the method : generateModifiedResources(rootDir); is called by this worker.
   */
  private boolean isFromWorker = false;

  /**
   * Constructor.
   * 
   * @param rootMap The root map.
   */
  public GenerateModifiedResourcesWorker(URL rootMap) {
    this.rootMap = rootMap;
  }
  /**
   * Main task. Executed in background thread.
   */
  @Override
  protected Void doInBackground() throws Exception {
    isFromWorker = true;
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
    modifiedResources = packageBuilder.collectModifiedResources(
        ResourceFactory.getInstance().getResource(rootMap), 
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
