package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.util.ArrayList;

import com.oxygenxml.translation.support.core.PackageBuilder;
import com.oxygenxml.translation.support.core.models.*;
import com.oxygenxml.translation.support.util.OxygenParserCreator;
import com.oxygenxml.translation.support.util.SaxResourceIteration;
import com.oxygenxml.translation.ui.ProgressChangeListener;
/**
 * Creates an AbstractWorker for generating the modified resources in a ditamap.
 * 
 * @author Bivolan Dalina
 *
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
  public ArrayList<ResourceInfo> getModifiedResources() {
    return modifiedResources;
  }
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
    PackageBuilder packageBuilder = new PackageBuilder();
    for (ProgressChangeListener l : listeners) {
      packageBuilder.addListener(l);
    }
    modifiedResources = packageBuilder.generateModifiedResources(new SaxResourceIteration(), new OxygenParserCreator(), rootDir, isFromWorker);
    
    return null;
  }
}
