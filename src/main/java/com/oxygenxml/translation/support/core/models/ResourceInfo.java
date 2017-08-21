package com.oxygenxml.translation.support.core.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * 
 * This class makes on abject of type Resource Info.
 *
 */
@XmlRootElement(name = "info-resource")
@XmlAccessorType (XmlAccessType.FIELD)
public class ResourceInfo {
	/**
	 * md5 The unique generated hash.
	 */
	@XmlElement(name = "md5")
	private String md5;
	
	/**
	 * relativePath The path relative to the entry point. For example:
   * Entry point: c:testIteration
   * Relative path: dir1/test.txt
   * Relative path: dir2/test.txt
   * Relative path: dir2/dir21/test.txt
   * 
	 */
	@XmlElement(name = "relativePath")
	private String relativePath;
	
	public ResourceInfo(){
		
	}
	
	public ResourceInfo(String md5, String relativePath){
		this.md5 = md5;
		this.relativePath = relativePath;
	}
	
	public ResourceInfo(String relativePath){
    this.relativePath = relativePath;
  }

	
	public String getMd5() {
		return md5;
	}

	
	public void setMd5(String md5) {
		this.md5 = md5;
	}


	public String getRelativePath() {
		return relativePath;
	}

	
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	@Override
	public String toString() {
		return "ResourceInfo [md5=" + md5 + ", relativePath=" + relativePath + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ResourceInfo)) {
			return false;
		}
		ResourceInfo res = (ResourceInfo) obj;
		return this.getMd5().equals(res.getMd5()) && this.getRelativePath().equals(res.getRelativePath());
	}
	
}
