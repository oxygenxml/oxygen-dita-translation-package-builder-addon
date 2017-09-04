package com.oxygenxml.translation.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.TranslationPackageBuilderPlugin;
import com.oxygenxml.translation.support.core.models.InfoResources;
import com.oxygenxml.translation.support.core.models.ResourceInfo;

import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarToggleButton;

public class ReportDialog extends OKCancelDialog {
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
  private final static String REPORT_FILE_NAME = "modified_resources_report.xhtml";
  public static String getReportFileName() {
    return REPORT_FILE_NAME;
  }
  /**
   * Predefined name of the modified resources archive.
   */
  private final static String ZIP_FILE_NAME = "to_translate_package.zip";
  /**
   *  Entry point for accessing the DITA Maps area.
   */
  private final StandalonePluginWorkspace pluginWorkspace = (StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace();
  /**
   * The location choosed by the user for the archive.
   */
  private File choosedZip = null;
  public File getChoosedLocation(){
    return choosedZip;
  }
  /**
   * The generate report checkbox.
   */
  private final JCheckBox checkbox;
  /**
   * True if the user selected the create report checkbox.
   */
  private boolean shouldCreateReport = false;
  public boolean isShouldCreateReport() {
    return shouldCreateReport;
  }
  /**
   * The parent directory of the current ditamap.
   */
  private File rootDir;
  /**
   * The location of the generated report.
   */
  private final File reportFile;
  /**
   * The list with all the modified resources.
   */
  private final ArrayList<ResourceInfo> modifiedResources;
  /**
   * True if the user pressed the "Save" button, false otherwise.
   */
  private boolean isSaveButtonPressed = false;
  public boolean isSaveButtonPressed() {
    return isSaveButtonPressed;
  }
  /**
   *  Where the package location is displayed.
   */
  private JComboBox<String> comboBox = new JComboBox<String>();
  /**
   * The predefined name of the file that will store the choosed zip locations.
   */
  private final static String OPTION_FILE_NAME = "combo_options.xml";
  /**
   * The predefined file containing the choosed package locations from the previous session.
   */
  private File optionsFile = new File( TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), OPTION_FILE_NAME);
  /**
   * A list that contains all the choosed package lacations.
   */
  private ArrayList<ResourceInfo> comboItems = new ArrayList<ResourceInfo>();
  /**
   * The selected path, the one that appears in the comboBox.
   */
  private String currentPath;
  /**
   * The default location of the package.
   */
  private File defaultPackageLocation;
  /**
   * The choosed location from the file chooser.
   */
  private File choosedZipFromChooser;
  /**
   * True if the package location was choosed from the file chooser, false otherwise.
   */
  private boolean isFromChooser = false;
  /**
   * Creates a dialog where the user can choose the location of the modified resources archive
   *  and generate or not a report.
   * 
   * @param parentFrame The parent frame of the dialog.
   * @param rootDir The parent directory of the current ditamap.
   * @param modifiedResources The list with all the modified resources.
   */
  public ReportDialog(Frame parentFrame, final File rootDir, ArrayList<ResourceInfo> modifiedResources) {
    //--------------------------------------------------------------------------- modal
    super(parentFrame, resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE), true);
    this.rootDir = rootDir;
    this.modifiedResources = modifiedResources;
    // The default location of the report file.
    reportFile = new File(rootDir, REPORT_FILE_NAME);
    // The default location of the package.
    defaultPackageLocation = new File(rootDir, ZIP_FILE_NAME);
    // Add into the comboBox the choosed paths stored in the optionsFile.
    try {
      ArrayList<ResourceInfo> savedPaths = loadSelectedPaths();
      for (ResourceInfo resourceInfo : savedPaths) {
        comboBox.addItem(resourceInfo.getRelativePath());
      }
    } catch (JAXBException e1) {
      logger.error(e1, e1);
    } catch (IOException e1) {
      logger.error(e1, e1);
    }


    getOkButton().setText("Save");

