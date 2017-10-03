package com.oxygenxml.translation.support.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.TranslationPackageBuilderExtension;
import com.oxygenxml.translation.support.core.PackageBuilder;
import com.oxygenxml.translation.support.core.models.ResourceInfo;

/**
 * Create objects over the file system.
 */
public class FileResourceBuilder {
  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(TranslationPackageBuilderExtension.class); 
  
  /**
   * An implementation over a local File. 
   */
  private static abstract class AbstractFileResource implements IResource {
    /**
     * The wrapped file.
     */
    protected File file;
    /**
     * The path of this file relative to the root resource.
     */
    protected String relativePath;
    /**
     * Constructor.
     * 
     * @param file Wrapped file.
     * @param relativePath The path of this file relative to the root resource.
     */
    private AbstractFileResource(File file, String relativePath) {
      this.file = file;
      this.relativePath = relativePath;
    }
  }
  
  /**
   * An implementation over a local directory.
   */
  private static class DirResource extends AbstractFileResource {
    /**
     * Constructor.
     * 
     * @param file Wrapped file.
     * @param relativePath The path of this file relative to the root resource.
     */
    private DirResource(File dir, String relativePath) {
      super(dir, relativePath);
    }

    /**
     * @see com.oxygenxml.translation.support.util.IResource#iterator()
     */
    public Iterator<IResource> iterator() {
      Iterator<IResource> toReturn = null;
      
      File[] listFiles = file.listFiles();
      if (listFiles != null) {
        List<IResource> children = new ArrayList<IResource>(listFiles.length);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < listFiles.length; i++) {
          File child = listFiles[i];
          if (!child.getName().equals(PackageBuilder.MILESTONE_FILE_NAME)) {
            // The milestone must not be added in the package.
            b.setLength(0);
            if (relativePath.length() > 0) {
              b.append(relativePath).append("/");
            }
            b.append(child.getName());
            children.add(wrap(child, b.toString()));
          }
        }
        
        toReturn = children.iterator();
      }
      
      return toReturn;
    }

    /**
     * @see com.oxygenxml.translation.support.util.IResource#getResourceInfo()
     */
    public ResourceInfo getResourceInfo() {
      // We don't add directories into the milestone.
      return null;
    }
  }
  
  private static class RootDirResource extends DirResource implements IRootResource {
    /**
     * Constructor.
     * 
     * @param file Wrapped file.
     */
    private RootDirResource(File dir) {
      super(dir, "");
    }

    /**
     * @see com.oxygenxml.translation.support.util.IRootResource#getMilestoneFile()
     */
    public File getMilestoneFile() {
      File milestoneFile = new File(file,  PackageBuilder.MILESTONE_FILE_NAME);
      return milestoneFile;
    }
  }
  
  /**
   * An implementation over a local file.
   */
  private static class FileResource extends AbstractFileResource {
    /**
     * Constructor.
     * 
     * @param file Wrapped file.
     * @param relativePath The path of this file relative to the root resource.
     */
    public FileResource(File file, String relativePath) {
      super(file, relativePath);
    }

    /**
     * @see com.oxygenxml.translation.support.util.IResource#iterator()
     */
    public Iterator<IResource> iterator() {
      // A file is a leaf. No children.
      return null;
    }

    /**
     * @see com.oxygenxml.translation.support.util.IResource#getResourceInfo()
     */
    public ResourceInfo getResourceInfo() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
      return new ResourceInfo(PackageBuilder.generateMD5(file), relativePath);
    }
  }

  /**
   * Creates a resource over the given file.
   * 
   * @param file File to wrap as a resource.
   * @param relativePath Path relative to the root resource.
   * 
   * @return An {@link IResource} wrapper over the given file.
   */
  private static IResource wrap(File file, String relativePath) {
    if (file.isDirectory()) {
      return new DirResource(file, relativePath);
    } else {
      return new FileResource(file, relativePath);
    }
  }

  /**
   * Creates a resource over the given file.
   * 
   * @param file File to wrap as a resource.
   * 
   * @return An {@link IResource} wrapper over the given file.
   * 
   * @throws IOException The given file is not a directory. 
   */
  public static IRootResource wrap(File dir) throws IOException {
    if (!dir.isDirectory()) {
      throw new IOException("Must start from a directory");
    }
    
    return new RootDirResource(dir);
  }
}
