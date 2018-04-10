package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.core.resource.IRootResource;
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
  private List<ResourceInfo> modifiedResources = new ArrayList<>();
  
  /**
   * The common ancestor of all the DITA resources referred in the DITA map tree.
   */
  private String commonPath;
  
  File milestoneFile;

  /**
   * Constructor.
   * 
   * @param rootMap The root map.
   */
  public GenerateModifiedResourcesWorker(URL rootMap, File milestoneFile) {
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
    
    List<ResourceInfo> milestoneContent = null;
    if (milestoneFile == null) {
      milestoneContent = MilestoneUtil.loadMilestoneFile(resource);
    } else {
      milestoneContent = MilestoneUtil.loadMilestoneContentFromFile(milestoneFile);
    }
    
    modifiedResources = packageBuilder.collectModifiedResources(resource, milestoneContent);
    commonPath = packageBuilder.getCommonPath();
    
    return null;
  }
  
  /**
   * @return The resources that had modifications.
   */
  public List<ResourceInfo> getModifiedResources() {
    return modifiedResources;
  }
  
  /**
   * @return The common ancestor of all the DITA resources referred in the DITA map tree or <code>null</code>.
   */
  public String getCommonPath() {
    return commonPath;
  }
}
