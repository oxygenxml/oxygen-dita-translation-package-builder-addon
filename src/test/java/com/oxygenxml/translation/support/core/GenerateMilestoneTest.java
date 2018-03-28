package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.support.core.resource.FileSystemResourceBuilder;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * MD5 and milestone generation tests.
 */
public class GenerateMilestoneTest {
  
  /**
   * Tests the MD5 generation for a file.
   * 
   * @throws Exception If it fails.
   */
  @Test
  public void testMd5_File() throws Exception {
    File file = TestUtil.getPath("md5Test.txt");
    
    String cksum = MilestoneUtil.generateMD5(file);
    Assert.assertEquals("8da05f6b66f46cfa4b1ac92ebdab16f7", cksum);
    
    file = TestUtil.getPath("generateMD5-test.txt");

    cksum = MilestoneUtil.generateMD5(file);
    Assert.assertEquals("95bcd2d5a06b5f63b84551ddd8ec1483", cksum);
  }

  /**
   * Generates the milestone file on disk and asserts its contents.
   * 
   * @throws Exception If it fails.
   */
	@Test
	public void testChangeMilestone() throws Exception {
		File rootDir = TestUtil.getPath("generateMilestone-Test");
		
		StandalonePluginWorkspace saPluginWorkspaceMock = Mockito.mock(StandalonePluginWorkspace.class);
	  PluginResourceBundle resourceBundleMock = Mockito.mock(PluginResourceBundle.class);
	  
	  PluginWorkspaceProvider.setPluginWorkspace(saPluginWorkspaceMock);
    Mockito.when(saPluginWorkspaceMock.getResourceBundle()).thenReturn(resourceBundleMock);

		ChangePackageGenerator packageBuilder = new ChangePackageGenerator(null);
		
		IRootResource rootResource = new FileSystemResourceBuilder().wrapDirectory(rootDir);
    
    packageBuilder.generateChangeMilestone(rootResource);
		
		ArrayList<ResourceInfo> expectedResult = new ArrayList<ResourceInfo>();
		expectedResult.add(new ResourceInfo("754d9436d3a245ad9a340b8d9929fc46", "testGenerate/md5.txt"));
		expectedResult.add(new ResourceInfo("3c01bd69152843f5aada9595c6b75bf2", "testGenerate/md5_no2.txt"));
		expectedResult.add(new ResourceInfo("754d9436d3a245ad9a340b8d9929fc46", "testIteration/dir1/md5.txt"));
		expectedResult.add(new ResourceInfo("521304ca436443d97ccf68ee919c03b3", "testIteration/dir1/md5_no2.txt"));
		expectedResult.add(new ResourceInfo("55047487acf9f525244b12cff4bfc49c", "testIteration/dir2/md5.txt"));
		expectedResult.add(new ResourceInfo("5c24a78aec732e9626a4a7114efd98b1", "testIteration/dir2/md5_no2.txt"));
		
		List<ResourceInfo> actualResult = MilestoneUtil.loadMilestoneFile(rootResource);
		
		Assert.assertEquals(TestUtil.dump(expectedResult), TestUtil.dump(actualResult));
	}
}
