package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.resource.FileSystemResourceBuilder;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;

/**
 * Tests for applying a package. basically copying the translated resources over a target
 * directory.
 */
public class CopyDirTest extends TranslationPackageTestBase {
  
  /**
   * Copies some resources over an empty directory.
   * 
   * @throws Exception If it fails.
   */
  public void testCopyDirToEmptyDir() throws Exception {
    File sourceLocation = TestUtil.getPath("copyDir-Test");
    File targetLocation = new File(sourceLocation.getParentFile(), "targetEmptyCopyDir-Test");

    new ArchiveBuilder(null).copyDirectory(sourceLocation, targetLocation, 0, true);

    ArrayList<ResourceInfo> expectedResult = new ArrayList<ResourceInfo>();
    FileSystemResourceBuilder builder = new FileSystemResourceBuilder();
    Set<URL> visited = new HashSet<URL>();
    new ChangePackageGenerator(null).computeResourceInfo(
        builder.wrapDirectory(sourceLocation), 
        expectedResult, visited);

    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    visited.clear();
    new ChangePackageGenerator(null).computeResourceInfo(
        builder.wrapDirectory(targetLocation), actualResult, visited);

    Assert.assertEquals(TestUtil.dump(expectedResult), TestUtil.dump(actualResult));
  }
  
  /**
   * Copy files to a non empty dir.
   */
  public void testCopyDirToNotEmptyDir() throws IOException, StoppedByUserException, NoSuchAlgorithmException {
    File sourceLocation = TestUtil.getPath("copyDir-Test");
    File targetLocation = TestUtil.getPath("targetCopyDir-Test");

    new ArchiveBuilder(null).copyDirectory(sourceLocation, targetLocation, 0, true);

    String expectedResult = 
        "file1.txt                   643b63c75d1205597e095f1e0a04fac2\n" +
        "test.txt                    de57058e47c4c0c9c1c8fcdabc1a5142\n" +
        "testFile.txt                f573c260c18ec17d95ac48baff76db9a\n" +
        "toCopy/dir1/dir2/file3.txt  ff94178c4ab70f1d64539a431a6d6d83\n" +
        "toCopy/dir1/file2.txt       3397df370cbcf15d92055b5f49612e98\n" +
        "toCopy/file1.txt            540b1d9d17d7c48a59e20cdce151e4ee\n" +
        "";
    
    Set<URL> visited = new HashSet<URL>();
    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    new ChangePackageGenerator(null).computeResourceInfo(
        new FileSystemResourceBuilder().wrapDirectory(targetLocation), actualResult, visited);

    Assert.assertEquals(expectedResult, TestUtil.dump(actualResult));
  }

}
