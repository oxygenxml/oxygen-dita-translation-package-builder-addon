package com.oxygenxml.translation.support.core.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import com.oxygenxml.translation.support.storage.ResourceInfo;

/**
 * A resource to be analyzed and maybe sent to translation. 
 */
public interface IResource {
  
  /**
   * @return An iterator over the child resources or <code>null</code> for a leaf.
   */
  Iterator<IResource> iterator();
  
  /**
   * If this resource is one that should be translated it return the MD5 and 
   * the relative path. Basically what you need to generate the milestone.
   * 
   * @return Milestone information or <code>null</code> if this resource shouldn't be recorded.
   */
  ResourceInfo getResourceInfo() throws NoSuchAlgorithmException, FileNotFoundException, IOException;
  
  /**
   * @return The URL of the currently verified resource.
   */
  URL getCurrentUrl();
}
