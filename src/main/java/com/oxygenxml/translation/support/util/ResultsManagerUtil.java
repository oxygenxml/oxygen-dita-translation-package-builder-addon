package com.oxygenxml.translation.support.util;

import com.oxygenxml.translation.ui.Tags;
import java.util.List;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.results.ResultsManager;
import ro.sync.exml.workspace.api.results.ResultsManager.ResultType;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Utility class used to present errors to user.
 * 
 * @author adrian_sorop
 */
public class ResultsManagerUtil {
  
  /**
   * Private constructor.
   */
  private ResultsManagerUtil() {
    // Nothing
  }
  
  /**
   * Generates a DPI and presents it to results panel.
   * 
   * @param severity  Severity of the error {@link DocumentPositionedInfo#SEVERITY_WARN}}, 
   * {@link DocumentPositionedInfo#SEVERITY_INFO}}, {@link DocumentPositionedInfo#SEVERITY_ERROR}}, 
   * {@link DocumentPositionedInfo#SEVERITY_FATAL}}
   * 
   * @param message Message to show.
   * @param systemId  System id of file that contains the error.
   * 
   * @param resultType The type of the result. 
   * One of PROBLEM or GENERIC. It the type is PROBLEM, the results tab will display an icon corresponding 
   * to the severity of the results, otherwise it will not.
   */
  public static void showInResultsPanel(int severity, String message, String systemId, ResultType resultType) {
    DocumentPositionedInfo dpi = new DocumentPositionedInfo(severity, message, systemId);
    StandalonePluginWorkspace pluginWorkspace = (StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace();
    if (pluginWorkspace != null) {
      ResultsManager resultsManager = pluginWorkspace.getResultsManager();
      if (resultsManager != null) {
        resultsManager.addResult(
            pluginWorkspace.getResourceBundle().getMessage(Tags.TRANSLATION_PACKAGE_BUILDER_PLUIGIN_NAME), 
            dpi, 
            resultType, 
            true, 
            false);
      }
    }
  }
  
  /**
   * Clear all errors from {@link Tags#TRANSLATION_PACKAGE_BUILDER_PLUIGIN_NAME}} results manager tab.
   */
  public static void clearResultsPanel() {
    StandalonePluginWorkspace pluginWorkspace = (StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace();
    if (pluginWorkspace != null) {
      ResultsManager resultsManager = pluginWorkspace.getResultsManager();
      if (resultsManager != null) {
        String tabName = pluginWorkspace.getResourceBundle().getMessage(Tags.TRANSLATION_PACKAGE_BUILDER_PLUIGIN_NAME);
        List<DocumentPositionedInfo> allResults = resultsManager.getAllResults(tabName);
        if (allResults != null && !allResults.isEmpty()) {
          for (DocumentPositionedInfo dpi : allResults) {
            resultsManager.removeResult(tabName, dpi);
          }
        }
      }
    }
  }
  
}
