package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.PackageGeneratorUtil;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Test the zipping functionality.
 * @author adrian_sorop
 */
public class ZipTest extends TranslationPackageTestBase {
  
  /**
   * <p><b>Description:</b> The root map might not be in the top folder.</p>
   * <p><b>Bug ID:</b> EXM-41055</p>
   *
   * @author adrian_sorop
   * @throws Exception
   */
  public void testZipWorker() throws Exception {
    
    File rootDir = TestUtil.getPath("issue-9-full");
    File rootMap = new File(rootDir, "root/THE_ROOT.ditamap");
    File rootParentFile = rootMap.getParentFile();
    final URL rootMapURL = rootMap.toURI().toURL();
    final File saveLocation = new File(rootParentFile, "archive.zip");
    
    final List<ResourceInfo> modifiedResources = generatesModifiedFilesUsingMapStructureBuilder(rootMapURL);
    
    Future<?> future = PackageGeneratorUtil.createPackage(
        rootMapURL, 
        saveLocation, 
        saPluginWorkspaceMock, 
        false, 
        modifiedResources, 
        false);
    
    // Wait for completion.
    future.get();
    
    URL archive = getClass().getClassLoader().getResource("issue-9-full/root/archive.zip");
    File file = new File(archive.getPath());
    assertTrue("Unable to generate archive: " + file.getAbsolutePath(), file.exists());
    
    List<String> zipContent = new ArrayList<String>();
    ZipFile zipFile = new ZipFile(file);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();

    while(entries.hasMoreElements()){
      ZipEntry entry = entries.nextElement();
      zipContent.add(entry.getName());
    }
    zipFile.close();
    
    Collections.sort(zipContent);
    
    // Assert the content of the archive.
    assertEquals(
        "["
        + "from_ref/, "               /*folder*/
        + "from_ref/a.dita, "         /*a file referred in map*/ 
        + "refMap.ditamap, "          /*a referred map*/
        
        + "root/, "                   /*folder. Contains the root map and other sub-folders*/
        + "root/THE_ROOT.ditamap, "   /*the root map*/
        + "root/from_root/, "         /*sub-folder*/
        + "root/from_root/topic1.dita"/*a file referred in the root map*/
        + "]",
        zipContent.toString());
  }

  /**
   * <p><b>Description:</b> The root map might not be in the top folder.</p>
   * <p><b>Bug ID:</b> EXM-41055</p>
   *
   * @author adrian_sorop
   * @throws Exception
   */
  public void testZipWorker_2() throws Exception {
    
    File rootDir = TestUtil.getPath("issue-9-full-sample-2");
    File rootMap = new File(rootDir, "translation/maps/map.ditamap");
    File rootParentFile = rootMap.getParentFile();
    
    final URL rootMapURL = rootMap.toURI().toURL();
    final File saveLocation = new File(rootParentFile, "archive.zip");
    
    List<ResourceInfo> modifiedResources = generatesModifiedFilesUsingMapStructureBuilder(rootMapURL);
    
    Future<?> future = PackageGeneratorUtil.createPackage(
        rootMapURL, 
        saveLocation, 
        saPluginWorkspaceMock, 
        false, 
        modifiedResources, 
        false);
    
    // Wait for completion.
    future.get();
    
    URL archive = getClass().getClassLoader().getResource("issue-9-full-sample-2/translation/maps/archive.zip");
    File file = new File(archive.getPath());
    assertTrue("Unable to generate archive: " + file.getAbsolutePath(), file.exists());
    
    List<String> zipContent = new ArrayList<String>();
    ZipFile zipFile = new ZipFile(file);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
  
    while(entries.hasMoreElements()){
      ZipEntry entry = entries.nextElement();
      zipContent.add(entry.getName());
    }
    zipFile.close();
    
    Collections.sort(zipContent);
    
    // Assert the content of the archive.
    assertEquals(
        "["
        + "topics/, "
        + "topics/t1.dita, "
        + "topics/t2.dita"
        + "]",
        zipContent.toString());
  }

  /**
   * <p><b>Description:</b> Zip entries's names should contain white spaces.</p>
   * <p><b>Bug ID:</b> EXM-41259</p>
   *
   * @author adrian_sorop
   * @throws Exception
   */
  public void testZipWorker_3() throws Exception {
    
    File rootDir = TestUtil.getPath("issue-9-1-file");
    File rootMap = new File(rootDir, "translation/ma ps/map.ditamap");
    File rootParentFile = rootMap.getParentFile();
    
    final URL rootMapURL = rootMap.toURI().toURL();
    final File saveLocation = new File(rootParentFile, "archive.zip");
    
    final List<ResourceInfo> modifiedResources = generatesModifiedFilesUsingMapStructureBuilder(rootMapURL);
    
    Future<?> future = PackageGeneratorUtil.createPackage(
        rootMapURL, 
        saveLocation, 
        saPluginWorkspaceMock, 
        false, 
        modifiedResources, 
        false);
    
    // Wait for completion.
    future.get();
    
    URL archive = getClass().getClassLoader().getResource("issue-9-1-file/translation/ma ps/archive.zip");
    File file = new File(archive.toURI());
    assertTrue("Unable to generate archive: " + file.getAbsolutePath(), file.exists());
    
    List<ZipEntry> zipContent = new ArrayList<ZipEntry>();
    ZipFile zipFile = new ZipFile(file);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
  
    while(entries.hasMoreElements()){
      ZipEntry entry = entries.nextElement();
      zipContent.add(entry);
    }
    zipFile.close();
    
    // Assert the content of the archive.
    assertEquals(
        "[to pics/, to pics/white space.dita]",
        zipContent.toString());
  }

  /**
   * <p><b>Description:</b> Pack all in "Create Modified Files Package".</p>
   *
   * @throws Exception
   */
  public void testZipWorker_4() throws Exception {

    File rootDir = TestUtil.getPath("issue-9-pack-all/translation");
    File rootMap = new File(rootDir, "ma ps/map.ditamap");

    final URL rootMapURL = rootMap.toURI().toURL();
    final File saveLocation = new File(rootDir.getParentFile(), "archive.zip");

    Future<?> future = PackageGeneratorUtil.createPackage(
        rootMapURL, 
        saveLocation, 
        saPluginWorkspaceMock,
        // Pack all
        true, 
        null, 
        false);

    // Wait to complete
    future.get();


    assertTrue(saveLocation.exists());

    URL archive = getClass().getClassLoader().getResource("issue-9-pack-all/archive.zip");
    File file = new File(archive.toURI());
    assertTrue("Unable to generate archive: " + file.getAbsolutePath(), file.exists());

    List<String> zipContent = new ArrayList<String>();
    ZipFile zipFile = new ZipFile(file);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();

    while(entries.hasMoreElements()){
      ZipEntry entry = entries.nextElement();
      zipContent.add(entry.getName());
    }
    zipFile.close();

    Collections.sort(zipContent);

    // Assert the content of the archive.
    assertEquals(
        "["
            + "ma ps/, "
            + "ma ps/map.ditamap, "
            + "to pics/, "
            + "to pics/white space.dita"
        + "]",
         zipContent.toString());
  }

  
}
