package com.oxygenxml.translation.progress;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.core.PackageBuilder;
import com.oxygenxml.translation.support.core.models.InfoResources;
import com.oxygenxml.translation.support.core.models.ResourceInfo;

import javax.xml.transform.*;
import java.io.*;
import javax.xml.transform.stream.*;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

public class ReportDialog extends OKCancelDialog{
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(PackageBuilder.class); 
  /**
   * Predefined name of the file that stores the relative path for each modified file.
   */
  private final static String REPORT_FILE_NAME = "modified_resources_report.xhtml";
  public static String getReportFileName() {
    return REPORT_FILE_NAME;
  }
  private File selectedFile = null;
  public File getSelectedFile() {
    return selectedFile;
  }
  /**
   * Creates a dialog with a file chooser, a checkbox for generating a report and a label for showing different informations.
   * 
   * @param parentFrame The parent frame.
   * @param title The dialog title.
   * @param modal True if modal.
   * 
   * @throws StoppedByUserException The user pressed the cancel button.
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException  The file doesn't exist.
   */
  public ReportDialog(Frame parentFrame, String title, final File rootDir, final ArrayList<ResourceInfo> modifiedResources){
    //----------------------- modal
    super(parentFrame, title, true);

    getOkButton().setVisible(false);
    getCancelButton().setVisible(false);

    JPanel panel = new JPanel(new GridBagLayout());
    final File report = new File(rootDir, REPORT_FILE_NAME);
    final JLabel label = new JLabel("A report will be created at : " + rootDir.getPath());
    final JCheckBox checkbox = new JCheckBox("Create report");

    checkbox.setSelected(true);
    checkbox.setToolTipText("If selected a report containing the relative paths of the modified resources will be generated.");
    checkbox.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED){
          label.setText("A report will be created at : " + rootDir.getPath());         
        } else {          
          label.setText("The last report was created on : " + new Date(report.lastModified()));
        }
      }
    });

    final JFileChooser fileChooser = new JFileChooser(rootDir);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setBackground(Color.WHITE);
    fileChooser.setForeground(Color.WHITE);
    fileChooser.setCurrentDirectory(rootDir);

    fileChooser.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand() == JFileChooser.APPROVE_SELECTION){
          if(checkbox.isSelected()){            
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
            //Transform the .xml report file into a .html file
            File xslFile = new File("D:/pluginWorkspace/Translation-Package-Builder/xsl/report_transformation.xsl");
            try {
              TransformerFactory tFactory = TransformerFactory.newInstance();

              Transformer transformer = tFactory.newTransformer (new StreamSource(xslFile.getAbsolutePath()));
              transformer.transform(new StreamSource(xmlReport.getAbsolutePath()),
                 new StreamResult(new FileOutputStream(report.getAbsolutePath())));
              }
            catch (Exception ex) {
              ex.printStackTrace( );
              }
            //Delete the .xml report file after converting it to a .html file
            try {
              FileUtils.forceDelete(xmlReport);
            } catch (IOException e2) {  
              e2.printStackTrace();
            }
            //Open the report file           
            try {
              Desktop.getDesktop().open(report);
            } catch (IOException e1) {
              System.out.println("Failed because of : " + e1.getMessage());
              e1.printStackTrace();
            }
          }
          selectedFile = fileChooser.getSelectedFile();
          if(!selectedFile.getName().endsWith(".zip")){
            selectedFile = new File(selectedFile.getPath() + ".zip");
          }
          System.out.println(selectedFile.getAbsolutePath());
          setVisible(false);
        } else if(e.getActionCommand() == JFileChooser.CANCEL_SELECTION){
          setVisible(false);
        }
      }
    });

    fileChooser.setFileFilter(new FileFilter() {

      @Override
      public String getDescription() {
        return "Zip Files(*.zip)";
      }

      @Override
      public boolean accept(File f) {
        if(f.isDirectory()){
          return true;
        } else if(f.getName().endsWith(".zip")){
          return true;
        } else {
          return false;
        }
      }
    });

    ImageIcon infoIcon = new ImageIcon("iconInfo.png");
    JLabel iconLabel = new JLabel(infoIcon);    

    panel.add(checkbox, new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(15, 1, 1, 1), 1, 1));
    panel.add(fileChooser, new GridBagConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(iconLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(label, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 1, 1));

    getContentPane().add(panel, BorderLayout.CENTER);

    setPreferredSize(new Dimension(700, 500));
    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
    setVisible(true);       
  }
  //  public static void main(String[] args) throws HeadlessException, FileNotFoundException, JAXBException, StoppedByUserException {
  //    new ReportDialog(new JFrame(), "test", new File("mapdir"), null);
  //  }
  /**
   * Saves the relative paths of the modified files on disk. 
   * 
   * @param info  An object of type InfoResources,this object will be serialized.
   * @param rootDir The directory were the report will be created after serialization.
   * @return The "modified_resources_report.xml" file.
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

}
