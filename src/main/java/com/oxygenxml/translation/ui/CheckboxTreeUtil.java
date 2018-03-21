package com.oxygenxml.translation.ui;

import java.net.URL;
import javax.swing.Icon;
import javax.swing.tree.DefaultTreeCellRenderer;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Utilities class for checkbox tree used in application.
 * 
 * @author adrian_sorop
 */
public class CheckboxTreeUtil {

  /**
   * Private constructor to avoid instantiation.
   */
  private CheckboxTreeUtil() {
    // Empty
  }

  /**
   * Install tree icons.
   * 
   * @param renderer Tree renderer. Can be <code>null</code>.
   */
  public static void installIcons(DefaultTreeCellRenderer renderer) {

    // Load generic "file" icon
    URL leafURL = PluginWorkspaceProvider.class.getClassLoader().getResource(Icons.TEXT_ICON);
    Icon leafIcon = null;
    if(leafURL != null) {
      leafIcon = (Icon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities().loadIcon(leafURL);
    }

    // Load the "folder" icon
    URL folderURL = PluginWorkspaceProvider.class.getClassLoader().getResource(Icons.OPEN_DIRECTOR_ICON);
    Icon folderIcon = null;
    if(folderURL != null) {
      folderIcon = (Icon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities().loadIcon(folderURL);
    }

    // Apply them if the renderer is not null.
    if (renderer != null) {
      if (leafIcon != null) {
        renderer.setLeafIcon(leafIcon);
      }

      if (folderIcon != null) {
        renderer.setOpenIcon(folderIcon);
        renderer.setClosedIcon(folderIcon);
      }
    }

  }
}
