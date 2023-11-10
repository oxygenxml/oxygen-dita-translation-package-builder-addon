package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.MapStructureResourceBuilder;
import com.oxygenxml.translation.support.core.resource.ReferencedResource;

import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.css.csstopdf.facade.CatalogResolverFacade;

/**
 * Issue #9 test class.
 * 
 * The root map might not be in the top folder.
 */
public class AttributesCollectorUsingSaxTest extends TranslationPackageTestBase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    initializeCatalogs();
  }
  
  private static final String CONFIG_FOLDER = "config";
  
  /**
   * Initializes the catalogs. Search for them in the config folder.
   * 
   * @param options The command line options.
   * @throws IOException When the installation dir does not exist.
   */
  private static void initializeCatalogs() throws IOException {
    String defaultCatalog = new File(CONFIG_FOLDER + "/catalogs/catalog.xml").toURI().toString();
    // Sets the catalogs
    String[] catalogURIs = new String[] {defaultCatalog};
    CatalogResolverFacade.setCatalogs(catalogURIs, "public");
  }

  /**
   * Resources dir for this test.
   */
  File rootDir = TestUtil.getPath("issue-9");
  
  /**
   * <p><b>Description:</b> Verify the attribute collector over DITA map.
   * Collect referred files non-recursion.</p>
   * <p><b>Bug ID:</b> #9</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testSaxParser() throws Exception {

    File ditaFile = new File(rootDir,"rootMap.ditamap");
    assertTrue("UNABLE TO LOAD FILE", ditaFile.exists());
    
    List<ReferencedResource> referredFiles = parseFileAndGatherReferences(ditaFile);
    
    assertEquals("Two files should have been referred.", 2, referredFiles.size());
    assertTrue("Referred topic in dita maps should be topic2.dita", 
        referredFiles.toString().contains("issue-9/topics/topic2.dita"));
    assertTrue("Referred topic in dita maps should be topic1.dita", 
        referredFiles.toString().contains("issue-9/topics/topic1.dita"));
  }

  /**
   * <p><b>Description:</b> Exclude External references. Load files only once.<p>
   * <p><b>Bug ID:</b> #9</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testSaxExcludeHTMLReferences() throws Exception {
  
    File ditaFile = new File(rootDir,"topics/add-terms-list.dita");
    assertTrue("UNABLE TO LOAD FILE", ditaFile.exists());
    
    List<ReferencedResource> referredFiles = parseFileAndGatherReferences(ditaFile);
    
    assertEquals("Four files should have been referred.", 4, referredFiles.size());
    assertTrue(referredFiles.toString().contains("issue-9/topics/dictionaries-preferences-page.dita"));
    assertTrue(referredFiles.toString().contains("issue-9/topics/add-Hunspell-dictionary.dita"));
    assertTrue(referredFiles.toString().contains("issue-9/topics/topic2.dita"));
    assertTrue(referredFiles.toString().contains("issue-9/topics/topic1.dita"));
  }
  
  /**
   * <p><b>Description:</b> Referred resources should be relative to root map,
   * not to current resource.</p>
   * <p><b>Bug ID:</b> #9</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testGatherReferences() throws Exception {
    
    File ditaFile = new File(TestUtil.getPath("issue-9"),"rootMap.ditamap");
    assertTrue("UNABLE TO LOAD ROOT MAP", ditaFile.exists());
    URL url = URLUtil.correct(ditaFile);
    
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(null);
    MapStructureResourceBuilder structureBuilder = new MapStructureResourceBuilder();
    IRootResource rootRes = structureBuilder.wrap(new ReferencedResource(url, true), null);
    File generateChangeMilestone = packageBuilder.generateChangeMilestone(rootRes);
    String result = TestUtil.readFile(generateChangeMilestone);
    
    assertTrue(result.contains("<relativePath>rootMap.ditamap</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topic2.dita</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topic3.dita</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topic1.dita</relativePath>"));
    generateChangeMilestone.delete();
  }

  /**
   * <p><b>Description:</b> Referred resources should appear only once in the milestone.</p>
   * <p><b>Bug ID:</b></p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testDoNotAddSameFileTwice() throws Exception {
    // Discovered while working on issue 15.
    File ditaFile = new File(TestUtil.getPath("issue-15"),"rootMap.ditamap");
    assertTrue("UNABLE TO LOAD ROOT MAP", ditaFile.exists());
    
    String result = generateMilestoneAndGetContent(ditaFile);
    assertTrue(result.contains("<relativePath>rootMap.ditamap</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topic2.dita</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/not-referred-in-ditamap.dita</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topic3.dita</relativePath>"));
    
  }

  /**
   * <p><b>Description:</b>Add the conref to milestone package even if the file is not  
   * referred directly to the map.</p>
   * <p><b>Bug ID:</b>#15</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testAddConrefsToMilestone() throws Exception {
    // Discovered while working on issue 15.
    File ditaFile = new File(TestUtil.getPath("issue-15_1"),"rootMap.ditamap");
    assertTrue("UNABLE TO LOAD ROOT MAP", ditaFile.exists());
    
    String result = generateMilestoneAndGetContent(ditaFile);
    assertTrue(result.contains("<relativePath>rootMap.ditamap</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topic2.dita</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topic3.dita</relativePath>"));
    assertTrue(result.contains("<relativePath>topics/topicConref.dita</relativePath>"));
    
  }

  /**
   * <p><b>Description:</b> Test the conrefs are handled by the SAXParser.</p>
   * <p><b>Bug ID:</b> #15</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testSaxParserConref() throws Exception {
    File ditaFile = new File(TestUtil.getPath("issue-15_1/topics"),"topic2.dita");
    assertTrue("UNABLE TO LOAD FILE", ditaFile.exists());
    
    List<ReferencedResource> referredFiles = parseFileAndGatherReferences(ditaFile);
    assertEquals("Two files should have been referred.", 2, referredFiles.size());
    assertTrue("Should be a content reference to topicConref.dita but was" + referredFiles.get(0).getLocation(), 
        referredFiles.get(0).getLocation().toString().contains("issue-15_1/topics/topicConref.dita"));
    assertTrue("Should be a xref to topic3.dita", 
        referredFiles.get(1).getLocation().toString().contains("issue-15_1/topics/topic3.dita"));
  }

  /**
   * <p><b>Description:</b> Collect references when DITA files with "xml" extension is used.</p>
   * <p><b>Bug ID:</b> #18</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testCollectReferencesFromXmlFiles() throws Exception {
    // Discovered while working on issue 15.
    File ditaFile = new File(TestUtil.getPath("issue-18"), "rootMap.ditamap");
    assertTrue("UNABLE TO LOAD ROOT MAP", ditaFile.exists());
    
    String result = generateMilestoneAndGetContent(ditaFile);
    assertTrue(result.contains("<relativePath>rootMap.ditamap</relativePath>"));
    assertTrue(result.contains("<relativePath>referredResource.xml</relativePath>"));
    assertTrue(result.contains("<relativePath>reusable.xml</relativePath>"));
    
  }

  /**
   * <p><b>Description:</b>Do not parse binary resources.</p>
   * <p><b>Bug ID:</b> #18</p>
   *
   * @author adrian_sorop
   *
   * @throws Exception
   */
  public void testDoNotParseImages() throws Exception {
    // Discovered while working on issue 15.
    File ditaFile = new File(TestUtil.getPath("issue-not-parse-image-sax"), "rootMap.ditamap");
    assertTrue("UNABLE TO LOAD ROOT MAP", ditaFile.exists());
    
    String result = generateMilestoneAndGetContent(ditaFile);
    assertTrue(result.contains("<relativePath>rootMap.ditamap</relativePath>"));
    assertTrue(result.contains("<relativePath>referredResource.xml</relativePath>"));
    assertTrue(result.contains("<relativePath>href_res.xml</relativePath>"));
    assertTrue(result.contains("<relativePath>conref_res.xml</relativePath>"));
    assertTrue(result.contains("<relativePath>Oxygen128.png</relativePath>"));
  }
}
