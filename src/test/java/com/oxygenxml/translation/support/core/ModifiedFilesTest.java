package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.ChangeDetector;
import com.oxygenxml.translation.support.core.models.ResourceInfo;

public class ModifiedFilesTest {

	@Test
	public void test_Modifiedfiles() throws NoSuchAlgorithmException, FileNotFoundException, JAXBException, IOException {
		
		//File rootDir = new File("src/test/resources/modifiedFiles-Test");		
		
		URL resource = getClass().getClassLoader().getResource("modifiedFiles-Test");

		File rootDir = new File(resource.getPath());
		
		ArrayList<ResourceInfo> actualResult = ChangeDetector.generateModifiedResources(rootDir);
		
		ArrayList<ResourceInfo> expectedResult = new ArrayList<ResourceInfo>();
		ResourceInfo first = new ResourceInfo("555b6a76c37746c6f2a4efd07874f01d" , "new.txt");
		
		expectedResult.add(first);
		
		Assert.assertEquals(DumpUtil.dump(expectedResult), DumpUtil.dump(actualResult));
		
		//Load and assert the milestone content.
		ArrayList<ResourceInfo> actualResourcesFromMilestone = ChangeDetector.loadMilestoneFile(rootDir);
		
		ArrayList<ResourceInfo> expectedResultMilestone = new ArrayList<ResourceInfo>();
		expectedResultMilestone.add(new ResourceInfo("0330c493e6a1efda89242d99195c6eca" , "dir1/dir1.1/test.txt"));
		expectedResultMilestone.add(new ResourceInfo("f4fbf41346cdf2995d37388c6331a184" , "dir1/new1.txt"));
		expectedResultMilestone.add(new ResourceInfo("6d7e8058723731106992e9e54e91e478" , "dir2/new2.txt"));
		
		Assert.assertEquals(DumpUtil.dump(expectedResultMilestone), DumpUtil.dump(actualResourcesFromMilestone));
		
	}
	
}
