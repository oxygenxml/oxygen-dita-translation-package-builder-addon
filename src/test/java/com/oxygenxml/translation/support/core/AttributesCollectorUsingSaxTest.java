package com.oxygenxml.translation.support.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.MapStructureResourceBuilder;
import com.oxygenxml.translation.support.core.resource.SaxContentHandler;

import junit.framework.TestCase;
import ro.sync.util.URLUtil;

/**
 * Issue #9 test class.
 * 
 * The root map might not be in the top folder.
 */
public class AttributesCollectorUsingSaxTest extends TestCase{
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
    URL url = URLUtil.correct(ditaFile);
    
    SAXParserFactory factory = SAXParserFactory.newInstance();
    // Ignore the DTD declaration
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    
    SAXParser parser = factory.newSAXParser();
    SaxContentHandler handler= new SaxContentHandler(url);
    parser.parse(ditaFile, handler);
    
    List<URL> referredFiles = new ArrayList<URL>();
    referredFiles.addAll(handler.getDitaMapHrefs());
    
    assertEquals("Two files should have been referred.", 2, referredFiles.size());
    
    assertTrue("First referred topic in dita maps should be topic2.dita", 
        referredFiles.get(0).toString().contains("issue-9/topics/topic2.dita"));
    assertTrue("Second referred topic in dita maps should be topic1.dita", 
        referredFiles.get(1).toString().contains("issue-9/topics/topic1.dita"));
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
    URL url = URLUtil.correct(ditaFile);
    
    SAXParserFactory factory = SAXParserFactory.newInstance();
    // Ignore the DTD declaration
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    
    SAXParser parser = factory.newSAXParser();
    SaxContentHandler handler= new SaxContentHandler(url);
    parser.parse(ditaFile, handler);
    
    List<URL> referredFiles = new ArrayList<URL>();
    referredFiles.addAll(handler.getDitaMapHrefs());
    
    assertEquals("Four files should have been referred.", 4, referredFiles.size());
    assertTrue(referredFiles.get(0).toString().contains("issue-9/topics/dictionaries-preferences-page.dita"));
    assertTrue(referredFiles.get(1).toString().contains("issue-9/topics/add-Hunspell-dictionary.dita"));
    assertTrue(referredFiles.get(2).toString().contains("issue-9/topics/topic2.dita"));
    assertTrue(referredFiles.get(3).toString().contains("issue-9/topics/topic1.dita"));
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
    
    File ditaFile = new File(rootDir,"rootMap.ditamap");
    assertTrue("UNABLE TO LOAD ROOT MAP", ditaFile.exists());
    URL url = URLUtil.correct(ditaFile);
    
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator();
    
    MapStructureResourceBuilder structureBuilder = new MapStructureResourceBuilder();
    IRootResource rootRes = structureBuilder.wrap(url);
    File generateChangeMilestone = packageBuilder.generateChangeMilestone(rootRes, true);
    generateChangeMilestone.deleteOnExit();
    String result = TestUtil.readFile(generateChangeMilestone);
    
    String date = "";
    String match = "<resources date=\"(.*)\">";
    Pattern pattern = Pattern.compile(match);
    Matcher matcher = pattern.matcher(result);
    if (matcher.find( )) {
      date = matcher.group(1);
    }
    
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<resources date=\"" + date + "\">\n" + 
        "    <info-resource>\n" + 
        "        <md5>7890fdbd90a03d403841b24ec0282e87</md5>\n" + 
        "        <relativePath>topics/topic2.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>be8e32eb380695d552b494231f9b3fa2</md5>\n" + 
        "        <relativePath>topics/topic1.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "</resources>\n" + 
        "", 
        result);
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
    rootDir = TestUtil.getPath("issue-15");
    File ditaFile = new File(rootDir,"rootMap.ditamap");
    assertTrue("UNABLE TO LOAD ROOT MAP", ditaFile.exists());
    URL url = URLUtil.correct(ditaFile);
    
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator();
    
    MapStructureResourceBuilder structureBuilder = new MapStructureResourceBuilder();
    IRootResource rootRes = structureBuilder.wrap(url);
    File generateChangeMilestone = packageBuilder.generateChangeMilestone(rootRes, true);
    generateChangeMilestone.deleteOnExit();
    String result = TestUtil.readFile(generateChangeMilestone);
    
    String date = "";
    String match = "<resources date=\"(.*)\">";
    Pattern pattern = Pattern.compile(match);
    Matcher matcher = pattern.matcher(result);
    if (matcher.find( )) {
      date = matcher.group(1);
    }
    
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<resources date=\"" + date + "\">\n" + 
        "    <info-resource>\n" + 
        "        <md5>0e1933d1eb8126735d6aac07fe7eed8c</md5>\n" + 
        "        <relativePath>topics/topic2.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "    <info-resource>\n" + 
        "        <md5>c9e3dcb387774f016ec14ff212060a9d</md5>\n" + 
        "        <relativePath>topics/topic3.dita</relativePath>\n" + 
        "    </info-resource>\n" + 
        "</resources>\n" +
        "", 
        result);
  }
}
