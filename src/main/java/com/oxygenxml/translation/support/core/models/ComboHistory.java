package com.oxygenxml.translation.support.core.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * This class makes an object that contains a list with ComboItem objects.
 * 
 * @author Bivolan Dalina
 *
 */
@XmlRootElement(name = "historyItems")
@XmlAccessorType (XmlAccessType.FIELD)
public class ComboHistory {
  /**
   * entries Gathers the selected package locations from the comboBox.
   */
  @XmlElement(name = "comboItem")
  private ArrayList<ComboItem> entries;
  
  public ComboHistory(){  }
  public ComboHistory(ArrayList<ComboItem> path){
    this.entries = path;
  }
 
  public ArrayList<ComboItem> getEntries() {
    return entries;
  }

  public void setEntries(ArrayList<ComboItem> path) {
    this.entries = path;
  }
}
