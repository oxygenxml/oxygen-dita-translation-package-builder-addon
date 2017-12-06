package com.oxygenxml.translation.support.core.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.OxygenParserCreator;
import com.oxygenxml.translation.support.util.ParserCreator;
import com.oxygenxml.translation.support.util.SAXParserCreator;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.util.URLUtil;

/**
 * An implementation that detects the resources referred inside the content of
 * the given resource. 
 */
public class MapStructureResourceBuilder implements IResourceBuilder {
  /**
   * An implementation that detects the resources referred inside the content of
   * the given resource.
   */
  private static class SaxResource implements IResource {
    /**
     * Creates a parser.
     */
    private ParserCreator parserCreator;
    /**
     * The resource to parse.
     */
    protected ReferredResource resource;
    /**
     * The root map.
     */
    private URL rootMap;
    /**
     * A set with all the parsed resources so far. Used to avoid infinite recursion.
     */
    private Set<ReferredResource> visitedURLs;
    /**
     * A path from the root resource to the current one.
     */
    private String relativePath;

    /**
     * Constructor.
     * 
     * @param resource The resource to wrap and parse for children. Initial is the map file.
     * @param relativePath A path from the root resource to the current one. 
     * @param parserCreator Creates a parser.
     * @param recursivityCheck A set to collect all the parsed resources. 
     * Used to avoid infinite recursion.
     * @param rootMap 
     */
    private SaxResource(
        ReferredResource resource, 
        String relativePath,
        ParserCreator parserCreator,
        Set<ReferredResource> recursivityCheck, 
        URL rootMap) {
      this.resource = resource;
      this.parserCreator = parserCreator;
      this.visitedURLs = recursivityCheck;
      this.relativePath = relativePath;
      this.rootMap = rootMap;
    }

    /**
     * @see com.oxygenxml.translation.support.core.resource.IResource#iterator()
     */
    public Iterator<IResource> iterator() {
      List<IResource> children = null;
      // DITA resource 
      if (resource != null && !visitedURLs.contains(resource) && !resource.nonDita()){
        visitedURLs.add(resource);
        try {
          Set<ReferredResource> currentHrefs = gatherReferences();
          if (currentHrefs != null) {
            children = new LinkedList<IResource>();
            for (ReferredResource child : currentHrefs) {
              String childRelativePath = URLUtil.makeRelative(rootMap, child.getLocation());
              // The path is relative to root map.
              SaxResource res = new SaxResource(
                  child,
                  childRelativePath,
                  parserCreator, 
                  visitedURLs,
                  rootMap);
              children.add(res);
            }
          }
        } catch (Exception e) {
          try {
            throw e;
          } catch (Exception e1) {}
        } 
      }

      return children != null ? children.iterator() : null;
    }

    /**
     * @see com.oxygenxml.translation.support.core.resource.IResource#getResourceInfo()
     */
    public ResourceInfo getResourceInfo() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
      ResourceInfo resourceInfo = new ResourceInfo(MilestoneUtil.generateMD5(resource.getLocation()), relativePath);
      if (relativePath.isEmpty()) {
        // It's the root map
        String name = new File(resource.getLocation().getFile()).getName();
        resourceInfo.setRelativePath(name);
      }

      return resourceInfo;
    }

    /**
     * Parses the resource to detect the referenced resources.
     *  
     * @return The referenced resources.
     *  
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Set<ReferredResource> gatherReferences()
        throws ParserConfigurationException, SAXException, IOException {

      URL toParse = URLUtil.correct(resource.getLocation());
      InputSource is = new InputSource(toParse.toExternalForm());
      XMLReader xmlReader = parserCreator.createXMLReader();
      SaxContentHandler handler = new SaxContentHandler(toParse);
      xmlReader.setContentHandler(handler);
      xmlReader.parse(is);

      return handler.getDitaMapHrefs(); 
    }

    public URL getCurrentUrl() {
      return resource.getLocation();
    }

  }

  /**
   * The root map.
   */
  private static class RootMapResource extends SaxResource implements IRootResource {

    /**
     * Constructor.
     * 
     * @param resource The resource to wrap and parse for children.
     * @param relativePath A path from the root resource to the current one. 
     * @param parserCreator Creates a parser.
     * @param recursivityCheck A set to collect all the parsed resources. 
     * Used to avoid infinite recursion.
     */
    private RootMapResource(
        ReferredResource resource, 
        String relativePath,
        ParserCreator parserCreator,
        Set<ReferredResource> recursivityCheck) {
      super(resource, relativePath, parserCreator, recursivityCheck, resource.getLocation());
    }

    /**
     * @see com.oxygenxml.translation.support.core.resource.IRootResource#getMilestoneFile()
     */
    public File getMilestoneFile() {
      return MilestoneUtil.getMilestoneFile(resource.getLocation());
    }

  }

  /**
   * @see com.oxygenxml.translation.support.core.resource.IResourceBuilder#wrap(java.io.File)
   */
  public IRootResource wrap(ReferredResource map) throws IOException {
    ParserCreator parserCreator = null;
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    if (pluginWorkspace != null) {
      // Running in Oxygen environment. Use a special parser.
      parserCreator = new OxygenParserCreator(pluginWorkspace.getXMLUtilAccess());
    } else {
      // Running from tests. Use a simple parser.
      parserCreator = new SAXParserCreator();
    }

    return new RootMapResource(
        map, 
        "", 
        parserCreator, 
        new HashSet<ReferredResource>());
  }
}
