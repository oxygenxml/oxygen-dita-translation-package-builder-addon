package com.oxygenxml.translation.support.core.resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.OxygenParserCreator;
import com.oxygenxml.translation.support.util.ParserCreator;
import com.oxygenxml.translation.support.util.SAXParserCreator;

import ro.sync.basic.util.URLUtil;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * An implementation that detects the resources referred inside the content of
 * the given resource. 
 */
public class MapStructureResourceBuilder implements IResourceBuilder {
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = LoggerFactory.getLogger(MapStructureResourceBuilder.class.getName());
  
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
    protected ReferencedResource resource;
    /**
     * The root map.
     */
    private URL rootMap;
    /**
     * A set with all the parsed resources so far. Used to avoid infinite recursion.
     */
    private Set<ReferencedResource> visitedURLs;
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
        ReferencedResource resource, 
        String relativePath,
        ParserCreator parserCreator,
        Set<ReferencedResource> recursivityCheck, 
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
      
      if (logger.isDebugEnabled()) {
        logger.debug("current resource: " + resource);
        logger.debug("contains??" + visitedURLs.contains(resource));
        logger.debug("exists: " + resourceExists());
        logger.debug("is DITA: " + resource.isDITAResource());
      }

      // DITA resource 
      if (resource != null && !visitedURLs.contains(resource) && 
          resource.isDITAResource() && resourceExists()){
        visitedURLs.add(resource);
        try {
          Set<ReferencedResource> currentHrefs = gatherReferences();
          if (currentHrefs != null) {
            children = new LinkedList<>();
            for (ReferencedResource child : currentHrefs) {
              String childRelativePath = URLUtil.makeRelative(rootMap, child.getLocation());
              childRelativePath = URLUtil.decodeURIComponent(childRelativePath);
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
          logger.error(String.valueOf(e), e);
        } 
      }

      return children != null ? children.iterator() : null;
    }

    /**
     * @return <code>true</code> if the file exists on the disk.
     */
    private boolean resourceExists() {
      File file = null;
      URL location = resource.getLocation();
      PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
      if (pluginWorkspace != null) {
        file = pluginWorkspace.getUtilAccess().locateFile(location);
      } else {
        try {
          file = new File(location.toURI());
        } catch (URISyntaxException e) {
          file = new File(location.getFile());
        }
      }
      return file == null ? false : file.exists();
    }

    /**
     * @see com.oxygenxml.translation.support.core.resource.IResource#getResourceInfo()
     */
    public ResourceInfo getResourceInfo() throws NoSuchAlgorithmException, IOException {
      ResourceInfo resourceInfo = new ResourceInfo(MilestoneUtil.generateMD5(resource.getLocation()), relativePath);
      if (relativePath.isEmpty()) {
        // It's the root map
        PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
        if (pluginWorkspace != null) {
          String name = pluginWorkspace.getUtilAccess().getFileName(resource.getLocation().toExternalForm());
          resourceInfo.setRelativePath(name);
        }
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
    private Set<ReferencedResource> gatherReferences()
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
     * The location where to generate the milestone. If <code>null</code>, the milestone will be generated 
     * next to the map.
     */
    private File milestone;

    /**
     * Constructor.
     * 
     * @param resource The resource to wrap and parse for children.
     * @param milestone The location where to generate the milestone. If <code>null</code>, the milestone will be generated 
     * next to the map.
     * @param relativePath A path from the root resource to the current one. 
     * @param parserCreator Creates a parser.
     * @param recursivityCheck A set to collect all the parsed resources. Used to avoid infinite recursion.
     */
    private RootMapResource(
        ReferencedResource resource, 
        File milestone, 
        String relativePath,
        ParserCreator parserCreator,
        Set<ReferencedResource> recursivityCheck) {
      super(resource, relativePath, parserCreator, recursivityCheck, resource.getLocation());
      this.milestone = milestone;
    }

    /**
     * @see com.oxygenxml.translation.support.core.resource.IRootResource#getMilestoneFile()
     */
    public File getMilestoneFile() {
      return milestone != null ? milestone : MilestoneUtil.getMilestoneFile(resource.getLocation());
    }
  }

  /**
   * @see com.oxygenxml.translation.support.core.resource.IResourceBuilder#wrap(ReferencedResource, java.io.File)
   */
  public IRootResource wrap(ReferencedResource map, File milestone) throws IOException {
    ParserCreator parserCreator = null;
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    if (pluginWorkspace != null && pluginWorkspace.getXMLUtilAccess() != null) {
      // Running in Oxygen environment. Use a special parser.
      parserCreator = new OxygenParserCreator(pluginWorkspace.getXMLUtilAccess());
    } else {
      // Running from tests. Use a simple parser.
      parserCreator = new SAXParserCreator();
    }

    return new RootMapResource(
        map, 
        milestone,
        "", 
        parserCreator, 
        new HashSet<ReferencedResource>());
  }
}
