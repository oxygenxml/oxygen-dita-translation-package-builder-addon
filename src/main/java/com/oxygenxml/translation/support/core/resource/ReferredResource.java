package com.oxygenxml.translation.support.core.resource;

import java.net.URL;

public class ReferredResource {
  
  /**
   * URL location of the referred file.
   */
  private URL location;
  
  /**
   * <code>true</code> if the file is parsable using SAX.
   */
  private boolean parsable;
  
  /**
   * Create a new object. 
   * 
   * @param location The location of the resource.
   * @param parsable <code>true</code> if file can be parsed as XML. 
   */
  public ReferredResource(URL location, boolean parsable) {
    this.location = location;
    this.parsable = parsable;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ReferredResource)) {
      return false;
    }
    ReferredResource res = (ReferredResource)obj;
    return this.location.equals(res.getLocation()) && 
        // DITA res (parsable XML resource)
        this.parsable == res.isParsable();
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
   * @return <code>true</code> if the resource is parsable as XML.
   */
  public boolean isParsable() {
    return parsable;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ReferredResource [location=" + location + ", parsable=" + parsable + "]";
  }
  
}
