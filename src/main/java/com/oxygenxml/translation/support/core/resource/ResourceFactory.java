package com.oxygenxml.translation.support.core.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Creates various builders that pack resources into a ZIP. 
 */
public class ResourceFactory {
  /**
   * Singleton.
   */
  private static ResourceFactory instance;
  /**
   * The type of detection.
   */
  private DetectionType detectionType;
  /**
   * Constructor.
   */
  private ResourceFactory() {
    detectionType = DetectionType.FILE_SYSTEM;
  }
  
  /**
   * @return The factory instance.
   */
  public static ResourceFactory getInstance() {
    if (instance == null) {
      instance = new ResourceFactory();
    }
    return instance;
  }
  
  /**
   * Creates a resource builder for the given map.
   * 
   * @param map The starting point. The root map.
   * 
   * @throws IOException Failed to create the resource. 
   */
  public IRootResource getResource(URL map) throws IOException {
    IResourceBuilder builder = null;
    // Decide which type of builder to use.
    if (detectionType == DetectionType.FILE_SYSTEM) {
      builder = new FileSystemResourceBuilder();
    } else if (detectionType == DetectionType.MAP_STRUCTURE) {
      builder = new MapStructureResourceBuilder();
    } else {
      throw new IllegalStateException("Unhandled detection type");
    }
    
    return builder.wrap(map);
  }
}
