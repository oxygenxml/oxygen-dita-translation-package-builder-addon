package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.ui.StoppedByUserException;

public class CopyDirTest {

  @Test
  public void copyDir_to_emptyDir_Test() throws IOException, StoppedByUserException, NoSuchAlgorithmException {
    URL resource1 = getClass().getClassLoader().getResource("copyDir-Test");
    URL resource2 = getClass().getClassLoader().getResource("targetEmptyCopyDir-Test");

    File sourceLocation = new File(resource1.getPath());
    File targetLocation = new File(resource2.getPath());
    
    new ArchiveBuilder().copyDirectory(sourceLocation, targetLocation, new int[] {0}, true);
    
    ArrayList<ResourceInfo> expectedResult = new ArrayList<ResourceInfo>();
    PackageBuilder.computeResourceInfo(sourceLocation, new Stack<String>(), expectedResult);
    
    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    PackageBuilder.computeResourceInfo(targetLocation, new Stack<String>(), actualResult);
    
    Assert.assertEquals(DumpUtil.dump(expectedResult), DumpUtil.dump(actualResult));
  }
  
  @Test
  public void copyDir_to_notEmptyDir_Test() throws IOException, StoppedByUserException, NoSuchAlgorithmException {
    URL resource1 = getClass().getClassLoader().getResource("copyDir-Test");
    URL resource2 = getClass().getClassLoader().getResource("targetCopyDir-Test");

    File sourceLocation = new File(resource1.getPath());
    File targetLocation = new File(resource2.getPath());
    
    new ArchiveBuilder().copyDirectory(sourceLocation, targetLocation, new int[] {0}, true);
    
    String expectedResult = "file1.txt                   24d8667b1ad00c154ef156f27a5624d6\n" +
"test.txt                    f41b4a7edfd1017687c829e8b435773e\n" +
"testFile.txt                63c002c136d5fc1d496d3bab066a0ce9\n" +
"toCopy/dir1/dir2/file3.txt  f41b4a7edfd1017687c829e8b435773e\n" +
"toCopy/dir1/file2.txt       dfe0e603ca0ef2066762a29778add77b\n" +
"toCopy/file1.txt            4b91e9daccdfb5e7af025e12020756fc\n" +
"";
 
    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    PackageBuilder.computeResourceInfo(targetLocation, new Stack<String>(), actualResult);
    
    Assert.assertEquals(expectedResult, DumpUtil.dump(actualResult));
  }

}
