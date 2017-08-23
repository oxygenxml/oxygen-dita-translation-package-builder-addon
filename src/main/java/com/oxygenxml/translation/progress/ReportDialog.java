package com.oxygenxml.translation.progress;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.core.models.ResourceInfo;

import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

public class ReportDialog extends OKCancelDialog{
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(ReportDialog.class); 
  /**
   *  Resource bundle.
   */
  private final static PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
  /**
   * Predefined name of the file that stores the relative path for each modified file.
   */
  private final static String REPORT_FILE_NAME = resourceBundle.getMessage(Tags.REPORT_DIALOG_REPORT_NAME);
  public static String getReportFileName() {
    return REPORT_FILE_NAME;
  }
  /**
   * The location choosed by the user for the archive.
   */
  private File choosedLocation = null;
  public File getChoosedLocation() {
    return choosedLocation;
  }
  /**
   * True if the user selected the create report checkbox.
   */
  private boolean shouldCreateReport = false;
  public boolean isShouldCreateReport() {
    return shouldCreateReport;
  }
  /**
   * True if user pressed the save button.
   */
  private boolean saveButtonSelected = false;
  public boolean isSaveButtonSelected() {
    return saveButtonSelected;
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
    
    final JFileChooser fileChooser = new JFileChooser(rootDir);
    
    getOkButton().setVisible(false);
    getCancelButton().setVisible(false);

    JPanel panel = new JPanel(new GridBagLayout());
    final File report = new File(rootDir, REPORT_FILE_NAME);
    final JLabel label = new JLabel(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL) + rootDir.getPath());
    final JCheckBox checkbox = new JCheckBox(resourceBundle.getMessage(Tags.REPORT_DIALOG_CHECKBOX_TEXT));

    checkbox.setSelected(true);
    checkbox.setToolTipText(resourceBundle.getMessage(Tags.REPORT_DIALOG_CHECKBOX_TOOLTIP));
    checkbox.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED){
          label.setText(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL) + rootDir.getPath());         
        } else {          
          label.setText(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL_TEXT2) + new Date(report.lastModified()));
        }
      }
    });
    
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setBackground(Color.WHITE);
    fileChooser.setForeground(Color.WHITE);
    fileChooser.setCurrentDirectory(rootDir);

    fileChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand() == JFileChooser.APPROVE_SELECTION){
          saveButtonSelected = true;
          if(checkbox.isSelected()){    
            shouldCreateReport = true;
            new ReportGenerator(rootDir, modifiedResources, report);
          }
          choosedLocation = fileChooser.getSelectedFile();
          if(!choosedLocation.getName().endsWith(".zip")){
            choosedLocation = new File(choosedLocation.getPath() + ".zip");
          }
          if(logger.isDebugEnabled()){
            logger.debug(resourceBundle.getMessage(Tags.REPORT_DIALOG_LOGGER_MESSAGE) + choosedLocation.getAbsolutePath());
          }
          setVisible(false);
        } else if(e.getActionCommand() == JFileChooser.CANCEL_SELECTION){
          setVisible(false);
        }
      }
    });
    
    fileChooser.setFileFilter(new FileFilter() {

      @Override
      public String getDescription() {
        return resourceBundle.getMessage(Tags.REPORT_DIALOG_FILE_DESCRIPTOR);
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

    panel.add(checkbox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(15, 1, 1, 1), 1, 1));
    panel.add(fileChooser, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(label, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 1, 1));
    
    getContentPane().add(panel, BorderLayout.CENTER);
    
    setPreferredSize(new Dimension(700, 500));
    setMinimumSize(getPreferredSize());
    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
    setVisible(true);       
  }
}
