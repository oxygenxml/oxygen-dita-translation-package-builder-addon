package com.oxygenxml.translation.support.core.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * Creates the object stored in the comboBox from the ReportDialog.
 */
@XmlRootElement(name = "comboItem")
@XmlAccessorType (XmlAccessType.FIELD)
public class ComboItem {
  /**
   * choosedPath The selected package location from the comboBox.
   */
  @XmlElement(name = "path")
  private String choosedPath;
  
  public ComboItem(String path) {
    this.choosedPath = path;
  }
  public ComboItem() {  }
  
  public String getPath() {
    return choosedPath;
  }
  public void setPath(String path) {
    this.choosedPath = path;
  }
}
