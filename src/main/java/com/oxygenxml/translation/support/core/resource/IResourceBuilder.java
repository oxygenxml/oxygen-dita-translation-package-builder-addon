package com.oxygenxml.translation.support.core.resource;

import java.io.File;
import java.io.IOException;

/**
 * Builds the resources starting from a given file. 
 */
public interface IResourceBuilder {
  /**
   * Wraps the given root map as a resource which allows iteration over its descendants.
   * 
   * @param map The starting point. The root resource: the map.
   * @param milestone The location where to generate the milestone. If <code>null</code>, the milestone will be generated 
   * next to the map.
   * 
   * @return An iterable resource.
   * 
   * @throws IOException Problems with the given resource.
   */
  IRootResource wrap(ReferencedResource map, File milestone) throws IOException;
}
