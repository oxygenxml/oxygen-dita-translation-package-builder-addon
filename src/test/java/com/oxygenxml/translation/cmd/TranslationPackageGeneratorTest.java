package com.oxygenxml.translation.cmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;

import com.oxygenxml.translation.support.core.TestUtil;
import com.oxygenxml.translation.support.core.TranslationPackageTestBase;

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
    
    PrintStream ps = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
    File milestone = TranslationPackageGenerator.generateMilestone(ditaMapURL, ps );
    
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
    
    PrintStream ps = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
    File packageFile = new File(parentDir, "translation.zip");
    TranslationPackageGenerator.createPackage(ditaMapURL, packageFile, ps );
    
    Assert.assertTrue("The translation package wasn't created", packageFile.exists());
    
    Assert.assertEquals("topics/\n" + 
        "topics/flowers/\n" + 
        "topics/flowers/iris.dita\n" + 
        "topics/introduction.dita\n" + 
        "", getZipEntries(packageFile).toString());
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
    
    PrintStream ps = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
    List<String> copiedResources = TranslationPackageGenerator.applyPackage(ditaMapURL, packageFile, ps );
    
    
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
    StringBuilder entries = new StringBuilder();
    try (ZipFile zipFile = new ZipFile(packageFile)) {
      Enumeration<?> enu = zipFile.entries();
      while (enu.hasMoreElements()) {
        ZipEntry zipEntry = (ZipEntry) enu.nextElement();
        String name = zipEntry.getName();
        entries.append(name).append("\n");
      }

      return entries.toString();
    }
  }

}
