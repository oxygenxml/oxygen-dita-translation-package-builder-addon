package com.oxygenxml.translation.support.util;

import com.oxygenxml.translation.exceptions.StoppedByUserException;
import com.oxygenxml.translation.support.storage.ComboHistory;
import com.oxygenxml.translation.support.storage.ComboItem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

/**
 * Utility methods for history management.
 * 
 * @author adrian_sorop
 */
public class HistoryUtils {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(HistoryUtils.class.getName());

  /**
   * Options key to preserve history in dialog.
   */
  private static final String REPORT_DIALOG_HISTORY = "translation.pack.milestone.history.dialog";
  
  /**
   * Oxygen option storage.
   */
  static WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
  
  /**
   * Private constructor. Avoid instantiation.
   */
  private HistoryUtils() {
  }
  
  /**
   * Loads chosen paths for the package location from disk.
   * 
   * @return A list with the absolute file paths of the package.
   * 
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws IOException  Problems reading the file/directory.
   */
  public static List<ComboItem> loadSelectedPaths() {
    List<ComboItem> entries = null;
    JAXBContext context;
    try {
      context = JAXBContext.newInstance(ComboHistory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      String option = optionsStorage.getOption(REPORT_DIALOG_HISTORY, null);
      if (option != null) {
        ComboHistory resources = (ComboHistory) unmarshaller.unmarshal(new StringReader(option));
        entries = resources.getEntries();
      }
    } catch (JAXBException e) {
      logger.error(e, e);
    }

    return entries;
  }
  
  /**
   * Save and persist the chosen paths in the Oxygen preferences user-defined keys and values.
   * @param info   An object of type ComboHistory, this object will be serialized.
   * 
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException  The file doesn't exist.
   * @throws StoppedByUserException  The user pressed the cancel button.
   */
  public static void storeSelectedPaths(ComboHistory info) {

    StringWriter sw = new StringWriter();
    JAXBContext context = null;
    try {
      context = JAXBContext.newInstance(ComboHistory.class);
      Marshaller marshaller = context.createMarshaller();  
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
      marshaller.marshal(info, sw);
    } catch (JAXBException e) {
      logger.error(e, e);
    }  
    optionsStorage.setOption(REPORT_DIALOG_HISTORY, sw.toString());
  }
}
