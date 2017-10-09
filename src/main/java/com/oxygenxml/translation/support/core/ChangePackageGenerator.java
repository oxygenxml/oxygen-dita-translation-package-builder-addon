package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.core.resource.IResource;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.storage.InfoResources;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.ui.NoChangedFilesException;
import com.oxygenxml.translation.ui.PackResult;
import com.oxygenxml.translation.ui.ProgressChangeAdapter;
import com.oxygenxml.translation.ui.ProgressChangeEvent;
import com.oxygenxml.translation.ui.ProgressChangeListener;
import com.oxygenxml.translation.ui.StoppedByUserException;
import com.oxygenxml.translation.ui.Tags;

import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * What this class does:
 * 
 * 1. generateChangeMilestone   - iterates over a directory, computes MD5s and writes them in a marker file.
 * 2. collectModifiedResources  - iterates over a directory, computes MD5s and compares them with the ones from the milestone. 
 * 3. generateChangedFilesPackage - puts the modified files in ZIP
 * 
 */
public class ChangePackageGenerator {
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(ChangePackageGenerator.class); 
  /**
   *  A list of custom listeners.
   */
  private List<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
  
  /**
   * Constructor.
   */
  public ChangePackageGenerator(List<ProgressChangeListener> listeners) {
    this.listeners = listeners; 
  }
  
  public ChangePackageGenerator() {}

  /**
   * Iterates over the descendents of the given files and computes a hash and a relative path.
   * 
   * The computed relative paths are relative to the entry point. For example:
   * Entry point: c:testIteration
   * Relative path: dir1/test.txt
   * Relative path: dir2/test.txt
   * Relative path: dir2/dir21/test.txt
   * 
   * @param dirs A stack used to compute a path relative to an ancestor.
   * @param list A list of ResourceTnfo objects which contains an unique MD5 and a relative path for
   *        every file inside dirPath.
   * 
   * @throws NoSuchAlgorithmException The MD5 algorithm is not available.
   * @throws FileNotFoundException  The file doesn't exist.
   * @throws IOException Problems reading the file/directory.
   * @throws StoppedByUserException The user pressed the cancel button.
   */
  void computeResourceInfo(IResource resource, List<ResourceInfo> list) 
      throws NoSuchAlgorithmException, FileNotFoundException, IOException, StoppedByUserException {
    
    Iterator<IResource> everythingInThisDir = resource.iterator();

    if (everythingInThisDir != null /*&& !everythingInThisDir.isEmpty()*/) {
      while (everythingInThisDir.hasNext()) {
        if(isCanceled()){
          throw new StoppedByUserException();
        }
        
        IResource iResource = (IResource) everythingInThisDir.next();
        
        // Collect the milestone related info.
        ResourceInfo resourceInfo = iResource.getResourceInfo();
        if (resourceInfo != null) {
          list.add(resourceInfo);
        }
        
        // Go deep.
        computeResourceInfo(iResource, list);
      }
    }
  }

