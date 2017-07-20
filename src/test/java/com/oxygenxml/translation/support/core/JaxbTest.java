package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.models.InfoResources;
import com.oxygenxml.translation.support.core.models.ResourceInfo;

public class JaxbTest {

	private URL resource = getClass().getClassLoader().getResource("JAXB-test");

	private File rootDir = new File(resource.getPath());


	@Test
	public void testMarshaller() throws JAXBException, IOException {

		InfoResources info = new InfoResources();
		ResourceInfo resource = new  ResourceInfo();
		ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();

		resource.setMd5("12345");
		resource.setRelativePath("dir1/test.txt");

		list.add(resource);

		info.setList(list);

		PackageBuilder.storeMilestoneFile(info, rootDir);

	    String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		+"<resources>\n"
		+"    <info-resource>\n"
		+"        <md5>12345</md5>\n"
		+"        <relativePath>dir1/test.txt</relativePath>\n"
		+"    </info-resource>\n"
		+"</resources>\n";

	    
	  String actualResult = IOUtils.toString(new FileInputStream(new File(rootDir, "milestone.xml")), "utf-8");
	  
		Assert.assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testUnmarshaller() throws JAXBException, IOException {

		ArrayList<ResourceInfo> list = PackageBuilder.loadMilestoneFile(rootDir);
		String dump = DumpUtil.dump(list);
		Assert.assertEquals("dir1/test.txt  12345\n", dump);;
	}

}
