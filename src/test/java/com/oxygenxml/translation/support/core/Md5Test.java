package com.oxygenxml.translation.support.core;


import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.ChangeDetector;

public class Md5Test {

	@Test
	public void testMd5_File() throws Exception {
		URL resource = getClass().getClassLoader().getResource("md5Test.txt");
		System.out.println(resource.getPath());
		File file = new File(resource.getPath());
		
		
		
		String cksum = ChangeDetector.generateMD5(file);
		System.out.println(cksum);
		Assert.assertEquals("c439e0812a8e0a5434bffa6f063d4bec", cksum);
		
		
		resource = getClass().getClassLoader().getResource("generateMD5-test.txt");

		file = new File(resource.getPath());

		//FileCheckSum cksumDigester=new FileCheckSum();
		cksum = ChangeDetector.generateMD5(file);
		System.out.println(cksum);
		Assert.assertEquals("95bcd2d5a06b5f63b84551ddd8ec1483", cksum);


	}
	
}
