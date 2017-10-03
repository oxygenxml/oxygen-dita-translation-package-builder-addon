package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.FileResourceBuilder;
import com.oxygenxml.translation.ui.StoppedByUserException;

/**
 * Tests for generating the milestone required information based on the file system. 
 */
public class GenerateResourceInfoTest {
  /**
   * Iterates over a directory structure and generates MD5's and relative paths.
   */
  @Test
  public void testGenerateRelativePaths() throws NoSuchAlgorithmException, IOException, StoppedByUserException {
    File dirPath = TestUtil.getPath("testIteration-ResourceInfoTest");

    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
    new PackageBuilder().computeResourceInfo(FileResourceBuilder.wrap(dirPath), list);

    Assert.assertEquals(
        "dir1/md5.txt      1308e502a17d62d0585d1487228b204c\n" + 
            "dir1/md5_no2.txt  521304ca436443d97ccf68ee919c03b3\n" + 
            "dir2/md5.txt      55047487acf9f525244b12cff4bfc49c\n" + 
            "dir2/md5_no2.txt  5c24a78aec732e9626a4a7114efd98b1\n" + 
            "", TestUtil.dump(list));

  }

  /**
   * Iterates over a directory structure and generates MD5's and relative paths.
   */
  @Test
  public void testGenerateRelativePaths_2() throws NoSuchAlgorithmException, FileNotFoundException, IOException, StoppedByUserException {
    File dirPath = TestUtil.getPath("testGenerate-ResourceInfoTest");

    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();

    new PackageBuilder().computeResourceInfo(FileResourceBuilder.wrap(dirPath), list);

    String expectedResult = 
        "md5.txt      f5e45edee5ee0e2dffe9fbe6a736ab02\n" + 
        "md5_no2.txt  521304ca436443d97ccf68ee919c03b3\n";

    Assert.assertEquals(expectedResult, TestUtil.dump(list));
  }

  /**
   * Collects milestone information from a directory structure.
   * 
   * @throws Exception If it fails.
   */
  @Test
  public void testGenerateResourceInfo() throws Exception {
    File rootDir = TestUtil.getPath("generateMilestone-Test");
    
    PackageBuilder packageBuilder = new PackageBuilder();
    
    ArrayList<ResourceInfo> milestoneInfo = new ArrayList<ResourceInfo>();
    packageBuilder.computeResourceInfo(FileResourceBuilder.wrap(rootDir), milestoneInfo);

    ArrayList<ResourceInfo> expectedResult = new ArrayList<ResourceInfo>();
    expectedResult.add(new ResourceInfo("1ea64c493f5278ec6ee5aaa7a35c77f6", "testGenerate/md5.txt"));
    expectedResult.add(new ResourceInfo("80c28c189a32e6e60f9e43010bb10a9e", "testGenerate/md5_no2.txt"));
    expectedResult.add(new ResourceInfo("1ea64c493f5278ec6ee5aaa7a35c77f6", "testIteration/dir1/md5.txt"));
    expectedResult.add(new ResourceInfo("521304ca436443d97ccf68ee919c03b3", "testIteration/dir1/md5_no2.txt"));
    expectedResult.add(new ResourceInfo("55047487acf9f525244b12cff4bfc49c", "testIteration/dir2/md5.txt"));
    expectedResult.add(new ResourceInfo("5c24a78aec732e9626a4a7114efd98b1", "testIteration/dir2/md5_no2.txt"));

    Assert.assertEquals(TestUtil.dump(expectedResult), TestUtil.dump(milestoneInfo));
  }
}
