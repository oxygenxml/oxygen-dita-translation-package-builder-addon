package com.oxygenxml.translation.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.TranslationPackageBuilderPlugin;
import com.oxygenxml.translation.support.core.models.ComboHistory;
import com.oxygenxml.translation.support.core.models.ComboItem;
import com.oxygenxml.translation.support.core.models.ResourceInfo;

import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
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
  private static File rootDir;
  public static void setRootDir(File rootDir) {
    ReportDialog.rootDir = rootDir;
  }
  /**
   * The location of the generated report.
   */
  private final File reportFile;
  /**
   * The list with all the modified resources.
   */
  private static ArrayList<ResourceInfo> modifiedResources;
  public static void setModifiedResources(ArrayList<ResourceInfo> modifiedResources) {
    ReportDialog.modifiedResources = modifiedResources;
  }
  /**
   * True if the user pressed the "Save" button, false otherwise.
   */
  private boolean isSaveButtonPressed = false;
  public boolean isSaveButtonPressed() {
    return isSaveButtonPressed;
  }
  public void setSaveButtonPressed(boolean isSaveButtonPressed) {
    this.isSaveButtonPressed = isSaveButtonPressed;
  }
  /**
   *  Where the package location is displayed.
   */
  private JComboBox<String> comboBox = new JComboBox<String>();
  /**
   * A list that contains all the choosed package locations.
   */
  private ArrayList<ComboItem> comboItems = new ArrayList<ComboItem>();
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
   * The parent frame of the dialog.
   */
  private static Frame parentFrame;
  public static void setParentFrame(Frame parentFrame) {
    ReportDialog.parentFrame = parentFrame;
  }
  /**
   * A ReportDialog instance.
   */
  private static ReportDialog instance;
  /**
   * Creates a dialog where the user can choose the location of the modified resources archive
   *  and generate or not a report.
   */
  private ReportDialog() {
    //--------------------------------------------------------------------------- modal
    super(parentFrame, resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE), true);
    // The default location of the report file.
    reportFile = new File(rootDir, REPORT_FILE_NAME);
    // The default location of the package.
    defaultPackageLocation = new File(rootDir, ZIP_FILE_NAME);
    // Add into the comboBox the choosed paths stored in the optionsFile.
    try {
      ArrayList<ComboItem> savedPaths = loadSelectedPaths();
      for (ComboItem resource : savedPaths) {
        comboBox.addItem(resource.getPath());
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
    final JTextArea textInfo = new JTextArea(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL) + new File(rootDir, REPORT_FILE_NAME).getAbsolutePath());
    textInfo.setWrapStyleWord(true);
    textInfo.setLineWrap(true);

    final JTextField textfield = (JTextField) comboBox.getEditor().getEditorComponent();
    currentPath = defaultPackageLocation.getPath();
    comboBox.setEditable(true);
    comboBox.setMaximumRowCount(4);
    comboBox.setSelectedItem(currentPath);
    textfield.select(currentPath.length() - defaultPackageLocation.getName().length(), currentPath.length());

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
    folderButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //Get the location from the file chooser.
        choosedZipFromChooser = pluginWorkspace.chooseFile(resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE),
            new String[] {"zip"},
            resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_DESCRIPTOR), 
            true);
        //Update the package location field with the choosed location from the file chooser.
        if(choosedZipFromChooser != null){
          logger.debug("Clicked on folder image and choosed this archive : " + choosedZipFromChooser.getPath());
          currentPath = choosedZipFromChooser.getPath();
          comboBox.setSelectedItem(currentPath);
          textfield.select(currentPath.length() - choosedZipFromChooser.getName().length(), currentPath.length());
        }
      }
    });

    checkbox.setSelected(true);
    checkbox.setToolTipText(resourceBundle.getMessage(Tags.REPORT_DIALOG_CHECKBOX_TOOLTIP));

    checkbox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED){
          textInfo.setText(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL) + new File(rootDir, REPORT_FILE_NAME).getAbsolutePath());         
        } else {
          textInfo.setText(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL_TEXT2) + new Date(reportFile.lastModified()));
        }
      }
    });

    File imageInfoLocation = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), "InlineHelp16.png");
    ImageIcon imageInfo = new ImageIcon(imageInfoLocation.getPath());
    JLabel infoLabel = new JLabel(imageInfo, JLabel.HORIZONTAL);

    JPanel mainPanel = new JPanel(new GridBagLayout());
    JPanel infoPanel = new JPanel(new GridBagLayout());
    
    final JTextArea modifiedFilesInfo = new JTextArea();
    modifiedFilesInfo.setEditable(false);
    modifiedFilesInfo.setText("A number of " + modifiedResources.size() +
        " files was modified since the last time the milestone was computed. More details...");
    modifiedFilesInfo.setLineWrap(true);
    modifiedFilesInfo.setWrapStyleWord(true);
    modifiedFilesInfo.setToolTipText("Click to see the list of files which will be packed in the zip.");
    
    modifiedFilesInfo.addMouseListener(new MouseListener() {
      public void mouseReleased(MouseEvent e) {  }
      public void mousePressed(MouseEvent e) {   }
      public void mouseExited(MouseEvent e) {  }
      public void mouseEntered(MouseEvent e) { 
        modifiedFilesInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
      public void mouseClicked(MouseEvent e) { 
      //Show the list of modified files.
        logger.debug("Show list of modified files.");
        
        try {
          showReport(pluginWorkspace, modifiedResources);
        } catch (IOException e1) {
          logger.error(e1, e1);
        }
      }
    });
    
//    JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
//    JPanel generateMilestonePanel = new JPanel(new GridBagLayout());
//    File milestoneFile = new File(rootDir, PackageBuilder.getMilestoneFileName());
//    String answer = "";
//    if(milestoneFile.exists()){
//      answer = "YES";
//    } else {
//      answer = "NO";
//    }
//    JLabel milestoneLabel = new JLabel("Is the milestone file created ? Answer : " + answer);
//    JLabel questionLabel = new JLabel("Do you want to generate/override the milestone file?");
//    JRadioButton radioButtonYES = new JRadioButton("YES");
//    JRadioButton radioButtonNO = new JRadioButton("NO");
  
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
//    mainPanel.add(separator, new GridBagConstraints(0, 2, 3, 1, 1, 0, 
//        GridBagConstraints.WEST, 
//        GridBagConstraints.HORIZONTAL, 
//        new Insets(1, 1, 1, 1), 1, 1));
//    mainPanel.add(generateMilestonePanel, new GridBagConstraints(0, 4, 3, 1, 0, 0, 
//        GridBagConstraints.WEST, 
//        GridBagConstraints.NONE, 
//        new Insets(1, 1, 1, 1), 1, 1));
//    mainPanel.add(separator, new GridBagConstraints(0, 5, 3, 1, 1, 0, 
//        GridBagConstraints.WEST, 
//        GridBagConstraints.HORIZONTAL, 
//        new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(infoPanel, new GridBagConstraints(0, 2, 3, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        new Insets(1, 1, 1, 1), 1, 1));

    infoPanel.add(infoLabel, new GridBagConstraints(0, 0, 1, 2, 0, 0, 
        GridBagConstraints.NORTH, 
        GridBagConstraints.NONE, 
        new Insets(1, 1, 1, 1), 1, 1));
    infoPanel.add(textInfo, new GridBagConstraints(1, 0, 1, 1, 1, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, 
        new Insets(1, 1, 1, 1), 1, 1));
    infoPanel.add(modifiedFilesInfo, new GridBagConstraints(1, 1, 1, 1, 0, 0, 
      GridBagConstraints.WEST, 
      GridBagConstraints.NONE, 
      new Insets(1, 1, 1, 1), 1, 1));
    
//    generateMilestonePanel.add(milestoneLabel, new GridBagConstraints(0, 0, 2, 1, 1, 0, 
//        GridBagConstraints.NORTH, 
//        GridBagConstraints.NONE, 
//        new Insets(1, 1, 1, 1), 1, 1));
//    generateMilestonePanel.add(questionLabel, new GridBagConstraints(0, 1, 2, 1, 0, 0, 
//        GridBagConstraints.NORTH, 
//        GridBagConstraints.NONE, 
//        new Insets(1, 1, 1, 1), 1, 1));
//    generateMilestonePanel.add(radioButtonYES, new GridBagConstraints(0, 2, 1, 1, 0, 0, 
//        GridBagConstraints.NORTH, 
//        GridBagConstraints.NONE, 
//        new Insets(1, 1, 1, 1), 1, 1));
//    generateMilestonePanel.add(radioButtonNO, new GridBagConstraints(1, 2, 1, 1, 0, 0, 
//        GridBagConstraints.NORTH, 
//        GridBagConstraints.NONE, 
//        new Insets(1, 1, 1, 1), 1, 1));
    
    getContentPane().add(mainPanel, BorderLayout.CENTER);

    setPreferredSize(new Dimension(500, 210));
    setMinimumSize(getPreferredSize());
    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
  }

  @Override
  protected void doOK() {
    // The selected path from the comboBox.
    currentPath = ((JTextComponent) comboBox.getEditor().getEditorComponent()).getText();
    choosedZip = new File(currentPath);
    //Add .zip at the package name if the user forgot.
    if(!choosedZip.getName().endsWith(".zip")){
      logger.debug("The choosed file doesn't end with .zip");
      choosedZip = new File(choosedZip.getPath() + ".zip");
    }
    // Find out if the currentPath is in the comboBox model.
    boolean isInModel = false;
    ComboBoxModel<String> model = comboBox.getModel();
    int size = model.getSize();
    for (int i = 0; i < size; i++) {
      if(model.getElementAt(i).equals(currentPath)){
        isInModel = true;
        break;
      }
    }
    // If it's not, add it.
    if(!isInModel){
      comboBox.addItem(currentPath);
      try {
       comboItems = loadSelectedPaths();
      } catch (JAXBException e) {
        logger.error(e, e);
      } catch (IOException e) {
        logger.error(e, e);
      }
      comboItems.add(new ComboItem(currentPath));
      // Add the current model list to the object that will be serialized.
      ComboHistory choosedLocations = new ComboHistory(comboItems);
      // Store the choosed locations.
      try {
        storeSelectedPaths(choosedLocations);
      } catch (FileNotFoundException e) {
        logger.error(e, e);
      } catch (JAXBException e) {
        logger.error(e, e);
      } catch (StoppedByUserException e) {
        logger.error(e, e);
      }
      logger.debug("It's not in the model, we added : " + currentPath);
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
    super.doOK();
  }
  /**
   *  Save and persist the chosen paths in the Oxygen preferences user-defined keys and values.
   * 
   * @param info   An object of type ComboHistory, this object will be serialized.
   * 
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws FileNotFoundException  The file doesn't exist.
   * @throws StoppedByUserException  The user pressed the cancel button.
   */
  private void storeSelectedPaths(ComboHistory info) throws JAXBException, FileNotFoundException, StoppedByUserException{
    JAXBContext contextObj = JAXBContext.newInstance(ComboHistory.class);  

    Marshaller marshallerObj = contextObj.createMarshaller();  
    marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);  

    StringWriter sw = new StringWriter();
    marshallerObj.marshal(info, sw);
    
    WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
    optionsStorage.setOption("REPORT_DIALOG_HISTORY", sw.toString());
  }
  /**
   * Loads chosen paths for the package location from disk.
   * 
   * @return A list with the absolute file paths of the package.
   * 
   * @throws JAXBException  Problems with JAXB, serialization/deserialization of a file.
   * @throws IOException  Problems reading the file/directory.
   */
  private ArrayList<ComboItem> loadSelectedPaths() throws JAXBException, IOException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ComboHistory.class); 

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    
    WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
    String option = optionsStorage.getOption("REPORT_DIALOG_HISTORY", null);
    
    if (option == null) {
      logger.debug("The option is null!");
      throw new IOException("No optionsFile was created.");
    }
    ComboHistory resources = (ComboHistory) jaxbUnmarshaller.unmarshal(new StringReader(option));    

    return resources.getEntries();
  }
  /**
   * Creates a ReportDialog object if it wasn't created before.
   * 
   * @return A ReportDialog instance.
   */
  public static ReportDialog getInstance(){
    if(instance == null){
      instance = new ReportDialog();
    }
    instance.setVisible(true);
    return instance;
  }
  
  /**
   *  Shows a message dialog with the modified files that will be packed.
   * 
   * @param pluginWorkspaceAccess  Entry point for accessing the DITA Maps area.
   * @param list  The relative paths of the modified files.
   * @throws IOException  Problems reading the files.
   */
  private void showReport(final StandalonePluginWorkspace pluginWorkspaceAccess,
      ArrayList<ResourceInfo> list) throws IOException {
//    final PluginResourceBundle resourceBundle = pluginWorkspaceAccess.getResourceBundle();

    // Present a log with the overridden files.
    if(list != null && !list.isEmpty()){
      JTextArea text = new JTextArea(10, 40);

      // Iterate with an index and put a new line
      //    for all lines except the first one. 
      text.append(list.get(0).getRelativePath());
      for(int i = 1; i < list.size(); i++){
        text.append("\n");
        text.append(list.get(i).getRelativePath());
      }
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);

      JScrollPane scroll = new JScrollPane(text);
      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

      JPanel panel = new JPanel(new GridBagLayout());

      GridBagConstraints gbcScroll = new GridBagConstraints();
      gbcScroll.gridx = 0;
      gbcScroll.gridy = 1;
      gbcScroll.gridwidth = 1;
      gbcScroll.gridheight = 1;
      gbcScroll.weightx = 0;
      gbcScroll.weighty = 0;
      gbcScroll.fill = GridBagConstraints.BOTH;
      gbcScroll.anchor = GridBagConstraints.LINE_START;
      panel.add(scroll , gbcScroll);

      JOptionPane.showMessageDialog((JFrame) pluginWorkspaceAccess.getParentFrame(), panel, "Files which will be packed", JOptionPane.INFORMATION_MESSAGE);
    }
    else{
      throw new IOException("The list containing the modified files is empty or null.");
    }
  }
}
