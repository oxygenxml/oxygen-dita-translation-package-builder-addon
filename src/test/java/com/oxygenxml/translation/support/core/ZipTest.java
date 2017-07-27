package com.oxygenxml.translation.support.core;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.util.ArchiveBuilder;

public class ZipTest {
	/**
	 * We create a zip with all the modified files of a directory
	 * Then unzip it and compare the relative paths of the modified files
	 * @throws Exception 
	 */
	
	URL resource = getClass().getClassLoader().getResource("packageZip-Test");

	File rootDir = new File(resource.getPath());
	File tempDir = new File(rootDir.getParentFile(), "tempZip");
	
	@Test
	public void testPackageUnzip() throws Exception {
		File packageLocation = new File(tempDir, "changedFiles.zip");
		
		/* 
		 * Generate the milestone for rootDir.
		 */
		new PackageBuilder().generateChangedFilesPackage(rootDir, packageLocation);
		
		ArrayList<String> actualResults = new ArchiveBuilder().unzipDirectory(packageLocation , tempDir);

		ArrayList<String> expectedResults = new ArrayList<String>();
		expectedResults.add("testGenerate/newAdded.txt");
		expectedResults.add("testIteration/dir1/md5.txt");
		
		Assert.assertEquals(expectedResults, actualResults);
		System.out.println(actualResults);
		
	}
	
	//Delete the "temp" dir
	@After
	public void deleteTempDir() throws IOException{
		FileUtils.deleteDirectory(tempDir);
	}

}
