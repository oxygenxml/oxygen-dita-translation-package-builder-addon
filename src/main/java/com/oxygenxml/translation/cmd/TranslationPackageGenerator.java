package com.oxygenxml.translation.cmd;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.support.util.PackageGeneratorUtil;
import com.oxygenxml.translation.support.util.PathUtil;
import com.oxygenxml.translation.ui.ProgressChangeListener;

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
   * @param milestone The location where to save the milestone file. If <code>null</code>, the milestone file is 
   * generated next to the root map.
   * @param ps An optional print stream where to write progress data and errors.
   * 
   * @return The milestone file.
   * 
   * @throws IOException Problems while generating the milestone.
   * @throws JAXBException Problems while serializing the milestone data.
   * @throws NoSuchAlgorithmException Problems while computing the MD5 keys needs for the milestone file.
   */
  public static File generateMilestone(URL ditaMapURL, File milestone, PrintStream ps) throws IOException, 
  NoSuchAlgorithmException, JAXBException {
    
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator();
    if (ps != null) {
      packageBuilder.addProgressListener(new OutputStreamProgressChangeListener(ps));
    }
    IRootResource resource = ResourceFactory.getInstance().getResource(ditaMapURL, milestone);
    try {
      return packageBuilder.generateChangeMilestone(resource);
    } catch (StoppedByUserException e) {
      // The progress listener we pass can't stop process.
      throw new IOException(e);
    }
  }
  
  /**
   * Creates a package with all the changed files that need translating.
   * 
   * @param rootMap DITA Map.
   * @param milestone The location of the milestone file. If <code>null</code>, the milestone file is 
   * searched for next to the root map.
   * @param packageFile Resulting package file.
   * @param ps An optional print stream where to write progress data and errors.
   * @param generateMilestone <code>true</code> to regenerate the milestone file after the package is created.
   * 
   * @return Resulting package file.
   * 
   * @throws IOException Problems while creating the package.
   * @throws JAXBException Problems while loading the milestone file.
   * @throws NoSuchAlgorithmException Problems while computing the MD5 keys needed to detect changes in files.
   */
  public static void createPackage(
      URL rootMap, 
      File milestoneFile,
      File packageFile, 
      PrintStream ps,
      boolean generateMilestone) throws NoSuchAlgorithmException, IOException, JAXBException {
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator();
    OutputStreamProgressChangeListener l = null;
    if (ps != null) {
      l = new OutputStreamProgressChangeListener(ps);
      packageBuilder.addProgressListener(l);
    }
    
    try {
      List<ResourceInfo> modifiedResources = packageBuilder.collectModifiedResources(
          ResourceFactory.getInstance().getResource(rootMap, milestoneFile));

      PackageGeneratorUtil.zipModifiedResources(
          rootMap, 
          l != null ? java.util.Arrays.asList(new ProgressChangeListener[] {l}) : Collections.emptyList(), 
          packageFile, 
          modifiedResources);

      // Regenerate the milestone.
      if (generateMilestone) {
        generateMilestone(rootMap, milestoneFile, ps);
      }
    } catch (StoppedByUserException e) {
      // The progress listener we pass can't stop process.
      throw new IOException(e);
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
   * @throws IOException Problems while applying the package.
   */
  public static List<String> applyPackage(
      URL rootMap, 
      File packageFile, 
      PrintStream ps) throws IOException {
    File unzipLocation = PathUtil.calculateTopLocationFile(rootMap);
    
    ArchiveBuilder archiveBuilder = new ArchiveBuilder();
    if (ps != null) {
      archiveBuilder.addProgressListener(new OutputStreamProgressChangeListener(ps));
    }
    try {
    return archiveBuilder.unzipDirectory(
        packageFile,
        unzipLocation);
    } catch (StoppedByUserException e) {
      // The progress listener we pass can't stop process.
      throw new IOException(e);
    }
  }
}
