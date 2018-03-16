package com.oxygenxml.translation.support.util;

import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.resource.IRootResource;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Utility class.
 */
public class PathUtil {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(PathUtil.class.getName());

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
    return commonPath(array);
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
      URL url = calculateTopLocationURL(rootMapUrl);
      if (url != null) {
        PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
        if (pluginWorkspace != null) {
          toReturn = pluginWorkspace.getUtilAccess().locateFile(url);
        }
      }
    } catch (Exception e) {
      logger.error(e, e);
    }
    return toReturn;
  }
  
  /**
   * XXX
   * @param rootMapUrl
   * @return
   */
  public static URL calculateTopLocationURL(URL rootMapUrl) {
    URL location = null;
    try {
      ChangePackageGenerator packageBuilder = new ChangePackageGenerator(null);
      String path = calculateTopLocation(rootMapUrl, packageBuilder);
      location = new URL(path);
    } catch (Exception e) {
      logger.error(e, e);
    }
    return location;
  }
  
  /**
   * XXX
   * @param rootMapUrl
   * @param packageBuilder
   * @return
   */
  public static String calculateTopLocation(URL rootMapUrl, ChangePackageGenerator packageBuilder) {
    String path = null;
    try {
      IRootResource rootRes = ResourceFactory.getInstance().getResource(rootMapUrl);
      if (rootRes != null) {
        List<ResourceInfo> list = new ArrayList<ResourceInfo>();
        list.add(rootRes.getResourceInfo());
        Set<URL> visited = new HashSet<URL>();//NOSONAR
        packageBuilder.computeResourceInfo(rootRes, list, visited);
        visited.add(rootMapUrl);
        path = PathUtil.commonPath(visited);
      }
    } catch (Exception e) {
      logger.error(e, e);
    }
    return path;
  }
}
