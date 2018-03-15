package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.support.core.resource.FileSystemResourceBuilder;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.ArchiveBuilder;
import com.oxygenxml.translation.ui.StoppedByUserException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for applying a package. basically copying the translated resources over a target
 * directory.
 */
public class CopyDirTest {

  /**
   * Copies some resources over an empty directory.
   * 
   * @throws Exception If it fails.
   */
  @Test
  public void copyDir_to_emptyDir_Test() throws Exception {
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

  @Test
  public void copyDir_to_notEmptyDir_Test() throws IOException, StoppedByUserException, NoSuchAlgorithmException {
    File sourceLocation = TestUtil.getPath("copyDir-Test");
    File targetLocation = TestUtil.getPath("targetCopyDir-Test");

    new ArchiveBuilder(null).copyDirectory(sourceLocation, targetLocation, 0, true);

    String expectedResult = "file1.txt                   66e17a2f53f0a5c2b4599ef525b9b150\n" +
        "test.txt                    00b06ff6801dc0a3ffa565f77d92052a\n" +
        "testFile.txt                f573c260c18ec17d95ac48baff76db9a\n" +
        "toCopy/dir1/dir2/file3.txt  efd89f6fb31003d61d62835be048ce86\n" +
        "toCopy/dir1/file2.txt       df182645e551073d3505dced3455b694\n" +
        "toCopy/file1.txt            bc8dc04e5c01a166ba05cc5c2e09b261\n" +
        "";
    
    Set<URL> visited = new HashSet<URL>();
    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    new ChangePackageGenerator(null).computeResourceInfo(
        new FileSystemResourceBuilder().wrapDirectory(targetLocation), actualResult, visited);

    Assert.assertEquals(expectedResult, TestUtil.dump(actualResult));
  }

}
