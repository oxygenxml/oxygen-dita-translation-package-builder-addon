package com.oxygenxml.translation.support.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.oxygenxml.translation.progress.NoChangedFilesException;
import com.oxygenxml.translation.progress.ProgressChangeEvent;
import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.progress.StoppedByUserException;
import com.oxygenxml.translation.progress.Tags;
import com.oxygenxml.translation.progress.worker.GenerateModifiedResourcesWorker;
import com.oxygenxml.translation.support.core.models.InfoResources;
import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;

import de.schlichtherle.io.FileInputStream;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;


public class PackageBuilder {
  /**
   *  Resource bundle.
   */
  private final static PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(PackageBuilder.class); 
  /**
   * Predefined name of the file that stores a hash for each file.
   */
  private final static String MILESTONE_FILE_NAME = resourceBundle.getMessage(Tags.MILESTONE_NAME);
  /**
   *  A list of custom listeners.
   */
  private static List<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();

  public PackageBuilder(){
    
  }
  
  public void addListener(ProgressChangeListener listener) {
    PackageBuilder.listeners.add(listener);
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
  static String generateMD5(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
    MessageDigest md = MessageDigest.getInstance("MD5");

    byte[] dataBytes = new byte[8 * 1024];
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
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

    byte[] mdbytes = md.digest();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < mdbytes.length; i++) {
      sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();
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
   * @param dirPath The location of the directory to iterate.
   * @param dirs A stack used to compute a path relative to an ancestor.
   * 
   * @return A list of ResourceTnfo objects which contains an unique MD5 and a relative path for
   * every file inside dirPath.
   * 
   * @throws NoSuchAlgorithmException The MD5 algorithm is not available.
   * @throws FileNotFoundException  The file doesn't exist.
   * @throws IOException Problems reading the file/directory.
   * @throws StoppedByUserException The user pressed the cancel button.
   */
  static void computeResourceInfo(File dirPath, Stack<String> dirs, ArrayList<ResourceInfo> list) throws NoSuchAlgorithmException, FileNotFoundException, IOException, StoppedByUserException{
    File[] everythingInThisDir = dirPath.listFiles();

    if (everythingInThisDir != null){
      for (File name : everythingInThisDir) {

        if (name.isDirectory()){	
          if(isCanceled()){
            throw new StoppedByUserException("You pressed the Cancel button.");
          }
          
          dirs.push(name.getName());

          computeResourceInfo(name, dirs, list);

          dirs.pop();
        }
        else if (name.isFile()
            // Do not put the milestone file into the package.
            && !name.getName().equals(MILESTONE_FILE_NAME)){	
          if(isCanceled()){
            throw new StoppedByUserException("You pressed the Cancel button.");
          }
          String relativePath = "";				
          for(int i = 0; i < dirs.size(); i++) {
            relativePath += dirs.get(i)+"/";					
          }

          ResourceInfo resourceInfo = new ResourceInfo(generateMD5(name), relativePath + name.getName());

          list.add(resourceInfo);	
        }
      }
    } else{
      throw new IOException(resourceBundle.getMessage(Tags.PREVIEW_DIALOG_IF_FILE_IS_NOT_DIR));
    }
  }
  
  /**
   * Saves the information about file changes on disk. 
   * 
   * @param info  An object of type InfoResources,this object will be serialized.
   * @param rootDir	The directory were the "special file" will be created after serialization.
   * @return The "translation_bulder_milestone.xml" file.
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException	The file doesn't exist.
   * @throws StoppedByUserException The user pressed the cancel button.
   */
  static File  storeMilestoneFile(InfoResources info, File rootDir) throws JAXBException, FileNotFoundException, StoppedByUserException{
    if(isCanceled()){
      throw new StoppedByUserException("You pressed the Cancel button.");
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
   * @param rootDir The location of the "special file"(milestone file).
   * 
   * @return	The content of the "special file"(milestone).
   * 
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   */
  static ArrayList<ResourceInfo> loadMilestoneFile(File rootDir) throws JAXBException, IOException {
    File milestoneFile = new File(rootDir,  MILESTONE_FILE_NAME);

    if (!milestoneFile.exists()) {
      throw new IOException(resourceBundle.getMessage(Tags.LOAD_MILESTONE_EXCEPTION));
    }
    
    JAXBContext jaxbContext = JAXBContext.newInstance(InfoResources.class); 

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();   

    InfoResources resources = (InfoResources) jaxbUnmarshaller.unmarshal(milestoneFile);    

    return resources.getList();
  }

  /**
   * Computes what resources were changed since the last created milestone.
   * 
   * @param rootDir The location of the directory we want to see what files were changed.
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
  public ArrayList<ResourceInfo> generateModifiedResources(File rootDir) throws JAXBException, NoSuchAlgorithmException, FileNotFoundException, IOException, StoppedByUserException{
    /**
     * 1. Loads the milestone XML from rootDIr using JAXB
     * 2. Calls generateCurrentMD5() to get the current MD5s
     * 3. Compares the current file MD5 with the old ones and collects the changed resources.
     **/
    System.out.println(GenerateModifiedResourcesWorker.isFromWorker() + " cames from worker?");
    // Store state.
    ArrayList<ResourceInfo> milestoneStates = loadMilestoneFile(rootDir);
    
    ArrayList<ResourceInfo> currentStates = new ArrayList<ResourceInfo>();
    computeResourceInfo(rootDir, new Stack<String>(), currentStates);

    ArrayList<ResourceInfo> modifiedResources = new ArrayList<ResourceInfo>();
    int counter = 0;
   
    // Compare serializedResources with newly generated hashes.
    for (ResourceInfo newInfo : currentStates) {
      boolean modified = !milestoneStates.contains(newInfo);
      if (modified) {
        modifiedResources.add(newInfo);
      }
      if(GenerateModifiedResourcesWorker.isFromWorker()){
        counter++;
        if(isCanceled()){
          throw new StoppedByUserException("You pressed the Cancel button.");
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
   * TODO It is a bad practice to use statics to keep the result of an operation.
   * 
   * generateChangedFilesPackage() can return an object PackResult with this information.
   */
  private static int nrModFiles = 0;
  
  public static int getCounter() {
    return nrModFiles;
  }

  /**
   * Entry point. Detect what files were modified and put them in a ZIP.
   * 
   * 
   * @param rootDir The location of the directory we want to see what files were changed.
   * @param packageLocation The location of the generated ZIP file.
   * @param listener A ProgressChangedListener for sending the updates.
   *  
   * @throws IOException  Problems reading the file/directory.
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws NoSuchAlgorithmException  The MD5 algorithm is not available.
   * @throws StoppedByUserException The user pressed the Cancel button.
   *
   */
  public void generateChangedFilesPackage(
      File rootDir, 
      File packageLocation
      ) throws NoSuchAlgorithmException, JAXBException, IOException, StoppedByUserException, NoChangedFilesException  {

    /**
     * 4. Inside a temporary "destinationDir" creates a file structure and copies the changed files.
     * 5. ZIP the "destinationDir" at "packageLocation".
     * 6. Delete the "destinationDir".
     */

    //The list with all modified files.
    final ArrayList<ResourceInfo> modifiedResources = generateModifiedResources(rootDir);
    
    // TODO packageLocation => PackageLocationProvider.getPackageLocation() that will present the chooser.
    /**
     * if (!modifiedResources.isEmpty()) {
     *   File packageLocation = packageLocationProvider.getPackageLocation();
     *   if (packageLocation == null) {
     *     throw StoppedByUserException();
     *   } else {
     * .........THE JOB.....
     * ..................
     *   }
     * } else {
     *   // Show be presented.
     *   throw new NoChangedFilesException();
     * 
     * }
     * 
     * 
     */
    
    nrModFiles = 0;
    final int numberOfModifiedfiles = modifiedResources.size();
    if (!modifiedResources.isEmpty()) {
      File tempDir = new File(rootDir, "toArchive");

      //We iterate over the list above, build the sistem of files in a temporary directory and copy the 
      //files in the right directory
      //Then we compress the tempDir and delete it.
      try{
        for(ResourceInfo aux : modifiedResources){
          File dest = new File(tempDir, aux.getRelativePath());
          dest.getParentFile().mkdirs();

          FileUtils.copyFile(new File(rootDir.getPath() + File.separator + aux.getRelativePath()), dest);
          
          if(isCanceled()){
            throw new StoppedByUserException("You pressed the Cancel button.");
          }
          
          nrModFiles++;
          ProgressChangeEvent progress = new ProgressChangeEvent(nrModFiles, resourceBundle.getMessage(Tags.PACKAGEBUILDER_PROGRESS_TEXT1) + nrModFiles + resourceBundle.getMessage(Tags.PACKAGEBUILDER_PROGRESS_TEXT2), 2*numberOfModifiedfiles);
          fireChangeEvent(progress);
        }
        ArchiveBuilder archiveBuilder = new ArchiveBuilder();
        archiveBuilder.addListener(new ProgressChangeListener() {
          public boolean isCanceled() {
            return PackageBuilder.isCanceled();
          }
          
          public void done() {
            fireDoneEvent();
          }
          
          public void change(ProgressChangeEvent progress) {
            ProgressChangeEvent event = new ProgressChangeEvent(progress.getCounter() + numberOfModifiedfiles, progress.getMessage(), 2*numberOfModifiedfiles);
            fireChangeEvent(event);
          }

          public void operationFailed(Exception ex) {}
        });
      
        archiveBuilder.zipDirectory(tempDir, packageLocation);
      } finally {
        FileUtils.deleteDirectory(tempDir);
      }
    } else {
      // Present the date when the milestore was created.
      File milestoneFile = new File(rootDir,  MILESTONE_FILE_NAME);
      
      // Trow a customn exception.
      NoChangedFilesException t = new NoChangedFilesException(resourceBundle.getMessage(Tags.ACTION2_NO_CHANGED_FILES_EXCEPTION) + new Date(milestoneFile.lastModified()));
      
      //System.out.println("To throw " + t.getMessage());
      
      throw t;
    }
  }

  /**
   * Entry point. Compute a hash for each file in the given directory and store this information
   * inside the directory (as a "special file"). 
   * 
   * 
   * @param rootDir The directory were the "special file"(translation_builder_milestone.xml) is located.
   * 
   * @return	The "special file"(translation_builder_milestone.xml).
   * 
   * @throws NoSuchAlgorithmException	The MD5 algorithm is not available.
   * @throws FileNotFoundException	The file/directory doesn't exist.
   * @throws IOException	Problems reading the file/directory.
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   * @throws StoppedByUserException The user pressed the "Cancel" button.
   */
  public File generateChangeMilestone(File rootDir) throws NoSuchAlgorithmException, FileNotFoundException, IOException, JAXBException, StoppedByUserException {
    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
    computeResourceInfo(rootDir, new Stack<String>(), list);
    
    File file = storeMilestoneFile(new InfoResources(list), rootDir);
    if(isCanceled()){
      throw new StoppedByUserException("You pressed the Cancel button.");
    }
    ProgressChangeEvent progress = new ProgressChangeEvent(resourceBundle.getMessage(Tags.CHANGE_MILESTONE_PROGRESS_TEXT) + "...");
    fireChangeEvent(progress);
    
    return file;
  }
  

  private void fireChangeEvent(ProgressChangeEvent progress) {
    for (ProgressChangeListener progressChangeListener : listeners) {
      progressChangeListener.change(progress);
    }
  }
  
  private void fireDoneEvent() {
    for (ProgressChangeListener progressChangeListener : listeners) {
      progressChangeListener.done();
    }
  }

  private static boolean isCanceled() {
    boolean result = false;
    for (ProgressChangeListener progressChangeListener : listeners) {
      if (progressChangeListener.isCanceled()) {
        result =  true;
      }
    }
    return result;
  }

}
