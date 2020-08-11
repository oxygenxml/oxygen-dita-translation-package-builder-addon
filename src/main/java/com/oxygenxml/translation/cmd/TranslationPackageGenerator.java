package com.oxygenxml.translation.cmd;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.oxygenxml.translation.support.util.PathUtil;
import com.oxygenxml.translation.ui.ProgressChangeListener;
import com.oxygenxml.translation.ui.worker.GenerateChangePackageWorker;
import com.oxygenxml.translation.ui.worker.GenerateMilestoneWorker;
import com.oxygenxml.translation.ui.worker.UnzipWorker;

/**
 * Utility methods for the common tasks:
 * 
 * 1. Generate a milestone.
 * 2. Create the package for translator.
 * 3. Apply a translation package over a map.
 * 
 * @author alex_jitianu
 */
public class TranslationPackageGenerator {
  /**
   * Utility methods class.
   */
  private TranslationPackageGenerator() {}

  /**
   * Iterates over the entire structure of the given DITA map and generates a milestone file. A milestone file is used later 
   * on to detect file changes.
   * 
   * @param ditaMapURL DITA Map.
   * @param progressListener An optional listener to receive notifications.
   * 
   * @return The worker that generates the milestone.
   * 
   * @throws ExecutionException 
   * @throws InterruptedException 
   */
  public static File generateMilestone(URL ditaMapURL, ProgressChangeListener progressListener) throws InterruptedException, ExecutionException {
    GenerateMilestoneWorker worker = new GenerateMilestoneWorker(ditaMapURL);
    
    if (progressListener != null) {
      worker.addProgressListener(progressListener);
    }
    
    worker.execute();
    
    return worker.get();
  }
  
  public static File createPackage(
      URL rootMap, 
      File packageFile, 
      ProgressChangeListener progressListener) throws InterruptedException, ExecutionException {
    // TODO add an option to generate the package when the milestone is missing.
    GenerateChangePackageWorker worker = new GenerateChangePackageWorker(rootMap, packageFile);
    
    if (progressListener != null) {
      worker.addProgressListener(progressListener);
    }
    
    worker.execute();
    
    return worker.get();
  }
  
  /**
   * Applies a translation package over a root map.
   * 
   * @param rootMap Target root map.
   * @param packageFile translation package.
   * @param progressListener Optional listener to get notifications.
   * 
   * @return A list with the relative path of every extracted file.
   * 
   * @throws ExecutionException 
   * @throws InterruptedException 
   */
  public static List<String> applyPackage(URL rootMap, File packageFile, ProgressChangeListener progressListener) throws InterruptedException, ExecutionException {
    // TODO Move this on the worker. It parses files and can take some time.
    // Although the way it will be invoked, we are waiting for the worker to finish.
    // TODO We could wait here for the worker... 
    final File unzipLocation = PathUtil.calculateTopLocationFile(rootMap);
    
    UnzipWorker unzipTask = new UnzipWorker(
        unzipLocation, 
        packageFile);
    
    if (progressListener != null) {
      unzipTask.addProgressListener(progressListener);
    }
    
    unzipTask.execute();
    
    return unzipTask.get();
  }
}
