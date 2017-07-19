package com.oxygenxml.translation.support.core;

import java.util.ArrayList;

import com.oxygenxml.translation.support.core.models.ResourceInfo;

public class DumpUtil {
	
	public static String dump(ArrayList<ResourceInfo> list) {

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

}
