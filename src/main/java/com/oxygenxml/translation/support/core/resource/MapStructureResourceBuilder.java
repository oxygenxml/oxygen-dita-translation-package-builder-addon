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

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.SAXParserCreator;
import com.oxygenxml.translation.support.util.OxygenParserCreator;
import com.oxygenxml.translation.support.util.ParserCreator;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.util.URLUtil;

/**
 * An implementation that detects the resources referred inside the content of
 * the given resource. 
 */
public class MapStructureResourceBuilder implements IResourceBuilder {
  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(MapStructureResourceBuilder.class);
  
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
    protected URL resource;
    /**
     * A set with all the parsed resources so far. Used to avoid infinite recursion.
     */
    private Set<URL> recursivityCheck;
    /**
     * A path from the root resource to the current one.
     */
    private String relativePath;
    
    /**
     * Constructor.
     * 
     * @param resource The resource to wrap and parse for children.
     * @param relativePath A path from the root resource to the current one. 
     * @param parserCreator Creates a parser.
     * @param recursivityCheck A set to collect all the parsed resources. 
     * Used to avoid infinite recursion.
     */
    private SaxResource(
        URL resource, 
        String relativePath,
        ParserCreator parserCreator,
        Set<URL> recursivityCheck) {
      this.resource = resource;
      this.parserCreator = parserCreator;
      this.recursivityCheck = recursivityCheck;
    }
    
    /**
     * @see com.oxygenxml.translation.support.core.resource.IResource#iterator()
     */
    public Iterator<IResource> iterator() {
      List<IResource> children = null;
      if(!recursivityCheck.contains(resource)){
        recursivityCheck.add(resource);
        String name = URLUtil.extractFileName(resource);
        if(name.endsWith(".dita") || name.endsWith(".ditamap")){
          // Probably a DITA file.
          try {
            Set<URL> currentHrefs = gatherReferences();
            if (currentHrefs != null) {
              children = new LinkedList<IResource>();
              for (URL child : currentHrefs) {
                String childRelativePath = URLUtil.makeRelative(resource, child);
                children.add(new SaxResource(
                    child,
                    childRelativePath,
                    parserCreator, 
                    recursivityCheck));
              }
            }
          } catch (Exception e) {
            // TODO THrow this further away. Define an exception.
            logger.error(e, e);
          } 
        }
      }
      
      return children != null ? children.iterator() : null;
    }

    /**
     * @see com.oxygenxml.translation.support.core.resource.IResource#getResourceInfo()
     */
    public ResourceInfo getResourceInfo() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
      return new ResourceInfo(MilestoneUtil.generateMD5(resource), relativePath);
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
    private Set<URL> gatherReferences()
        throws ParserConfigurationException, SAXException, IOException {
        URL toParse = URLUtil.correct(resource);
        InputSource is = new InputSource(toParse.toExternalForm());

        XMLReader xmlReader = parserCreator.createXMLReader();

        MySaxParserHandler userhandler = new MySaxParserHandler(toParse);
        xmlReader.setContentHandler(userhandler);
        xmlReader.parse(is);

        return userhandler.getDitaMapHrefs();
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
        URL resource, 
        String relativePath,
        ParserCreator parserCreator,
        Set<URL> recursivityCheck) {
      super(resource, relativePath, parserCreator, recursivityCheck);  
    }

    /**
     * @see com.oxygenxml.translation.support.core.resource.IRootResource#getMilestoneFile()
     */
    public File getMilestoneFile() {
      return MilestoneUtil.getMilestoneFile(resource);
    }
    
  }

  /**
   * @see com.oxygenxml.translation.support.core.resource.IResourceBuilder#wrap(java.io.File)
   */
  public IRootResource wrap(URL map) throws IOException {
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
        new HashSet<URL>());
  }
}
