package com.oxygenxml.translation.progress.worker;

import java.io.File;
import java.util.ArrayList;

import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.support.core.PackageBuilder;
import com.oxygenxml.translation.support.core.models.*;
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
  private ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
  public ArrayList<ResourceInfo> getList() {
    return list;
  }
  /**
   * True if the method : generateModifiedResources(rootDir); is called by this worker.
   */
  private static boolean isFromWorker = false;
  public static boolean isFromWorker() {
    return isFromWorker;
  }
  public static void setFromWorker(boolean isFromWorker) {
    GenerateModifiedResourcesWorker.isFromWorker = isFromWorker;
  }

  public GenerateModifiedResourcesWorker(ArrayList<ProgressChangeListener> listeners, File rootDir) {
    super(listeners);
    
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
    list = packageBuilder.generateModifiedResources(rootDir);
    
    return null;
  }

}
