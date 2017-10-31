package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.resource.FileSystemResourceBuilder;
import com.oxygenxml.translation.support.storage.ResourceInfo;
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

    Set<URL> visited = new HashSet<URL>();
    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
    new ChangePackageGenerator().computeResourceInfo(
        new FileSystemResourceBuilder().wrapDirectory(dirPath), list, visited);

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

    Set<URL> visited = new HashSet<URL>();
    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
    new ChangePackageGenerator().computeResourceInfo(
        new FileSystemResourceBuilder().wrapDirectory(dirPath), list, visited);

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
    
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator();
    Set<URL> visited = new HashSet<URL>();
    ArrayList<ResourceInfo> milestoneInfo = new ArrayList<ResourceInfo>();
    packageBuilder.computeResourceInfo(
        new FileSystemResourceBuilder().wrapDirectory(rootDir), milestoneInfo, visited);

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
