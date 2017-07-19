package com.oxygenxml.translation.support.core.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
	 * list Gathers the generated ResourceInfo objects.
	 */
	@XmlElement(name = "info-resource")
	private ArrayList<ResourceInfo> list;
	
	
	public ArrayList<ResourceInfo> getList() {
		return list;
	}
	
	public void setList(ArrayList<ResourceInfo> list) {
		this.list = list;
	}
	
	public InfoResources(){
		
	}
	
	public InfoResources(ArrayList<ResourceInfo> list) {
		this.list = list;
	}
		
}
