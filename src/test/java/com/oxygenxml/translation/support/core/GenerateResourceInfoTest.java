package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.CustomResourceIteration;
import com.oxygenxml.translation.support.util.PathOption;
import com.oxygenxml.translation.ui.StoppedByUserException;

public class GenerateResourceInfoTest {
  private PathOption pathOption = new PathOption();
  
	@Test
	public void testGenerateRelativePaths() throws NoSuchAlgorithmException, IOException, StoppedByUserException {
		File dirPath = pathOption.getPath("testIteration-ResourceInfoTest");

		ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
		PackageBuilder.computeResourceInfo(new CustomResourceIteration(), null, new File(dirPath, "test.txt"), new Stack<String>(), list, true);
		
		Assert.assertEquals(
				"dir1/md5.txt      1308e502a17d62d0585d1487228b204c\n" + 
				"dir1/md5_no2.txt  521304ca436443d97ccf68ee919c03b3\n" + 
				"dir2/md5.txt      55047487acf9f525244b12cff4bfc49c\n" + 
				"dir2/md5_no2.txt  5c24a78aec732e9626a4a7114efd98b1\n" + 
				"", DumpUtil.dump(list));
		
	}
	
	@Test
	public void testGenerateRelativePaths_2() throws NoSuchAlgorithmException, FileNotFoundException, IOException, StoppedByUserException {
		File dirPath = pathOption.getPath("testGenerate-ResourceInfoTest");

		ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
		
		PackageBuilder.computeResourceInfo(new CustomResourceIteration(), null, new File(dirPath, "test.txt"), new Stack<String>(), list, true);
		
		String expectedResult = "md5.txt      f5e45edee5ee0e2dffe9fbe6a736ab02\n" + 
								"md5_no2.txt  521304ca436443d97ccf68ee919c03b3\n";
		
		Assert.assertEquals(expectedResult, DumpUtil.dump(list));
		
		System.out.println(DumpUtil.dump(list));
				
	}
}
