package com.oxygenxml.translation.support.storage;

import java.io.File;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * This class makes an object that contains a list with Resource Info objects.
 *
 */
@XmlRootElement(name = "resources")
@XmlAccessorType (XmlAccessType.FIELD)
public class InfoResources {
  /**
   * The date and time of the last milestone creation.
   */
  @XmlAttribute(name = "date")
  private Date milestoneCreation;
  
  /**
   * The root folder of the current ditaMap.
   */
  @XmlAttribute(name = "rootFolder")
  private File rootFolder;
  
  
  public File getRootFolder() {
    return rootFolder;
  }

  public void setRootFolder(File rootFolder) {
    this.rootFolder = rootFolder;
  }

  
	/**
	 * list Gathers the generated ResourceInfo objects.
	 */
	@XmlElement(name = "info-resource")
	private List<ResourceInfo> list;
	
	public Date getMilestoneCreation() {
    return milestoneCreation;
  }
  public void setMilestoneCreation(Date milestoneCreation) {
    this.milestoneCreation = milestoneCreation;
  }
	
	public List<ResourceInfo> getList() {
		return list;
	}
	
	public void setList(List<ResourceInfo> list) {
		this.list = list;
	}
	
	public InfoResources(List<ResourceInfo> list, Date milestoneCreation) {
    this.list = list;
    this.milestoneCreation = milestoneCreation;
  }
	
	public InfoResources(){	}
	
	public InfoResources(List<ResourceInfo> list) {
		this.list = list;
	}
		
}
