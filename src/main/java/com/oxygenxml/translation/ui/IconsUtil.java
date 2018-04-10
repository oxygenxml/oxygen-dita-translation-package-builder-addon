package com.oxygenxml.translation.ui;

import java.net.URL;
import javax.swing.Icon;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.images.ImageUtilities;

public class IconsUtil {
  
  /**
   * Private Constructor.
   */
  private IconsUtil() {
    // Nothing
  }
  
  public static Icon getFolderIcon() {
    Icon folderIcon = null;
    URL folderIconURL = PluginWorkspaceProvider.class.getClassLoader().getResource(Icons.OPEN_DIRECTOR_ICON);
    if (folderIconURL != null) {
      PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
      if(pluginWorkspace != null) {
        ImageUtilities imageUtilities = pluginWorkspace.getImageUtilities();
        folderIcon = (Icon) imageUtilities.loadIcon(folderIconURL);
      }
    }
    return folderIcon;
  }
  
  /**
   * @return The info icon.
   */
  public static Icon getInfoIcon() {
    Icon icon = null;
    URL iconURL = PluginWorkspaceProvider.class.getClassLoader().getResource(Icons.INFO_ICON);
    if (iconURL != null) {
      PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
      if(pluginWorkspace != null) {
        ImageUtilities imageUtilities = pluginWorkspace.getImageUtilities();
        icon = (Icon) imageUtilities.loadIcon(iconURL);
      }
    }
    return icon;
  }
}
