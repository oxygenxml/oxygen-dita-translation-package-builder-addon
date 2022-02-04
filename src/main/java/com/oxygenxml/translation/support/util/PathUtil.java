package com.oxygenxml.translation.support.util;

import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.util.URLUtil;

/**
 * Utility class.
 */
public class PathUtil {
  /**
   * Logger for logging.
   */
  private static final Logger logger = LoggerFactory.getLogger(PathUtil.class.getName());

  /**
   * Private constructor.
   */
  private PathUtil() {
    // Nothing
  }
  
  /**
   * Returns the common path of multiple paths
   * @param paths   An array... of paths
   * 
   * @return    The common path.
   */
  public static String commonPath(String[] paths){
    StringBuilder commonPath = new StringBuilder();
    String[][] folders = new String[paths.length][];
    for(int i = 0; i < paths.length; i++){
      folders[i] = paths[i].split("/"); //split on file separator
    }
    for(int j = 0; j < folders[0].length; j++){
      String thisFolder = folders[0][j]; //grab the next folder name in the first path
      boolean allMatched = true; //assume all have matched in case there are no more paths
      for(int i = 1; i < folders.length && allMatched; i++){ //look at the other paths
        if(folders[i].length < j){ //if there is no folder here
          allMatched = false; //no match
          break; //stop looking because we've gone as far as we can
        }
        //otherwise
        allMatched &= folders[i][j].equals(thisFolder); //check if it matched
      }
      if(allMatched){ //if they all matched this folder name
        //add it to the answer
        commonPath.append(thisFolder).append('/');
      }else{//otherwise
        break;//stop looking
      }
    }
    return commonPath.toString();
  }
  
  /**
   * Returns the common path of multiple paths
   * @param paths   A list of paths
   * 
   * @return    The common path.
   */
  public static String commonPath(List<String> paths) {
    String[] a = new String[paths.size()];
    a = paths.toArray(a);
    return commonPath(a);
  }
  
  /**
   * Returns the common path of multiple paths
   * @param paths   A set of paths.
   * 
   * @return    The common path.
   */
  public static String commonPath(Set<URL> paths) {
    int size = paths.size();
    String[] array = new String[size];
    int i = 0;
    for (Iterator<URL> iterator = paths.iterator(); iterator.hasNext();) {
      URL url = iterator.next();
      array[i] = url.toExternalForm();
      i++;
    }
    
    // Avoid specific URL related escaping sequences.
    return URLUtil.decodeURIComponent(commonPath(array));
  }
  
  /**
   * Utility method to calculate the top location folder.
   * 
   * @param rootMapUrl
   * @return
   * @throws Exception
   */
  public static File calculateTopLocationFile(URL rootMapUrl) {
    File toReturn = null;
    try {
      URL url = calculateTopLocationURL(rootMapUrl, null);
      if (url != null) {
        PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
        if (pluginWorkspace != null) {
          toReturn = pluginWorkspace.getUtilAccess().locateFile(url);
        }
      }
    } catch (Exception e) {
      logger.error(String.valueOf(e), e);
    }
    return toReturn;
  }
  
  /**
   * Calculates the top location using the URL of a DITA MAP.
   * 
   * @param rootMapUrl      URL of DITA map opened in DMM.
   * @param packageBuilder can be <code>null</code>. 
   * @return The top location in URL form.             
   */
  public static URL calculateTopLocationURL(URL rootMapUrl, ChangePackageGenerator packageBuilder) {
    URL location = null;
    try {
      if (packageBuilder == null) {
        packageBuilder = new ChangePackageGenerator();
      }
      String path = calculateTopLocation(rootMapUrl, packageBuilder);
      location = new URL(path);
    } catch (Exception e) {
      logger.error(String.valueOf(e), e);
    }
    return location;
  }
  
  /**
   * Calculates the top location using the URL of a DITA MAP.
   * 
   * @param rootMapUrl      URL of DITA map opened in DMM.
   * @param packageBuilder  The package generator.
   * @return                The top location in STRING form.
   */
  private static String calculateTopLocation(URL rootMapUrl, ChangePackageGenerator packageBuilder) {
    String path = null;
    try {
      IRootResource rootRes = ResourceFactory.getInstance().getResource(rootMapUrl);
      if (rootRes != null) {
        List<ResourceInfo> list = new ArrayList<>();
        list.add(rootRes.getResourceInfo());
        Set<URL> visited = new HashSet<URL>();//NOSONAR
        packageBuilder.computeResourceInfo(rootRes, list, visited);
        visited.add(rootMapUrl);
        path = PathUtil.commonPath(visited);
      }
    } catch (Exception e) {
      logger.error(String.valueOf(e), e);
    }
    return path;
  }
  
  /**
   * Creates a temporary folder in the OS's temporary files system.
   * 
   * @return  The temporary folder or <code>null</code>.
   */
  public static File createTempDirectory() {
    try {
      Path tempPath = Files.createTempDirectory("D_T_B_temp");
      return tempPath.toFile();
    } catch (Exception e) {
      logger.error(String.valueOf(e), e);
    }
    
    return null;
  }
}
