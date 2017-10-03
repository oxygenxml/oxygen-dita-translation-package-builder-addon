package com.oxygenxml.translation.support.util;

import java.io.File;

public interface IRootResource extends IResource {
  /**
   * @return The file where to store the milestone information.
   */
  File getMilestoneFile();
}
