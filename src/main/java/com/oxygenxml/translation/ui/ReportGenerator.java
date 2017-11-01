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
import org.apache.commons.io.FilenameUtils;

import com.oxygenxml.translation.support.TranslationPackageBuilderPlugin;
import com.oxygenxml.translation.support.storage.InfoResources;
import com.oxygenxml.translation.support.storage.ResourceInfo;
/**
 * Creates a .xhtml report file, a list with all the modified resources.
 * 
 * @author Bivolan Dalina
 *
 */
public class ReportGenerator {
  /**
   * Stylesheet to transform the xml report to xhtml document.
   */
  private static final String REPORT_TRANSFORMATION_XSL = "xsl/report_transformation.xsl";
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(ReportGenerator.class); 
  
  /**
   * 
   * @param ditaMap The dita map to process.
   * @param modifiedResources A list with the modified resources.
   * @param report The report file.
   */
  public ReportGenerator(File ditaMap, ArrayList<ResourceInfo> modifiedResources, File report){
    generateReport(ditaMap, modifiedResources, report);
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
  private File storeReportFile(InfoResources info, File rootDir) throws JAXBException, FileNotFoundException{

    File reportFile = new File(rootDir + File.separator + "report.xml");
    JAXBContext context = JAXBContext.newInstance(InfoResources.class);  
    Marshaller marshaller = context.createMarshaller();  
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);  
    marshaller.marshal(info, reportFile);  

    return reportFile;
  }
  
  /**
   * Creates a .xhtml report file, a list with all the modified resources.
   * 
   * @param ditaMap The ditamap to process.
   * @param modifiedResources All the modified files.
   * @param report  Where the generated report will be saved.
   * @throws TransformerFactoryConfigurationError A problem with configuration with the Transformer Factories exists.
   */
  private void generateReport(final File ditaMap, final ArrayList<ResourceInfo> modifiedResources, final File report)
      throws TransformerFactoryConfigurationError {
    final File rootDir = ditaMap.getParentFile();
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
    }
    
    //Transform the .xml report file into a .xhtml file
    File xslFile = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), 
        REPORT_TRANSFORMATION_XSL);
    
    FileOutputStream outputStream = null;
    try {
      TransformerFactory factory = TransformerFactory.newInstance();

      StreamSource xslSource = new StreamSource(xslFile.getAbsolutePath());
      Transformer transformer = factory.newTransformer (xslSource);
      
      String mapTitle = FilenameUtils.removeExtension(ditaMap.getName());
      if (logger.isDebugEnabled()) {
        logger.debug("ROOT MAP IS " + mapTitle != null ? mapTitle : "DITAMap");
      }
      transformer.setParameter("mapTitle", mapTitle != null ? mapTitle : "DITAMap");
      
      outputStream = new FileOutputStream(report.getAbsolutePath());
      StreamSource xmlSource = new StreamSource(xmlReport.getAbsolutePath());
      StreamResult outputTarget = new StreamResult(outputStream);
      transformer.transform(xmlSource, outputTarget);
      
    } catch (Exception ex) {
      logger.error(ex, ex);
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          /*Nothing to do here*/
          }
      }
    }
    //Delete the .xml report file after converting it to a .xhtml file
    try {
      if (xmlReport != null) {
        FileUtils.forceDelete(xmlReport);
      }
    } catch (IOException e2) {  
      e2.printStackTrace();
    }
  }
}
