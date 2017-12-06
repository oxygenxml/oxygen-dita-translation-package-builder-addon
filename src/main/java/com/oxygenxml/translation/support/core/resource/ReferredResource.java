package com.oxygenxml.translation.support.core.resource;

import java.net.URL;

public class ReferredResource {
  
  /**
   * URL location of the referred file.
   */
  private URL location;
  
  /**
   * <code>true</code> if the file is parsable.
   */
  private boolean xmlParsable;
  
  /**
   * Default constructor.
   */
  public ReferredResource() {
  }
  
  /**
   * Create a new object. 
   * 
   * @param location The location of the resource.
   * @param isNonDita <code>true</code> if the file is non DITA 
   * and should not be parsed as XML file.
   */
  public ReferredResource(URL location, boolean isNonDita) {
    this.location = location;
    this.xmlParsable = isNonDita;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ReferredResource)) {
      return false;
    }
    ReferredResource res = (ReferredResource)obj;
    return this.location.equals(res.getLocation()) && 
        // DITA res (parsable XML resource)
        this.xmlParsable == res.nonDita();
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
   * @return <code>true</code> if the resource is parsable.
   */
  public boolean nonDita() {
    return xmlParsable;
  }

  /**
   * @param parsable <code>true</code> if file is parsable.
   */
  public void setIsDita(boolean parsable) {
    this.xmlParsable = parsable;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ReferredResource [location=" + location + ", isBinary=" + xmlParsable + "]";
  }
  
}
