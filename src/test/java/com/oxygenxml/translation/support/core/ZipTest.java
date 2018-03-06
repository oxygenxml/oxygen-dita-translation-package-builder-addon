package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.support.TranslationPackageBuilderExtension;
import com.oxygenxml.translation.support.core.resource.DetectionType;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import junit.framework.TestCase;
import org.mockito.Mockito;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.util.UtilAccess;

/**
 * Test the zipping functionality.
 * @author adrian_sorop
 */
public class ZipTest extends TestCase{
  
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
    
    final StandalonePluginWorkspace saPluginWorkspaceMock = Mockito.mock(StandalonePluginWorkspace.class);
    PluginWorkspaceProvider.setPluginWorkspace(saPluginWorkspaceMock);
    
    final PluginResourceBundle resourceBundleMock = Mockito.mock(PluginResourceBundle.class);
    Mockito.when(saPluginWorkspaceMock.getResourceBundle()).thenReturn(resourceBundleMock);
    Mockito.when(resourceBundleMock.getMessage(Mockito.anyString())).thenReturn("RETURN_MESSAGE");
    
    UtilAccess utilMock = Mockito.mock(UtilAccess.class);
    Mockito.when(utilMock.locateFile((URL) Mockito.any())).thenReturn(rootMap);
    
    Mockito.when(saPluginWorkspaceMock.getUtilAccess()).thenReturn(utilMock);    
    
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(null);
    ResourceFactory resourceFactory = ResourceFactory.getInstance();
    resourceFactory.setDetectionTypeForTestes(DetectionType.MAP_STRUCTURE);
    
    IRootResource resource = resourceFactory.getResource(rootMapURL);
    final ArrayList<ResourceInfo> modifiedResources = packageBuilder.collectModifiedResources(
        resource, 
        true);
    
    Future<?> future = TranslationPackageBuilderExtension.createPackage(
        null, 
        rootMapURL, 
        saveLocation, 
        resourceBundleMock, 
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

  
}
