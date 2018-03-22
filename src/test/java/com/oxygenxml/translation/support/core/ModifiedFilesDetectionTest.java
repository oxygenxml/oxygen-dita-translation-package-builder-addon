package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.resource.FileSystemResourceBuilder;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for detecting the modified files with respect to a saved milestone. 
 */
public class ModifiedFilesDetectionTest {
	@Test
	public void testModifiedfiles() throws NoSuchAlgorithmException, FileNotFoundException, JAXBException, IOException, StoppedByUserException {
		File rootDir = TestUtil.getPath("modifiedFiles-Test");
		IRootResource rootResource = new FileSystemResourceBuilder().wrapDirectory(rootDir);
		
    // Load and assert the milestone content.
		List<ResourceInfo> actualResourcesFromMilestone = MilestoneUtil.loadMilestoneFile(rootResource);
		
		ArrayList<ResourceInfo> expectedResultMilestone = new ArrayList<ResourceInfo>();
		expectedResultMilestone.add(new ResourceInfo("0330c493e6a1efda89242d99195c6eca" , "dir1/dir1.1/test.txt"));
		expectedResultMilestone.add(new ResourceInfo("f4fbf41346cdf2995d37388c6331a184" , "dir1/new1.txt"));
		expectedResultMilestone.add(new ResourceInfo("6d7e8058723731106992e9e54e91e478" , "dir2/new2.txt"));
		
		Assert.assertEquals(
		    TestUtil.dump(expectedResultMilestone), 
		    TestUtil.dump(actualResourcesFromMilestone));
		
		// Test how the change files are detected.
    List<ResourceInfo> actualResult = new ChangePackageGenerator(null).collectModifiedResources(rootResource);
    
    ArrayList<ResourceInfo> expectedResult = new ArrayList<ResourceInfo>();
    expectedResult.add(new ResourceInfo("555b6a76c37746c6f2a4efd07874f01d" , "new.txt"));
    
    Assert.assertEquals(TestUtil.dump(expectedResult), 
        TestUtil.dump(actualResult));
	}

}
