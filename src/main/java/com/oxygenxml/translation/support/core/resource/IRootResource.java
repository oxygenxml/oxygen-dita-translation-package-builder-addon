package com.oxygenxml.translation.support.core.resource;

import java.io.File;

/**
 * The root resource. Knows where it should be stored.
 */
public interface IRootResource extends IResource {
  
  /**
   * @return The file where to store the milestone information.
   */
  File getMilestoneFile();
}
