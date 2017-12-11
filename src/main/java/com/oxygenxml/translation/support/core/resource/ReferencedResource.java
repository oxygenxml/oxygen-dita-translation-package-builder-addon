package com.oxygenxml.translation.support.core.resource;

import java.net.URL;

public class ReferencedResource {
  
  /**
   * URL location of the referred file.
   */
  private URL location;
  
  /**
   * <code>true</code> if the file is DITA.
   */
  private boolean isDITAResource;
  
  /**
   * Create a new object. 
   * 
   * @param location The location of the resource.
   * @param isDITAReference <code>true</code> if the current resource is DITA.
   */
  public ReferencedResource(URL location, boolean isDITAReference) {
    this.location = location;
    this.isDITAResource = isDITAReference;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ReferencedResource)) {
      return false;
    }
    ReferencedResource res = (ReferencedResource)obj;
    return this.location.equals(res.getLocation()) && 
        // DITA res (parsable XML resource)
        this.isDITAResource == res.isDITAResource();
  }
  
  @Override
  public int hashCode() {
    return this.location.hashCode() + 42;
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /**
   * @return <code>true</code> if the resource is DITA.
   */
  public boolean isDITAResource() {
    return isDITAResource;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ReferredResource [location=" + location + ", isDITAResource=" + isDITAResource + "]";
  }
  
}
