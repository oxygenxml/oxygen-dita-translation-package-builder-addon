package com.oxygenxml.translation.ui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeCellRenderer;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.images.ImageUtilities;

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
    ClassLoader classLoader = PluginWorkspaceProvider.class.getClassLoader();
    ImageUtilities imageUtilities = PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities();

    // Load generic "file" icon
    Icon leafIcon = null;
    URL leafIconURL = classLoader.getResource(Icons.TEXT_ICON);
    if(leafIconURL != null) {
      leafIcon = (Icon) imageUtilities.loadIcon(leafIconURL);
    }

    // Load the "folder" icon
    Icon folderIcon = null;
    URL folderIconURL = classLoader.getResource(Icons.OPEN_DIRECTOR_ICON);
    if(folderIconURL != null) {
      folderIcon = (Icon) imageUtilities.loadIcon(folderIconURL);
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
