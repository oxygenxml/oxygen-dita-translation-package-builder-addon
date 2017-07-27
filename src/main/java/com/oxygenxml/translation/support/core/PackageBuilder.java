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

import com.oxygenxml.translation.progress.ProgressChangeEvent;
import com.oxygenxml.translation.progress.ProgressChangeListener;
import com.oxygenxml.translation.progress.StoppedByUserException;
import com.oxygenxml.translation.support.core.models.InfoResources;
import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;

import de.schlichtherle.io.FileInputStream;


public class PackageBuilder {
  private List<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
  
  public PackageBuilder(){
    
  }
  
  public void addListener(ProgressChangeListener listener) {
    this.listeners.add(listener);
  }
  
  

  private static Logger logger = Logger.getLogger(PackageBuilder.class); 
  /**
   * Predefined name of the file that stores a hash for each file.
   */
  private final static String MILESTONE_FILE_NAME = "milestone.xml";
  
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
   */
  static void computeResourceInfo(File dirPath, Stack<String> dirs, ArrayList<ResourceInfo> list) throws NoSuchAlgorithmException, FileNotFoundException, IOException{
    File[] everythingInThisDir = dirPath.listFiles();

    if (everythingInThisDir != null){
      for (File name : everythingInThisDir) {

        if (name.isDirectory()){	
          dirs.push(name.getName());

          computeResourceInfo(name, dirs, list);

          dirs.pop();
        }
        else if (name.isFile()
            // Do not put the milestone file into the package.
            && !name.getName().equals(MILESTONE_FILE_NAME)){					
          String relativePath = "";				
          for(int i = 0; i < dirs.size(); i++) {
            relativePath += dirs.get(i)+"/";					
          }

          ResourceInfo resourceInfo = new ResourceInfo(generateMD5(name), relativePath + name.getName());

          list.add(resourceInfo);	
        }
      }
    } else{
      throw new IOException("Please select a directory.");
    }
  }
  
  /**
   * Saves the information about file changes on disk. 
   * 
   * @param info  An object of type InfoResources,this object will be serialized.
   * @param rootDir	The directory were the "special file" will be created after serialization.
   * 
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException	The file doesn't exist.
   */
  static File  storeMilestoneFile(InfoResources info, File rootDir) throws JAXBException, FileNotFoundException{
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
   * @param rootDir The location of the "special file".
   * 
   * @return	The content of the "special file"
   * 
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   */
  static ArrayList<ResourceInfo> loadMilestoneFile(File rootDir) throws JAXBException, IOException {
    File milestoneFile = new File(rootDir,  MILESTONE_FILE_NAME);

    if (!milestoneFile.exists()) {
      throw new IOException("No milestone was created.");
    }
    
    JAXBContext jaxbContext = JAXBContext.newInstance(InfoResources.class); 

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();   

    InfoResources resources = (InfoResources) jaxbUnmarshaller.unmarshal(milestoneFile);    

    return resources.getList();
  }

  /**
   * Computes what resources were changed since the last created milestore.
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
   */
  static ArrayList<ResourceInfo> generateModifiedResources(File rootDir) throws JAXBException, NoSuchAlgorithmException, FileNotFoundException, IOException{
    /**
     * 1. Loads the milestone XML from rootDIr using JAXB
     * 2. Calls generateCurrentMD5() to get the current MD5s
     * 3. Compares the current file MD5 with the old ones and collects the changed resources.
     **/
    // Store state.
    ArrayList<ResourceInfo> milestoneStates = loadMilestoneFile(rootDir);
    
    ArrayList<ResourceInfo> currentStates = new ArrayList<ResourceInfo>();
    computeResourceInfo(rootDir, new Stack<String>(), currentStates);

    ArrayList<ResourceInfo> modifiedResources = new ArrayList<ResourceInfo>();
    // Compare serializedResources with newly generated hashes.
    for (ResourceInfo newInfo : currentStates) {
      boolean modified = !milestoneStates.contains(newInfo);
      if (modified) {
        modifiedResources.add(newInfo);
      }
    }
    
    return modifiedResources;
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

   */
  public void generateChangedFilesPackage(
      File rootDir, 
      File packageLocation
      ) throws NoSuchAlgorithmException, JAXBException, IOException, StoppedByUserException  {

    /**
     * 4. Inside a temporary "destinationDir" creates a file structure and copies the changed files.
     * 5. ZIP the "destinationDir" at "packageLocation"
     * 6. Delete the "destinationDir"
     */

    //The list with all modified files.
    final ArrayList<ResourceInfo> modifiedResources = generateModifiedResources(rootDir);
    int counter = 0;
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
          
          counter++;
          ProgressChangeEvent progress = new ProgressChangeEvent(counter, counter + " files copied in a temp dir.", 2*numberOfModifiedfiles);
          fireChangeEvent(progress);
        }
        ArchiveBuilder archiveBuilder = new ArchiveBuilder();
        archiveBuilder.addListener(new ProgressChangeListener() {
          public boolean isCanceled() {
            return listeners.get(0).isCanceled();
          }
          
          public void done() {
            listeners.get(0).done();
          }
          
          public void change(ProgressChangeEvent progress) {
            listeners.get(0).change(new ProgressChangeEvent(progress.getCounter() + numberOfModifiedfiles, progress.getMessage(), 2*numberOfModifiedfiles));
          }
        });
      
        archiveBuilder.zipDirectory(tempDir, packageLocation);
      } finally {
        FileUtils.deleteDirectory(tempDir);
      }
    } else {
      // Present the date when the milestore was created.
      File milestoneFile = new File(rootDir,  MILESTONE_FILE_NAME);
      throw new IOException("There are no changed files since the milestone created on: " + new Date(milestoneFile.lastModified()));
    }
  }

  /**
   * Entry point. Compute a hash for each file in the given directory and store this information
   * inside the directory (as a "special file"). 
   * 
   * 
   * @param rootDir The directory were the "special file"(milestone.xml) is located.
   * 
   * @return	The "special file"(milestone.xml).
   * 
   * @throws NoSuchAlgorithmException	The MD5 algorithm is not available.
   * @throws FileNotFoundException	The file/directory doesn't exist.
   * @throws IOException	Problems reading the file/directory.
   * @throws JAXBException	 Problems with JAXB, serialization/deserialization of a file.
   */
  public static File generateChangeMilestone(File rootDir) throws NoSuchAlgorithmException, FileNotFoundException, IOException, JAXBException {
    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
    computeResourceInfo(rootDir, new Stack<String>(), list);
    
    return storeMilestoneFile(new InfoResources(list), rootDir);
  }
  

  private void fireChangeEvent(ProgressChangeEvent progress) {
    for (ProgressChangeListener progressChangeListener : listeners) {
      progressChangeListener.change(progress);
    }
  }

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