    JLabel label = new JLabel("Package Location : ");
    checkbox = new JCheckBox("Generate report");
    //Show the user informations about the report creation.
    final JTextArea textInfo = new JTextArea(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL) + rootDir.getPath());
    textInfo.setWrapStyleWord(true);
    textInfo.setLineWrap(true);

    final JTextField textfield = (JTextField) comboBox.getEditor().getEditorComponent();
    currentPath = defaultPackageLocation.getPath();
    comboBox.setEditable(true);
    comboBox.setSelectedItem(currentPath);
    textfield.select(currentPath.length() - defaultPackageLocation.getName().length(), currentPath.length());
    comboItems.add(new ResourceInfo(currentPath));

    final UndoManager undo = new UndoManager();
    Document textFieldDocument = textfield.getDocument();
    // Listen for undo and redo events
    textFieldDocument.addUndoableEditListener(new UndoableEditListener() {
      public void undoableEditHappened(UndoableEditEvent evt) {
        undo.addEdit(evt.getEdit());
      }
    });
    // Create an undo action and add it to the text component
    textfield.getActionMap().put("Undo",
        new AbstractAction("Undo") {
      public void actionPerformed(ActionEvent evt) {
        try {
          if (undo.canUndo()) {
            undo.undo();
          }
        } catch (CannotUndoException e) {
          logger.error(e, e);
        }
      }
    });
    // Bind the undo action to ctl-Z
    textfield.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

    // Create a redo action and add it to the text component
    textfield.getActionMap().put("Redo",
        new AbstractAction("Redo") {
      public void actionPerformed(ActionEvent evt) {
        try {
          if (undo.canRedo()) {
            undo.redo();
          }
        } catch (CannotRedoException e) {
          logger.error(e, e);
        }
      }
    });
    // Bind the redo action to ctl-Y
    textfield.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

    File imageFolderLocation = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), "Open16.png");
    ImageIcon image = new ImageIcon(imageFolderLocation.getPath());
    final ToolbarToggleButton folderButton = new ToolbarToggleButton(image);
    // Show a file chooser when the user clicks on the folder image.
    folderButton.addMouseListener(new MouseAdapter() {  
      public void mousePressed(MouseEvent e) {
        //Get the location from the file chooser.
        isFromChooser = true;
        choosedZipFromChooser = pluginWorkspace.chooseFile(resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE),
            new String[] {"zip"},
            resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_DESCRIPTOR), 
            true);
        //Update the package location field with the choosed location from the file chooser.
        if(choosedZipFromChooser != null){
          logger.debug("Clicked on folder image and choosed this archive : " + choosedZipFromChooser.getPath());
          currentPath = choosedZipFromChooser.getPath();
          comboBox.setSelectedItem(currentPath);
          comboBox.addItem(currentPath);
          textfield.select(currentPath.length() - choosedZipFromChooser.getName().length(), currentPath.length());
          comboItems.add(new ResourceInfo(currentPath));
        }
      }
      @Override
      public void mouseEntered(MouseEvent e) {
        folderButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    });

    checkbox.setSelected(true);
    checkbox.setToolTipText(resourceBundle.getMessage(Tags.REPORT_DIALOG_CHECKBOX_TOOLTIP));

    checkbox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED){
          textInfo.setText(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL) + rootDir.getPath());         
        } else {
          textInfo.setText(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL_TEXT2) + new Date(reportFile.lastModified()));
        }
      }
    });

    File imageInfoLocation = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), "InlineHelp16.png");
    ImageIcon imageInfo = new ImageIcon(imageInfoLocation.getPath());
    JLabel infoLabel = new JLabel(imageInfo, JLabel.HORIZONTAL);

    JPanel mainPanel = new JPanel(new GridBagLayout());
    JPanel bottomPanel = new JPanel(new GridBagLayout());

    mainPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(comboBox, new GridBagConstraints(1, 0, 1, 1, 1, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, 
        new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(folderButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(checkbox, new GridBagConstraints(0, 1, 3, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(bottomPanel, new GridBagConstraints(0, 2, 3, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, 
        new Insets(1, 1, 1, 1), 1, 1));

    bottomPanel.add(infoLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        new Insets(1, 1, 1, 1), 1, 1));
    bottomPanel.add(textInfo, new GridBagConstraints(1, 0, 1, 1, 1, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, 
        new Insets(1, 1, 1, 1), 1, 1));

    getContentPane().add(mainPanel, BorderLayout.CENTER);

    setPreferredSize(new Dimension(700, 200));
    setMinimumSize(getPreferredSize());
    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
    setVisible(true);  
  }

  @Override
  protected void doOK() {
    currentPath = (String) comboBox.getSelectedItem();
    // Update choosedZip 
    if (currentPath.equals(defaultPackageLocation.getPath())){
      choosedZip = defaultPackageLocation;
    } else if(isFromChooser && currentPath.equals(choosedZipFromChooser.getPath())){
      choosedZip = choosedZipFromChooser;
    } else {
      choosedZip = new File(currentPath);
      // Add .zip at the package name if the user forgot.
      if(!choosedZip.getName().endsWith(".zip")){
        logger.debug("The choosed file doesn't end with .zip");
        choosedZip = new File(choosedZip.getPath() + ".zip");
      }
      comboBox.addItem(choosedZip.getPath());
      comboItems.add(new ResourceInfo(choosedZip.getPath()));
    } 

    isSaveButtonPressed = true;
    // Generate report if the user selected the checkbox
    if(checkbox.isSelected()){    
      shouldCreateReport = true;
      new ReportGenerator(rootDir, modifiedResources, reportFile);
    }

    if(logger.isDebugEnabled()){
      logger.debug(resourceBundle.getMessage(Tags.REPORT_DIALOG_LOGGER_MESSAGE) + choosedZip.getAbsolutePath());
    }

    InfoResources choosedLocations = new InfoResources(comboItems);
    // Store the choosed locations in the "combo_options.xml" file.
    try {
      storeSelectedPaths(choosedLocations);
    } catch (FileNotFoundException e) {
      logger.error(e, e);
    } catch (JAXBException e) {
      logger.error(e, e);
    } catch (StoppedByUserException e) {
      logger.error(e, e);
    }
    logger.debug("The location of the combo_options.xml is : " + optionsFile.getAbsolutePath());
    
    super.doOK();
  }
  /**
   *  Saves the chosen paths for the creation of the archive on disk. 
   * 
   * @param info   An object of type InfoResources,this object will be serialized.
   * 
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException  The file doesn't exist.
   * @throws StoppedByUserException  The user pressed the cancel button.
   */
  private void storeSelectedPaths(InfoResources info) throws JAXBException, FileNotFoundException, StoppedByUserException{

    JAXBContext contextObj = JAXBContext.newInstance(InfoResources.class);  

    Marshaller marshallerObj = contextObj.createMarshaller();  
    marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);  

    marshallerObj.marshal(info, optionsFile);  

  }
  /**
   * Loads the last chosen paths for the package location from disk.
   * 
   * @return The content of the "combo_options.xml" file. A list with the absolute file paths of the package.
   * 
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws IOException  Problems reading the file/directory.
   */
  private ArrayList<ResourceInfo> loadSelectedPaths() throws JAXBException, IOException {
    if (!optionsFile.exists()) {
      throw new IOException("No optionsFile was created.");
    }

    JAXBContext jaxbContext = JAXBContext.newInstance(InfoResources.class); 

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();   

    InfoResources resources = (InfoResources) jaxbUnmarshaller.unmarshal(optionsFile);    

    return resources.getList();
  }

}
