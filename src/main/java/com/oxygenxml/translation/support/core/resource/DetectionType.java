package com.oxygenxml.translation.support.core.resource;

/**
 * Detection type for the target resources. 
 */
public enum DetectionType {
  /**
   * The resources are detected based on the file system. The target resources
   * are all the descendants of the directory that contains the map. 
   */
  FILE_SYSTEM,
  /**
   * The resources are detected by analyzing the map structure.
   */
  MAP_STRUCTURE
}
