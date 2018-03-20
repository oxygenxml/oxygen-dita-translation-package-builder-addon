package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.support.util.ApplyPackageUtil;
import com.oxygenxml.translation.support.util.PathUtil;
import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;
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
public class UnZipTest extends TestCase{
  
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
    
    final StandalonePluginWorkspace saPluginWorkspaceMock = Mockito.mock(StandalonePluginWorkspace.class);
    PluginWorkspaceProvider.setPluginWorkspace(saPluginWorkspaceMock);
    
    final PluginResourceBundle resourceBundleMock = Mockito.mock(PluginResourceBundle.class);
    Mockito.when(saPluginWorkspaceMock.getResourceBundle()).thenReturn(resourceBundleMock);
    Mockito.when(resourceBundleMock.getMessage(Mockito.anyString())).thenReturn("RETURN_MESSAGE");
    
    UtilAccess utilMock = Mockito.mock(UtilAccess.class);
    Mockito.when(utilMock.locateFile((URL) Mockito.any())).thenReturn(rootDir);
    Mockito.when(saPluginWorkspaceMock.getUtilAccess()).thenReturn(utilMock);    
 
    File archiveLocation = new File(rootDir, "root/THE_ROOT_translation_package.zip");
    File unzippingLocation = PathUtil.calculateTopLocationFile(url);
    assertTrue("", unzippingLocation.getAbsolutePath().endsWith("issue-9-full-unzip"));
    
    Future<?> future = ApplyPackageUtil.overrideTranslatedFiles(
        saPluginWorkspaceMock, 
        unzippingLocation, 
        archiveLocation);
    
    // Wait for completion.
    future.get();
    
    // TODO Adrian Add some asserts.
    // Check files if they are modified after unzipping.
  }

  
}
