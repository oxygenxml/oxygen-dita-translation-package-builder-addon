package com.oxygenxml.translation.support.core;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

import com.oxygenxml.translation.support.core.models.ResourceInfo;

public class TestUtil {
	/**
	 * @param list A list with ResourceInfo objects.
	 * 
	 * @return A sorted and aligned string.
	 */
  public static String dump(List<ResourceInfo> list) {
    list.sort(new Comparator<Object>() {
      public int compare(Object ListOne, Object ListTwo) {
        return ((ResourceInfo)ListOne).getRelativePath().compareTo(((ResourceInfo)ListTwo).getRelativePath());
      }
    });
	  
		StringBuilder b =new StringBuilder();
		int maxLength = 0;
		for(int j = 0;j < list.size();j++){
			int length = list.get(j).getRelativePath().length();
			if(maxLength < length){
				maxLength = length;
			}
		}
		for(int i=0;i<list.size();i++){
			String relativePath = list.get(i).getRelativePath();
			b.append(relativePath);
			for(int k=0;k <= maxLength - relativePath.length() + 1;k++){
				b.append(" ");
			}
			b.append(list.get(i).getMd5());
			b.append("\n");
		}
		
		return b.toString();
	}
  
  /**
   * Gets the needed resource.
   * 
   * @param dirName The name of the resource we need.
   * 
   * @return The file/directory specified by dirName.
   */
  public static File getPath(String dirName){
    URL resource = TestUtil.class.getClassLoader().getResource(dirName);
    return new File(resource.getPath());
  }
}
