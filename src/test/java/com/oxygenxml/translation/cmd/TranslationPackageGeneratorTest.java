package com.oxygenxml.translation.cmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.core.TestUtil;
import com.oxygenxml.translation.support.core.TranslationPackageTestBase;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.util.ArchiveBuilder;

import edu.emory.mathcs.backport.java.util.Collections;
import ro.sync.io.FileSystemUtil;

/**
 * Tests cases for the 3 major use cases:
 * 
 * 1. Generate a milestone file.
 * 2. Create a package for translation.
 * 3. Apply a translation package.
 * 
 * @author alex_jitianu
 */
public class TranslationPackageGeneratorTest extends TranslationPackageTestBase {

  /**
   * <p><b>Description:</b> Tests the entry method that creates a milestone.</p>
   * <p><b>Bug ID:</b> EXM-46006</p>
   *
   * @author alex_jitianu
   *
   * @throws Exception If it fails
   */
  @Test
  public void testGenerateMilestone() throws Exception {
    URL ditaMapURL = new File(TestUtil.getPath("cmd/v1"), "flowers.ditamap").toURI().toURL();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out, true, "UTF-8");
    File milestone = TranslationPackageGenerator.generateMilestone(ditaMapURL, null, ps );
    
    String milestoneContent = TestUtil.readFile(milestone);
    