  /**
   * Computes what resources were changed since the last created milestone.
   * 
   * @param isFromWorker True if the method is called by the GenerateModifiedResourcesWorker.
   *  
   * @return	A list of objects ResourceInfo which contains a MD5 and a relative path for
   * every file that was changed/every modification you made inside the rootDir. An empty list if nothing changed.
   * 
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   * @throws NoSuchAlgorithmException	The MD5 algorithm is not available.
   * @throws FileNotFoundException	The file/directory doesn't exist.
   * @throws IOException	Problems reading the file/directory.
   * @throws StoppedByUserException The user pressed the cancel button.
   */
  public ArrayList<ResourceInfo> collectModifiedResources(
      IRootResource resource, 
      boolean isFromWorker) throws JAXBException, NoSuchAlgorithmException, FileNotFoundException, IOException, StoppedByUserException{
    /**
     * 1. Loads the milestone XML from rootDIr using JAXB
     * 2. Calls generateCurrentMD5() to get the current MD5s
     * 3. Compares the current file MD5 with the old ones and collects the changed resources.
     **/
    if(logger.isDebugEnabled()){
      logger.debug("Cames from modifiedResourcesWorker?-->" + isFromWorker);
    }
    // Store state.
    List<ResourceInfo> milestoneStates = MilestoneUtil.loadMilestoneFile(resource);
    
    //Current states.
    ArrayList<ResourceInfo> currentStates = new ArrayList<ResourceInfo>();
    computeResourceInfo(resource, currentStates);
    // A list to hold the modified resources.
    ArrayList<ResourceInfo> modifiedResources = new ArrayList<ResourceInfo>();
    int counter = 0;

    // Compare serializedResources with newly generated hashes.
    for (ResourceInfo newInfo : currentStates) {
      boolean modified = !milestoneStates.contains(newInfo);
      if (modified) {
        modifiedResources.add(newInfo);
      }
      if(isFromWorker){
        PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
        counter++;
        if(isCanceled()){
          throw new StoppedByUserException();
        }
        ProgressChangeEvent progress = new ProgressChangeEvent(counter, 
            resourceBundle.getMessage(Tags.GENERATE_MODIFIED_FILES_PROGRESS_MESSAGE1) + 
            counter + 
            resourceBundle.getMessage(Tags.GENERATE_MODIFIED_FILES_PROGRESS_MESSAGE2), 
            currentStates.size());
        fireChangeEvent(progress);
      }
    }
    return modifiedResources;
  }
  /**
   * Entry point. Detect what files were modified and put them in a ZIP.
   * 
   * @param rootDir The location of the directory we want to see what files were changed.
   * @param packageLocation The location of the generated ZIP file.
   * @param modifiedResources The list with all the modified files.
   * @param isFromTest True if this method is called by a JUnit test class.
   * 
   * @return How many files were modified.
   * 
   * @throws NoSuchAlgorithmException  The MD5 algorithm is not available.
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws IOException  Problems reading the file/directory.
   * @throws StoppedByUserException The user pressed the Cancel button.
   * @throws NoChangedFilesException  No file was changed since the last generation of a milestone file.
   */
  public PackResult generateChangedFilesPackage(
      File rootDir,
      File packageLocation,
      ArrayList<ResourceInfo> modifiedResources,
      boolean isFromTest) throws NoSuchAlgorithmException, JAXBException, IOException, StoppedByUserException, NoChangedFilesException  {

    /**
     * 1. Inside a temporary "destinationDir" creates a file structure and copies the changed files.
     * 2. ZIP the "destinationDir" at "packageLocation".
     * 3. Delete the "destinationDir".
     */
    
    PackResult result = new PackResult();

    int nrModFiles = 0;
    final int totalModifiedfiles = modifiedResources.size();
    // If there are modified resources
    if (!modifiedResources.isEmpty()) {
        final File tempDir = new File(rootDir, "toArchive");

        //We iterate over the list above, build the sistem of files in a temporary directory and copy the 
        //files in the right directory
        //Then we compress the tempDir and delete it.
        try{
          for(ResourceInfo aux : modifiedResources){
            File dest = new File(tempDir, aux.getRelativePath());
            dest.getParentFile().mkdirs();

            FileUtils.copyFile(new File(rootDir.getPath() + File.separator + aux.getRelativePath()), dest);

            if(isCanceled()){
              throw new StoppedByUserException();
            }
            if(!isFromTest){
              PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
              nrModFiles++;
              ProgressChangeEvent progress = new ProgressChangeEvent(nrModFiles, resourceBundle.getMessage(Tags.PACKAGEBUILDER_PROGRESS_TEXT1) + nrModFiles + resourceBundle.getMessage(Tags.PACKAGEBUILDER_PROGRESS_TEXT2), 2*totalModifiedfiles);
              fireChangeEvent(progress);
            }
          }

          result.setModifiedFilesNumber(nrModFiles);

          ArchiveBuilder archiveBuilder = new ArchiveBuilder();
          archiveBuilder.addListener(new ProgressChangeAdapter() {
            public boolean isCanceled() {
              return ChangePackageGenerator.this.isCanceled();
            }

            public void done() {
              fireDoneEvent();
            }

            public void change(ProgressChangeEvent progress) {
              ProgressChangeEvent event = new ProgressChangeEvent(progress.getCounter() + totalModifiedfiles, progress.getMessage(), 2*totalModifiedfiles);
              fireChangeEvent(event);
            }
          });

          archiveBuilder.zipDirectory(tempDir, packageLocation, isFromTest);
        } finally {
          FileUtils.deleteDirectory(tempDir);
        }
    } /*else {
      // Present the date when the milestore was created.
      File milestoneFile = new File(rootDir,  MILESTONE_FILE_NAME);

      // Trow a customn exception.
      NoChangedFilesException t = new NoChangedFilesException("There are no changed files since the milestone created on: " + new Date(milestoneFile.lastModified()));
      throw t;
    }*/

    return result;
  }

  /**
   * Entry point. Compute a hash for each file in the given directory and store this information
   * inside the directory (as a "special file"). 
   * 
   * 
   * @param isFromTest True if this method is called by a JUnit test class.
   * 
   * @return	The "special file"(translation_builder_milestone.xml).
   * 
   * @throws NoSuchAlgorithmException	The MD5 algorithm is not available.
   * @throws FileNotFoundException	The file/directory doesn't exist.
   * @throws IOException	Problems reading the file/directory.
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   * @throws StoppedByUserException The user pressed the "Cancel" button.
   */
  public File generateChangeMilestone(
      IRootResource resource, 
      boolean isFromTest) throws NoSuchAlgorithmException, FileNotFoundException, IOException, JAXBException, StoppedByUserException {
    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();

    computeResourceInfo(resource, list);
    File milestoneFile = resource.getMilestoneFile();
    /**
     * TODO Adrian check functionality of the date and time of the milestone creation.
     */
    long lastModified = milestoneFile.lastModified();
    if (lastModified == 0) {
      lastModified = new Date().getTime();
    }
    MilestoneUtil.storeMilestoneFile(
        new InfoResources(list, new Date(lastModified)), 
        milestoneFile);
    if(isCanceled()){
      throw new StoppedByUserException();
    }
    if (!isFromTest) {
      PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
      ProgressChangeEvent progress = new ProgressChangeEvent(resourceBundle.getMessage(Tags.CHANGE_MILESTONE_PROGRESS_TEXT) + "...");
      fireChangeEvent(progress);
    }

    return milestoneFile;
  }

  /**
   * Notifies all listeners to update the progress of the task.
   * 
   * @param progress A ProgressChangeEvent object.
   */
  private void fireChangeEvent(ProgressChangeEvent progress) {
    for (ProgressChangeListener progressChangeListener : listeners) {
      progressChangeListener.change(progress);
    }
  }
  /**
   * Notifies all listeners that the task has finished.
   */
  private void fireDoneEvent() {
    for (ProgressChangeListener progressChangeListener : listeners) {
      progressChangeListener.done();
    }
  }
  /**
   * Notifies all listeners that the task was canceled.
   * 
   * @return True if the worker was canceled, false otherwise.
   */
  private boolean isCanceled() {
    boolean result = false;
    for (ProgressChangeListener progressChangeListener : listeners) {
      if (progressChangeListener.isCanceled()) {
        result =  true;
      }
    }
    return result;
  }

}
