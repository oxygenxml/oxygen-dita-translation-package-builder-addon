package com.oxygenxml.translation.support.core;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.core.resource.DetectionType;
import com.oxygenxml.translation.support.core.resource.FileSystemResourceBuilder;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.MapStructureResourceBuilder;
import com.oxygenxml.translation.support.core.resource.ReferencedResource;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.core.resource.SaxContentHandler;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.TestCase;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.util.UtilAccess;
import ro.sync.util.URLUtil;

/**
 * This is a test base for all translation package builder add-on. 
 * Plugin workspace, resources bundle and other utility methods are mocked here.
 * 
 * @author adrian_sorop
 */
public class TranslationPackageTestBase extends TestCase {
  public void testTrue() throws Exception {
    assertTrue(true);
  }
  
  final StandalonePluginWorkspace saPluginWorkspaceMock = Mockito.mock(StandalonePluginWorkspace.class);
  final UtilAccess utilMock = Mockito.mock(UtilAccess.class);
  
  @Override
  protected void setUp() throws Exception {
    
    super.setUp();

    PluginWorkspaceProvider.setPluginWorkspace(saPluginWorkspaceMock);
    Mockito.when(saPluginWorkspaceMock.getResourceBundle()).thenReturn(new PluginResourceBundle() {
      @Override
      public String getMessage(String messageKey) {
        return messageKey;
      }
    });
    
    Mockito.when(saPluginWorkspaceMock.getUtilAccess()).thenReturn(utilMock);
    
    Mockito.when(utilMock.locateFile((URL) Mockito.any())).then(new Answer<File>() {
      @Override
      public File answer(InvocationOnMock invocation) throws Throwable {
        URL url = (URL) invocation.getArguments()[0];
        return URLUtil.getCanonicalFileFromFileUrl(url);
      }
    });
    Mockito.when(utilMock.getFileName((String) Mockito.any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return URLUtil.extractFileName((String) invocation.getArguments()[0]);
      }
    });
    
  }
  /**
   * Generates a milestone using the map structure builder and returns the milestone content.
   * @param ditaMap Current DITA map opened in DMM.
   */
  protected static String generateMilestoneAndGetContent(File ditaMap) throws MalformedURLException, IOException, NoSuchAlgorithmException,
  JAXBException, StoppedByUserException, FileNotFoundException {
    URL url = URLUtil.correct(ditaMap);
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(null);
    MapStructureResourceBuilder structureBuilder = new MapStructureResourceBuilder();
    IRootResource rootRes = structureBuilder.wrap(new ReferencedResource(url, true));
    File generateChangeMilestone = packageBuilder.generateChangeMilestone(rootRes);
    String result = TestUtil.readFile(generateChangeMilestone);
    generateChangeMilestone.delete();
    return result;
  }
  
  /**
   * Creates a SAX parser over an xml file and collect all referred files.
   * @param ditaFile  XML file to be parsed.
   */
  protected static List<ReferencedResource> parseFileAndGatherReferences(File ditaFile)
      throws MalformedURLException, ParserConfigurationException,
      SAXNotRecognizedException, SAXNotSupportedException, SAXException, IOException {
    URL url = URLUtil.correct(ditaFile);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    // Ignore the DTD declaration
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    
    SAXParser parser = factory.newSAXParser();
    SaxContentHandler handler= new SaxContentHandler(url);
    parser.parse(ditaFile, handler);
    List<ReferencedResource> referredFiles = new ArrayList<ReferencedResource>();
    referredFiles.addAll(handler.getDitaMapHrefs());
    return referredFiles;
  }
  
  /**
   * Detects the modified files using the File System builder.
   * @param dirPath   The directory we want to scan for modified files.
   */
  protected static List<ResourceInfo> generatesModifiedFilesUsingFileSystemBuilder(File dirPath)
      throws NoSuchAlgorithmException, IOException, StoppedByUserException {
    ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
    new ChangePackageGenerator(null).computeResourceInfo(
        new FileSystemResourceBuilder().wrapDirectory(dirPath), list, new HashSet<URL>());
    return list;
  }
  
  /**
   * Generates a list of modified files over the DITA map URL. 
   */
  protected static List<ResourceInfo> generatesModifiedFilesUsingMapStructureBuilder(final URL rootMapURL)
      throws IOException, JAXBException, NoSuchAlgorithmException, StoppedByUserException {
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(null);
    ResourceFactory resourceFactory = ResourceFactory.getInstance();
    resourceFactory.setDetectionTypeForTestes(DetectionType.MAP_STRUCTURE);
    
    IRootResource resource = resourceFactory.getResource(rootMapURL);
    final List<ResourceInfo> modifiedResources = packageBuilder.collectModifiedResources(resource);
    return modifiedResources;
  }
  
}