    milestoneContent = milestoneContent.replaceAll("date=\".*\"", "date=\"\"");
    Assert.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<resources date=\"\">\n" + 
        "    <info-resource>\n" + 
        "        <md5>5863a76f6983d3e97f643802cd485442</md5>\n" + 
        "        <relativePath>flowers.ditamap</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>408630b3734631d02568f1997e4df3e6</md5>\n" + 
        "        <relativePath>topics/introduction.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>f04d2ed1f248b0033d0433be55caef52</md5>\n" + 
        "        <relativePath>topics/flowers/iris.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>26692a27cda3dd11f8d26c87d7de050b</md5>\n" + 
        "        <relativePath>topics/flowers/snowdrop.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "</resources>\n" + 
        "", milestoneContent);
    
    Assert.assertEquals("", TestUtil.read(out.toByteArray(), "UTF-8"));
  }

  /**
   * <p><b>Description:</b> Tests the entry method that creates the package for translation.</p>
   * <p><b>Bug ID:</b> EXM-46006</p>
   *
   * @author alex_jitianu
   *
   * @throws Exception If it fails
   */
  @Test
  public void testGeneratePackage() throws Exception {
    File parentDir = TestUtil.getPath("cmd/v2");
    URL ditaMapURL = new File(parentDir, "flowers.ditamap").toURI().toURL();
    
    File milestoneFile = MilestoneUtil.getMilestoneFile(ditaMapURL);
    String milestoneContent = TestUtil.readFile(milestoneFile).replaceAll("date=\".*\"", "date=\"\"");
    String expectedInitialMilestoneContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<resources date=\"\">\n" + 
        "    <info-resource>\n" + 
        "        <md5>5863a76f6983d3e97f643802cd485442</md5>\n" + 
        "        <relativePath>flowers.ditamap</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>408630b3734631d02568f1997e4df3e6</md5>\n" + 
        "        <relativePath>topics/introduction.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>f04d2ed1f248b0033d0433be55caef52</md5>\n" + 
        "        <relativePath>topics/flowers/iris.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>26692a27cda3dd11f8d26c87d7de050b</md5>\n" + 
        "        <relativePath>topics/flowers/snowdrop.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "</resources>\n" + 
        "";
    assertEquals(expectedInitialMilestoneContent, milestoneContent);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out, true, "UTF-8");
    File packageFile = new File(parentDir, "translation.zip");
    TranslationPackageGenerator.createPackage(ditaMapURL, null, packageFile, ps, false);
    
    Assert.assertTrue("The translation package wasn't created", packageFile.exists());
    
    Assert.assertEquals(
        "topics/\n" + 
        "topics/flowers/\n" + 
        "topics/flowers/iris.dita\n" + 
        "topics/introduction.dita\n" + 
        "", getZipEntries(packageFile).toString());
    
    String log = TestUtil.read(out.toByteArray(), "UTF-8").replaceAll("\r", "");
    Assert.assertTrue(log.contains("Analyze_for_changes: flowers.ditamap"));
    Assert.assertTrue(log.contains("Analyze_for_changes: topics/flowers/iris.dita"));
    Assert.assertTrue(log.contains("Add_to_package: topics/flowers/iris.dita"));
    Assert.assertTrue(log.contains("Add_to_package: topics/introduction.dita"));
    
    // The milestone is unchanged.
    milestoneContent = TestUtil.readFile(milestoneFile).replaceAll("date=\".*\"", "date=\"\"");
    Assert.assertEquals(expectedInitialMilestoneContent, milestoneContent);
    
  }
  
  /**
   * <p><b>Description:</b> Tests the entry method that applies a translation package.</p>
   * <p><b>Bug ID:</b> EXM-46006</p>
   *
   * @author alex_jitianu
   *
   * @throws Exception If it fails
   */
  @Test
  public void testApplyPackage() throws Exception {
    File packageFile = TestUtil.getPath("cmd/v2/translation.zip");
    File parentDir = TestUtil.getPath("cmd/original");
    URL ditaMapURL = new File(parentDir, "flowers.ditamap").toURI().toURL();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out, true, "UTF-8");
    List<String> copiedResources = TranslationPackageGenerator.applyPackage(ditaMapURL, packageFile, ps );
    
    Collections.sort(copiedResources);
    Assert.assertEquals("[topics/flowers/iris.dita, topics/introduction.dita]", copiedResources.toString());
    
    assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" + 
        "<!DOCTYPE topic PUBLIC \"-//OASIS//DTD DITA Topic//EN\" \"topic.dtd\">\n" + 
        "<topic id=\"iris\">\n" + 
        "    <title>Iris</title>\n" + 
        "    <body>\n" + 
        "        <p><b>Iris</b> is a genus of between 200-300 species of flowering plants with showy flowers.\n" + 
        "            It takes its name from the Greek word for a <i>rainbow</i>, referring to the wide\n" + 
        "            variety of flower colors found among the many species.</p>\n" + 
        "        <p>New para</p>\n" + 
        "    </body>\n" + 
        "</topic>\n" + 
        "", TestUtil.readFile(new File(parentDir, "topics/flowers/iris.dita")));
    assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" + 
        "<!DOCTYPE topic PUBLIC \"-//OASIS//DTD DITA Topic//EN\" \"topic.dtd\">\n" + 
        "<topic id=\"introduction\">\n" + 
        "    <title>Introduction</title>\n" + 
        "    <shortdesc>With just a little bit of care and preparation, any flower garden can be a vibrantly\n" + 
        "        colored environment.</shortdesc>\n" + 
        "    <body>\n" + 
        "        <p>Anoterh new para.</p>\n" + 
        "    </body> \n" + 
        "</topic>\n" + 
        "", TestUtil.readFile(new File(parentDir, "topics/introduction.dita")));
    
    Assert.assertEquals(
        "Unpack_file\n" + 
        "Unpack_file\n" + 
        "", TestUtil.read(out.toByteArray(), "UTF-8").replaceAll("\r", ""));
  }
  
  /**
   * <p><b>Description:</b> Tests the entry method that creates the package for translation.</p>
   * <p><b>Bug ID:</b> EXM-46006</p>
   *
   * @author alex_jitianu
   *
   * @throws Exception If it fails
   */
  @Test
  public void testGeneratePackage_RegenerateMilestone() throws Exception {
    File parentDir = TestUtil.getPath("cmd/v2");
    File workingDir = new File(parentDir.getParentFile(), "v2_working");
    FileSystemUtil.deleteRecursivelly(workingDir);
    new ArchiveBuilder(null).copyDirectory(parentDir, workingDir, 0, true);
    
    URL ditaMapURL = new File(workingDir, "flowers.ditamap").toURI().toURL();
    
    File milestoneFile = MilestoneUtil.getMilestoneFile(ditaMapURL);
    String milestoneContent = TestUtil.readFile(milestoneFile).replaceAll("date=\".*\"", "date=\"\"");
    String expectedInitialMilestoneContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<resources date=\"\">\n" + 
        "    <info-resource>\n" + 
        "        <md5>5863a76f6983d3e97f643802cd485442</md5>\n" + 
        "        <relativePath>flowers.ditamap</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>408630b3734631d02568f1997e4df3e6</md5>\n" + 
        "        <relativePath>topics/introduction.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>f04d2ed1f248b0033d0433be55caef52</md5>\n" + 
        "        <relativePath>topics/flowers/iris.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>26692a27cda3dd11f8d26c87d7de050b</md5>\n" + 
        "        <relativePath>topics/flowers/snowdrop.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "</resources>\n" + 
        "";
    assertEquals(expectedInitialMilestoneContent, milestoneContent);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out, true, "UTF-8");
    File packageFile = new File(parentDir, "translation.zip");
    TranslationPackageGenerator.createPackage(ditaMapURL, null, packageFile, ps, true);
    
    Assert.assertTrue("The translation package wasn't created", packageFile.exists());
    
    Assert.assertEquals(
        "topics/\n" + 
        "topics/flowers/\n" + 
        "topics/flowers/iris.dita\n" + 
        "topics/introduction.dita\n" + 
        "", getZipEntries(packageFile).toString());
    
    // The milestone is unchanged.
    milestoneContent = TestUtil.readFile(milestoneFile).replaceAll("date=\".*\"", "date=\"\"");
    Assert.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<resources date=\"\">\n" + 
        "    <info-resource>\n" + 
        "        <md5>5863a76f6983d3e97f643802cd485442</md5>\n" + 
        "        <relativePath>flowers.ditamap</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>4b818e2e24212cd3098c1b341ca44f6c</md5>\n" + 
        "        <relativePath>topics/introduction.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>2e6cb38edf8cecb1d8a03e7285dd3b9b</md5>\n" + 
        "        <relativePath>topics/flowers/iris.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>26692a27cda3dd11f8d26c87d7de050b</md5>\n" + 
        "        <relativePath>topics/flowers/snowdrop.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "</resources>\n" + 
        "", milestoneContent);
    
  }

  /**
   * Gets the entries in the archive.
   * 
   * @param packageFile Archive file.
   * 
   * @return A list with all the entries from the archive.
   * 
   * @throws ZipException
   * @throws IOException
   */
  private static String getZipEntries(File packageFile) throws ZipException, IOException {
    List<String> entries = new ArrayList<>();
    try (ZipFile zipFile = new ZipFile(packageFile)) {
      Enumeration<?> enu = zipFile.entries();
      while (enu.hasMoreElements()) {
        ZipEntry zipEntry = (ZipEntry) enu.nextElement();
        String name = zipEntry.getName();
        entries.add(name);
      }

      StringBuilder b = new StringBuilder();
      Collections.sort(entries);
      for (String string : entries) {
        b.append(string).append("\n");
      }
      
      return b.toString();
    }
  }
  


  /**
   * <p><b>Description:</b> Creates a milestone at a specific location.</p>
   * <p><b>Bug ID:</b> EXM-46198</p>
   *
   * @author alex_jitianu
   *
   * @throws Exception If it fails
   */
  @Test
  public void testGenerateMilestone_SpecifyLocation() throws Exception {
    URL ditaMapURL = new File(TestUtil.getPath("cmd/v1"), "flowers.ditamap").toURI().toURL();
    File defaultMilestone = MilestoneUtil.getMilestoneFile(ditaMapURL);
    assertTrue(defaultMilestone.delete());;
    
    File milestone = new File(TestUtil.getPath("cmd"), "mil.xml");
    ResourceFactory.getInstance().getResource(ditaMapURL, milestone);
    
    TranslationPackageGenerator.generateMilestone(ditaMapURL, milestone, null);
    
    
    String milestoneContent = TestUtil.readFile(milestone);
    
    milestoneContent = milestoneContent.replaceAll("date=\".*\"", "date=\"\"");
    Assert.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<resources date=\"\">\n" + 
        "    <info-resource>\n" + 
        "        <md5>5863a76f6983d3e97f643802cd485442</md5>\n" + 
        "        <relativePath>flowers.ditamap</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>408630b3734631d02568f1997e4df3e6</md5>\n" + 
        "        <relativePath>topics/introduction.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>f04d2ed1f248b0033d0433be55caef52</md5>\n" + 
        "        <relativePath>topics/flowers/iris.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>26692a27cda3dd11f8d26c87d7de050b</md5>\n" + 
        "        <relativePath>topics/flowers/snowdrop.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "</resources>\n" + 
        "", milestoneContent);
    
    defaultMilestone = MilestoneUtil.getMilestoneFile(ditaMapURL);
    assertFalse("The default milestone should not be used.", defaultMilestone.exists());
  }
  
  /**
   * <p><b>Description:</b> Create a package for translation with a specified milestone.</p>
   * <p><b>Bug ID:</b> EXM-46198</p>
   *
   * @author alex_jitianu
   *
   * @throws Exception If it fails
   */
  @Test
  public void testCreatePackage_SpecifyLocation() throws Exception {
    File parentDir = TestUtil.getPath("operation/v2");
    URL ditaMapURL = new File(parentDir, "flowers.ditamap").toURI().toURL();
    File milestoneFile = new File(TestUtil.getPath("operation"), "milestone.xml");
    
    File packageFile = new File(parentDir, "translation.zip");
    TranslationPackageGenerator.createPackage(ditaMapURL, milestoneFile, packageFile, null, false);
    
    Assert.assertTrue("The translation package wasn't created", packageFile.exists());
    
    Assert.assertEquals(
        "topics/\n" + 
        "topics/flowers/\n" + 
        "topics/flowers/iris.dita\n" + 
        "topics/introduction.dita\n" + 
        "", TestUtil.getZipEntries(packageFile).toString());
    
  }


}
