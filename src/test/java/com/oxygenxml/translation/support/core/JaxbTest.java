package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.resource.FileSystemResourceBuilder;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.storage.InfoResources;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.ui.StoppedByUserException;

import edu.emory.mathcs.backport.java.util.Arrays;

public class JaxbTest {
	private File rootDir = TestUtil.getPath("JAXB-test");

	@Test
	public void testMarshaller() throws JAXBException, IOException, StoppedByUserException {

		InfoResources info = new InfoResources();
		ResourceInfo resource = new  ResourceInfo();

		resource.setMd5("12345");
		resource.setRelativePath("dir1/test.txt");

		@SuppressWarnings("unchecked")
    List<ResourceInfo> list = Arrays.asList(new ResourceInfo[] {resource});
	  info.setList(list);

	  File milestone = new File(rootDir, "unknown_translation_milestone.xml");
		MilestoneUtil.storeMilestoneFile(info, milestone);

	    String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		+"<resources>\n"
		+"    <info-resource>\n"
		+"        <md5>12345</md5>\n"
		+"        <relativePath>dir1/test.txt</relativePath>\n"
		+"    </info-resource>\n"
		+"</resources>\n";

	    
    String actualResult = IOUtils.toString
	      (new FileInputStream(milestone), "utf-8");
	  
		Assert.assertEquals(expectedResult, actualResult);
	}

	/**
	 * Tests the milestone file loading.
	 *  
	 *  @throws Exception If it fails.
	 */
	@Test
	public void testUnmarshaller() throws Exception {
	  File file = new File(rootDir, "JAXB-test");
	  IRootResource rootResource = new FileSystemResourceBuilder().wrapDirectory(file);
		List<ResourceInfo> list = MilestoneUtil.loadMilestoneFile(rootResource);
		String dump = TestUtil.dump(list);
		Assert.assertEquals("dir1/test.txt  12345\n", dump);;
	}
}
