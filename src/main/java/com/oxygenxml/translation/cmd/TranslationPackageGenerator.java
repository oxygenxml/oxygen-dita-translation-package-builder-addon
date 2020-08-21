package com.oxygenxml.translation.cmd;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.oxygenxml.translation.support.util.PathUtil;
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
   * @param ps An optional print stream where to write progress data and errors.
   * 
   * @return The worker that generates the milestone.
   * 
   * @throws ExecutionException 
   * @throws InterruptedException 
   */
  public static File generateMilestone(URL ditaMapURL, PrintStream ps) throws InterruptedException, ExecutionException {
    GenerateMilestoneWorker worker = new GenerateMilestoneWorker(ditaMapURL);
    
    if (ps != null) {
      worker.addProgressListener(new OutputStreamProgressChangeListener(ps));
    }
    
    worker.execute();
    
    return worker.get();
  }
  
  /**
   * Creates a package with all the changed files that need translating.
   * 
   * @param rootMap DITA Map.
   * @param packageFile Resulting package file.
   * @param ps An optional print stream where to write progress data and errors.
   * @param generateMilestone <code>true</code> to regenerate the milestone file after the package is created.
   * 
   * @return Resulting package file.
   * 
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public static void createPackage(
      URL rootMap, 
      File packageFile, 
      PrintStream ps,
      boolean generateMilestone) throws InterruptedException, ExecutionException {
    GenerateChangePackageWorker worker = new GenerateChangePackageWorker(rootMap, packageFile);
    
    if (ps != null) {
      worker.addProgressListener(new OutputStreamProgressChangeListener(ps));
    }
    
    worker.execute();
    
    worker.get();
    
    // Regenerate the milestone.
    if (generateMilestone) {
      generateMilestone(rootMap, ps);
    }
  }
  
  /**
   * Applies a translation package over a root map.
   * 
   * @param rootMap Target root map.
   * @param packageFile translation package.
   * @param ps An optional print stream where to write progress data and errors.
   * 
   * @return A list with the relative path of every extracted file.
   * 
   * @throws ExecutionException 
   * @throws InterruptedException 
   */
  public static List<String> applyPackage(
      URL rootMap, 
      File packageFile, 
      PrintStream ps) throws InterruptedException, ExecutionException {
    File unzipLocation = PathUtil.calculateTopLocationFile(rootMap);
    
    UnzipWorker unzipTask = new UnzipWorker(
        packageFile,
        unzipLocation);
    
    if (ps != null) {
      unzipTask.addProgressListener(new OutputStreamProgressChangeListener(ps));
    }
    
    unzipTask.execute();
    
    return unzipTask.get();
  }
}
