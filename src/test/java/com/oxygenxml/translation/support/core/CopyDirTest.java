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
    
    String expectedResult = "file1.txt                   66e17a2f53f0a5c2b4599ef525b9b150\n" +
"test.txt                    00b06ff6801dc0a3ffa565f77d92052a\n" +
"testFile.txt                f573c260c18ec17d95ac48baff76db9a\n" +
"toCopy/dir1/dir2/file3.txt  efd89f6fb31003d61d62835be048ce86\n" +
"toCopy/dir1/file2.txt       df182645e551073d3505dced3455b694\n" +
"toCopy/file1.txt            bc8dc04e5c01a166ba05cc5c2e09b261\n" +
"";
 
    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    PackageBuilder.computeResourceInfo(targetLocation, new Stack<String>(), actualResult);
    
    Assert.assertEquals(expectedResult, DumpUtil.dump(actualResult));
  }

}
