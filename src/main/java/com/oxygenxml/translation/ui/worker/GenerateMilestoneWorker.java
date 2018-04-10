package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.net.URL;

import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
/**
 * Creates an AbstractWorker for generating the milestone file.
 * 
 * @author Bivolan Dalina
 */
public class GenerateMilestoneWorker extends AbstractWorker{
  /**
   * The root map.
   */
  private URL rootMap;
  
  /**
   * The locaiton of the milestone file.
   */
  private File milestoneFile;
  
  /**
   * Constructor.
   * 
   * @param rootMap System ID of the DITA map.
   */
  public GenerateMilestoneWorker(URL rootMap, File milestoneFile) {
    this.rootMap = rootMap;
    this.milestoneFile = milestoneFile;
  }
  
  /**
   * Main task. Executed in background thread.
   */
  @Override
  protected Void doInBackground() throws Exception {
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
    IRootResource resource = ResourceFactory.getInstance().getResource(rootMap);
    packageBuilder.generateChangeMilestone(resource, milestoneFile);
    
    return null;
  }
}
