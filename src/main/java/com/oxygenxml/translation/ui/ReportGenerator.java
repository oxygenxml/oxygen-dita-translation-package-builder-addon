package com.oxygenxml.translation.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;

import com.oxygenxml.translation.support.TranslationPackageBuilderPlugin;
import com.oxygenxml.translation.support.core.models.InfoResources;
import com.oxygenxml.translation.support.core.models.ResourceInfo;
/**
 * Creates a .xhtml report file, a list with all the modified resources.
 * 
 * @author Bivolan Dalina
 *
 */
public class ReportGenerator {
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(ReportGenerator.class); 
  
  public ReportGenerator(File rootDir, ArrayList<ResourceInfo> modifiedResources, File report){
    generateReport(rootDir, modifiedResources, report);
  }
  
  /**
   * Saves the relative paths of the modified files on disk. 
   * 
   * @param info  An object of type InfoResources,this object will be serialized.
   * @param rootDir The directory were the report will be created after serialization.
   * 
   * @return The "modified_resources_report.xml" file.
   * 
   * @throws JAXBException   Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException  The file doesn't exist.
   * @throws StoppedByUserException The user pressed the cancel button.
   */ 
  private File storeReportFile(InfoResources info, File rootDir) throws JAXBException, FileNotFoundException, StoppedByUserException{

    File reportFile = new File(rootDir + File.separator + "report.xml");

    JAXBContext contextObj = JAXBContext.newInstance(InfoResources.class);  

    Marshaller marshallerObj = contextObj.createMarshaller();  
    marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);  

    marshallerObj.marshal(info, reportFile);  

    return reportFile;
  }
  
  /**
   * Creates a .xhtml report file, a list with all the modified resources.
   * 
   * @param rootDir The directory were the report will be created after serialization.
   * @param modifiedResources All the modified files.
   * @param report  Where the generated report will be saved.
   * @throws TransformerFactoryConfigurationError A problem with configuration with the Transformer Factories exists.
   */
  private void generateReport(final File rootDir, final ArrayList<ResourceInfo> modifiedResources, final File report)
      throws TransformerFactoryConfigurationError {
    ArrayList<ResourceInfo> relativePaths = new ArrayList<ResourceInfo>();
    for (int i = 0; i < modifiedResources.size(); i++) {
      relativePaths.add(new ResourceInfo(modifiedResources.get(i).getRelativePath()));
    }

    InfoResources resources = new InfoResources(relativePaths);
    File xmlReport = null;
    try {
      xmlReport = storeReportFile(resources, rootDir);
    } catch (FileNotFoundException e2) {
      logger.error(e2, e2);
    } catch (JAXBException e2) {
      logger.error(e2, e2);
    } catch (StoppedByUserException e2) {
      logger.error(e2, e2);
    }
    //Transform the .xml report file into a .xhtml file
    File xslFile = new File(
        TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), 
        "xsl/report_transformation.xsl");
    try {
      TransformerFactory tFactory = TransformerFactory.newInstance();

      Transformer transformer = tFactory.newTransformer (new StreamSource(xslFile.getAbsolutePath()));
      transformer.transform(new StreamSource(xmlReport.getAbsolutePath()),
         new StreamResult(new FileOutputStream(report.getAbsolutePath())));
      }
    catch (Exception ex) {
      logger.error(ex, ex);
      }
    //Delete the .xml report file after converting it to a .xhtml file
    try {
      FileUtils.forceDelete(xmlReport);
    } catch (IOException e2) {  
      e2.printStackTrace();
    }
  }
}
