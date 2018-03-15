package com.oxygenxml.translation.support.storage;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * This class makes an object that contains a list with ComboItem objects.
 * 
 * @author Bivolan Dalina
 */
@XmlRootElement(name = "historyItems")
@XmlAccessorType (XmlAccessType.FIELD)
public class ComboHistory {
  /**
   * entries Gathers the selected package locations from the comboBox.
   */
  @XmlElement(name = "comboItem")
  private List<ComboItem> entries;
  
  /**
   * Default constructor.
   */
  public ComboHistory(){
    // Nothing
  }
  
  /**
   * @param path  A list of the objects stored in the comboBox.
   */
  public ComboHistory(List<ComboItem> path){
    this.entries = path;
  }
  
  /**
   * @return  A list of the objects stored in the comboBox.
   */
  public List<ComboItem> getEntries() {
    return entries;
  }
  
  /**
   * @param path  A list of the objects stored in the comboBox.
   */
  public void setEntries(List<ComboItem> path) {
    this.entries = path;
  }
}
