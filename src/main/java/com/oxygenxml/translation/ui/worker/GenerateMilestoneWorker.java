package com.oxygenxml.translation.ui.worker;

import java.net.URL;

import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
/**
 * Creates an AbstractWorker for generating the milestone file.
 * 
 * @author Bivolan Dalina
 *
 */
public class GenerateMilestoneWorker extends AbstractWorker{
  /**
   * The root map.
   */
  private URL rootMap;
  
  public GenerateMilestoneWorker(URL rootMap) {
    this.rootMap = rootMap;
  }
  /**
   * Main task. Executed in background thread.
   */
  @Override
  protected Void doInBackground() throws Exception {
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
    packageBuilder.generateChangeMilestone(
        ResourceFactory.getInstance().getResource(rootMap), 
        false);
    
    return null;
  }
}
