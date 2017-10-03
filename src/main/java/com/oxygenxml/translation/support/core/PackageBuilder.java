package com.oxygenxml.translation.support.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.core.models.InfoResources;
import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.support.util.IResource;
import com.oxygenxml.translation.support.util.IRootResource;
import com.oxygenxml.translation.support.util.ParserCreator;
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


public class PackageBuilder {
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(PackageBuilder.class); 
  /**
   * Predefined name of the file that stores a hash for each file.
   */
  public final static String MILESTONE_FILE_NAME = "translation_builder_milestone.xml";
  /**
   *  A list of custom listeners.
   */
  private List<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
  
  /**
   * Constructor.
   */
  public PackageBuilder(List<ProgressChangeListener> listeners) {
    this.listeners = listeners; 
  }
  
  public PackageBuilder() {
  }

  public static String getMilestoneFileName() {
    return MILESTONE_FILE_NAME;
  }

  /**
   * Reads a file and generates an MD5 from its content.
   * 
   * @param file The file to read.
   * 
   * @return An unique MD5 hash.
   * 
   * @throws NoSuchAlgorithmException The MD5 algorithm is not available.
   * @throws FileNotFoundException The file doesn't exist.
   * @throws IOException Problems reading the file.
   */
  public static String generateMD5(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
    MessageDigest md = MessageDigest.getInstance("MD5");

    byte[] dataBytes = new byte[8 * 1024];
    BufferedInputStream bis = new BufferedInputStream(new java.io.FileInputStream(file));
    int nread = 0;
    try {
      while ((nread = bis.read(dataBytes)) != -1) {
        md.update(dataBytes, 0, nread);
      }
    } finally {
      try {
        bis.close();
      } catch (IOException e) {
        logger.error(e, e);
      }
    }

    return toHexString(md.digest());

  }
  
  public static String toHexString(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
        String hex = Integer.toHexString(0xFF & bytes[i]);
        if (hex.length() == 1) {
            hexString.append('0');
        }
        hexString.append(hex);
    }

    return hexString.toString();
}

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
   * Saves the information about file changes on disk. 
   * 
   * @param info  An object of type InfoResources,this object will be serialized.
   * @param rootDir	The directory were the "special file" will be created after serialization.
   * 
   * @return The "translation_bulder_milestone.xml" file.
   * 
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException	The file doesn't exist.
   * @throws StoppedByUserException The user pressed the cancel button.
   */
  File  storeMilestoneFile(InfoResources info, File rootDir) throws JAXBException, FileNotFoundException, StoppedByUserException{
    if(isCanceled()){
      throw new StoppedByUserException();
    }
    File milestoneFile = new File(rootDir + File.separator + MILESTONE_FILE_NAME);

    JAXBContext contextObj = JAXBContext.newInstance(InfoResources.class);  

    Marshaller marshallerObj = contextObj.createMarshaller();  
    marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);  

    marshallerObj.marshal(info, milestoneFile); 			 

    return milestoneFile;
  }

  /**
   * Loads the information about file changes from disk.
   * 
   * @param iRootResource The location of the "special file"(milestone file).
   * 
   * @return	The content of the "special file"(milestone).
   * 
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   */
  static List<ResourceInfo> loadMilestoneFile(IRootResource iRootResource) throws JAXBException, IOException {
    File milestoneFile = iRootResource.getMilestoneFile();

    if (!milestoneFile.exists()) {
      throw new IOException("No milestone was created.");
    }

    JAXBContext jaxbContext = JAXBContext.newInstance(InfoResources.class); 

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();   

    InfoResources resources = (InfoResources) jaxbUnmarshaller.unmarshal(milestoneFile);    

    return resources.getList();
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
  public ArrayList<ResourceInfo> generateModifiedResources(
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
    List<ResourceInfo> milestoneStates = loadMilestoneFile(resource);
    
    System.out.println(TestUtil.dump(milestoneStates));
    
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
      boolean isFromTest
      ) throws NoSuchAlgorithmException, JAXBException, IOException, StoppedByUserException, NoChangedFilesException  {

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
              return PackageBuilder.this.isCanceled();
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
   * @param rootDir The directory were the "special file"(translation_builder_milestone.xml) is located.
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
  public File generateChangeMilestone(IResource resource, ParserCreator parser, File rootDir, boolean isFromTest) throws NoSuchAlgorithmException, FileNotFoundException, IOException, JAXBException, StoppedByUserException {
    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();

    computeResourceInfo(resource, list);
    File milestoneFile = new File(rootDir.getParentFile(), MILESTONE_FILE_NAME);
    /**
     * TODO check functionality of the date and time of the milestone creation.
     */
    File file = storeMilestoneFile(new InfoResources(list, new Date(milestoneFile.lastModified())), rootDir.getParentFile());
    if(isCanceled()){
      throw new StoppedByUserException();
    }
    if(!isFromTest){
      PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
      ProgressChangeEvent progress = new ProgressChangeEvent(resourceBundle.getMessage(Tags.CHANGE_MILESTONE_PROGRESS_TEXT) + "...");
      fireChangeEvent(progress);
    }

    return file;
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
