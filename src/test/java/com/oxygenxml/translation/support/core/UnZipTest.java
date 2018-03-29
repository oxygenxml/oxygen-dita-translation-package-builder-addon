package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.support.util.ApplyPackageUtil;
import com.oxygenxml.translation.support.util.PathUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;

/**
 * Test the zipping functionality.
 * @author adrian_sorop
 */
public class UnZipTest extends TranslationPackageTestBase {
  
  /**
   * <p><b>Description:</b> The root map might not be in the top folder.</p>
   * <p><b>Bug ID:</b> EXM-41055</p>
   *
   * @author adrian_sorop
   * @throws Exception
   */
  public void testUnZipWorker() throws Exception {
    
    File rootDir = TestUtil.getPath("issue-9-full-unzip");
    File rootMap = new File(rootDir, "root/THE_ROOT.ditamap");
    URL url = rootMap.toURI().toURL();
    
    File topic1 = new File(rootDir, "root/from_root/topic1.dita");
    
    InputStream is0 = new FileInputStream(topic1);
    String topic1Content = IOUtils.toString(is0, "UTF-8");
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<!DOCTYPE dita PUBLIC \"-//OASIS//DTD DITA Composite//EN\" \"ditabase.dtd\">\n" + 
        "<dita id=\"aidi\">\n" + 
        "    <topic><aiDi>xxaasda</aiDi>\n" + 
        "        <title>DOREL WAS HERE!!!</title>\n" + 
        "        <body>\n" + 
        "            <p></p>\n" + 
        "        </body>\n" + 
        "    </topic>\n" + 
        "</dita>\n" + 
        "", 
        topic1Content);
    
    File archiveLocation = new File(rootDir, "root/THE_ROOT_translation_package.zip");
    ZipFile zipFile = new ZipFile(archiveLocation);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    
    String zipContent = null;
    while(entries.hasMoreElements()){
      ZipEntry entry = entries.nextElement();
      String name = entry.getName();
      if (!name.endsWith("/") && !name.endsWith(".ditamap")) {
        // file
        if (name.endsWith("topic1.dita")) {
          InputStream inputStream = zipFile.getInputStream(entry);
          zipContent = IOUtils.toString(inputStream, "UTF-8");
        }
      }
    }  
    
    zipFile.close();
    
    File unzippingLocation = PathUtil.calculateTopLocationFile(url);
    assertTrue("", unzippingLocation.getAbsolutePath().endsWith("issue-9-full-unzip"));
    
    Future<?> future = ApplyPackageUtil.overrideTranslatedFiles(
        saPluginWorkspaceMock, 
        unzippingLocation, 
        archiveLocation);
    
    // Wait for completion.
    future.get();
    
    // Check files if they are modified after unzipping.
    InputStream is = new FileInputStream(topic1);
    String topic1ContentAfterUnzipping = IOUtils.toString(is, "UTF-8");
    
    assertEquals(
        zipContent, 
        topic1ContentAfterUnzipping);
  }

  
}
