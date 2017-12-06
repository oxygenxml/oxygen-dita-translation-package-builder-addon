package com.oxygenxml.translation.support.core.resource;

import java.net.URL;

public class ReferredResource {
  
  /**
   * URL location of the referred file.
   */
  private URL location;
  
  /**
   * <code>true</code> if the file is binary.
   */
  private boolean isBinary;
  
  /**
   * Default constructor.
   */
  public ReferredResource() {
    
  }
  
  /**
   * Create a new object. 
   * 
   * @param location The location of the resource.
   * @param isBinary <code>true</code> if the file is binary
   */
  public ReferredResource(URL location, boolean isBinary) {
    this.location = location;
    this.isBinary = isBinary;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ReferredResource)) {
      return false;
    }
    ReferredResource res = (ReferredResource)obj;
    return this.location.equals(res.getLocation()) && 
        // Location
        this.hashCode() == res.hashCode() &&
        // binary res
        this.isBinary == res.isBinary();
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
   * @param location the location to set
   */
  public void setLocation(URL location) {
    this.location = location;
  }

  /**
   * @return the isBinary
   */
  public boolean isBinary() {
    return isBinary;
  }

  /**
   * @param isBinary the isBinary to set
   */
  public void setBinary(boolean isBinary) {
    this.isBinary = isBinary;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ReferredResource [location=" + location + ", isBinary=" + isBinary + "]";
  }
  
}
