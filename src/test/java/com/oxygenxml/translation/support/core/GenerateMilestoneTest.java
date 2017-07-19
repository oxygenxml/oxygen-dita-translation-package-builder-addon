package com.oxygenxml.translation.support.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class GenerateMilestoneTest {

	@Test
	public void test_ChangeMilestone() throws NoSuchAlgorithmException, FileNotFoundException, IOException, JAXBException {
		
		URL resource = getClass().getClassLoader().getResource("generateMilestone-test");
		System.out.println(resource.getPath());
		File rootDir = new File(resource.getPath());

		
		File file = ChangeDetector.generateChangeMilestone(rootDir);
		
		
		String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
				"<resources>\n" + 
				"    <info-resource>\n" + 
				"        <md5>1ea64c493f5278ec6ee5aaa7a35c77f6</md5>\n" + 
				"        <relativePath>testGenerate/md5.txt</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>80c28c189a32e6e60f9e43010bb10a9e</md5>\n" + 
				"        <relativePath>testGenerate/md5_no2.txt</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>1ea64c493f5278ec6ee5aaa7a35c77f6</md5>\n" + 
				"        <relativePath>testIteration/dir1/md5.txt</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>521304ca436443d97ccf68ee919c03b3</md5>\n" + 
				"        <relativePath>testIteration/dir1/md5_no2.txt</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>55047487acf9f525244b12cff4bfc49c</md5>\n" + 
				"        <relativePath>testIteration/dir2/md5.txt</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>5c24a78aec732e9626a4a7114efd98b1</md5>\n" + 
				"        <relativePath>testIteration/dir2/md5_no2.txt</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>33095daf2f434bf50c134e236a1b0a57</md5>\n" + 
				"        <relativePath>tests/GenerateDescriptorTest.class</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>f322b478d747815c097dafc9f5541218</md5>\n" + 
				"        <relativePath>tests/JaxbTest.class</relativePath>\n" + 
				"    </info-resource>\n" + 
				"    <info-resource>\n" + 
				"        <md5>5a97486ecdad6b48740a42e04129e4d8</md5>\n" + 
				"        <relativePath>tests/Md5Test.class</relativePath>\n" + 
				"    </info-resource>\n" + 
				"</resources>\n" + 
				"";
		
		String actualResult = IOUtils.toString(new FileInputStream(new File(file.getPath())), "utf-8");
		
		Assert.assertEquals(expectedResult, actualResult);
	}

}
