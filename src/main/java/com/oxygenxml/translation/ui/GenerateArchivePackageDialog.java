package com.oxygenxml.translation.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.TranslationPackageBuilderPlugin;
import com.oxygenxml.translation.support.storage.ComboHistory;
import com.oxygenxml.translation.support.storage.ComboItem;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.HistoryUtils;
import com.oxygenxml.translation.support.util.ProjectConstants;
import com.oxygenxml.translation.support.util.UndoRedoUtils;

import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarToggleButton;

/**
 * The dialog shown when the archive is prepared.
 */
public class GenerateArchivePackageDialog extends OKCancelDialog {
  /**
   *  Logger for logging.
   */
  private static Logger logger = Logger.getLogger(GenerateArchivePackageDialog.class); 
  /**
   *  Resource bundle.
   */
  private static final PluginResourceBundle messages = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();

  /**
   * The location chosen by the user for the archive.
   */
  private File chosenZip = null;
  /**
   * DITA Map file.
   */
  private File rootMapFile;
  /**
   * The location of the generated report.
   */
  private File reportFile;
  /**
   * The list with all the modified resources.
   */
  private ArrayList<ResourceInfo> modifiedResources;
  /**
   *  Where the package location is displayed.
   */
  private JComboBox<String> archiveLocationCombobox = new JComboBox<String>();
  /**
   * A list that contains all the chosen package locations.
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
   * The chosen location from the file chooser.
   */
  private File chosenZipFromChooser;
  /**
   * Information message where the information regarding the modified files will be presented.
   */
  private JLabel moreDetailsLabel;
  /**
   * More info about the modified files.
   */
  private JLabel modifiedFilesLabel;
  
  /**
   * Text area where the information about XHTML report are presented.
   */
  private JTextArea textInfo = new JTextArea();
  /**
   * Archive location field.
   */
  final JTextField locationField;
  /**
   * The generate report check box.
   */
  private final JCheckBox generateReportCheckbox;
  /**
   * Mouse Listener
   */
  private MouseListener mouseListener = new MouseAdapter() {
    @Override
    public void mouseEntered(MouseEvent e) {
      moreDetailsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    @Override
    public void mouseClicked(MouseEvent e) {
      //Show the list of modified files.
      try {
        ShowModifiedFilesDialog.getInstance().showDialog(modifiedResources);
      } catch (IOException e1) {
        logger.error(e1, e1);
      }
    }
  };
  /**
   * A ReportDialog instance.
   */
  private static GenerateArchivePackageDialog instance;
  /**
   * Creates a dialog where the user can choose the location of the modified resources archive
   *  and generate or not a report.
   */
  private GenerateArchivePackageDialog(JFrame parentFrame) {
    super(
        /*
         * Parent frame
         */
        parentFrame,
        /*
         * Title
         */
        messages.getMessage(Tags.PACKAGE_LOCATION),
        /*
         * Modal
         */
        true);
    
    // Set location
    setLocationRelativeTo(parentFrame);
    
    modifiedFilesLabel = new JLabel();
    moreDetailsLabel = new JLabel();
    // Add the listener once 
    moreDetailsLabel.addMouseListener(mouseListener);
    
    // Add into the comboBox the chosen paths stored in the optionsFile.
    ArrayList<ComboItem> savedPaths = HistoryUtils.loadSelectedPaths();
    if (savedPaths != null) {
      for (ComboItem resource : savedPaths) {
        archiveLocationCombobox.addItem(resource.getPath());
      }
    }

    //Show the user informations about the report creation.
    locationField = (JTextField) archiveLocationCombobox.getEditor().getEditorComponent();
    locationField.setPreferredSize(new Dimension(300, locationField.getPreferredSize().height));

    // Install the Undo/Redo support
    UndoRedoUtils.installUndoRedoSupport(locationField);

    File imageFolderLocation = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), "Open16.png");
    ImageIcon image = new ImageIcon(imageFolderLocation.getPath());
    final ToolbarToggleButton folderButton = new ToolbarToggleButton(image);
    // Show a file chooser when the user clicks on the folder image.
    folderButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         showArchiveSaveLocationChooser();
      }
    });
    
    // The default location of the report file.
    generateReportCheckbox = new JCheckBox("Generate report");

    File imageInfoLocation = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), "InlineHelp16.png");
    ImageIcon imageInfo = new ImageIcon(imageInfoLocation.getPath());
    JLabel infoLabel = new JLabel(imageInfo, JLabel.HORIZONTAL);

    JPanel mainPanel = new JPanel(new GridBagLayout());
    JPanel infoPanel = new JPanel(new GridBagLayout());
    
    JLabel label = new JLabel("Package Location: ");
    Insets insets = new Insets(2, 2, 2, 2);
    
    mainPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        insets, 1, 1));
    mainPanel.add(archiveLocationCombobox, new GridBagConstraints(1, 0, 1, 1, 1, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, 
        insets, 1, 1));
    mainPanel.add(folderButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        insets, 1, 1));
    mainPanel.add(generateReportCheckbox, new GridBagConstraints(0, 1, 3, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        insets, 1, 1));
    mainPanel.add(infoPanel, new GridBagConstraints(0, 2, 3, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        insets, 1, 1));

    infoPanel.add(infoLabel, new GridBagConstraints(0, 0, 1, 2, 0, 0, 
        GridBagConstraints.NORTH, 
        GridBagConstraints.NONE, 
        insets, 1, 1));
    infoPanel.add(textInfo, new GridBagConstraints(1, 0, 1, 1, 1, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, 
        insets, 1, 1));
    infoPanel.add(modifiedFilesLabel, new GridBagConstraints(1, 1, 1, 1, 0, 0, 
      GridBagConstraints.WEST, 
      GridBagConstraints.NONE, 
      insets, 1, 1));
    infoPanel.add(moreDetailsLabel, new GridBagConstraints(1, 2, 1, 1, 0, 0, 
        GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        insets, 1, 1));
    
    getContentPane().add(mainPanel, BorderLayout.CENTER);
  }
  
  /**
   * @return The location where the archive will be saved.
   */
  private String showArchiveSaveLocationChooser() {
    //Get the location from the file chooser.
    chosenZipFromChooser = PluginWorkspaceProvider.getPluginWorkspace().chooseFile(
        messages.getMessage(Tags.PACKAGE_LOCATION),
        new String[] {"zip"},
        messages.getMessage(Tags.ZIP_FILES), 
        true);
    //Update the package location field with the choosed location from the file chooser.
    if(chosenZipFromChooser != null){
      currentPath = chosenZipFromChooser.getPath();
      archiveLocationCombobox.setSelectedItem(currentPath);
      locationField.select(currentPath.length() - chosenZipFromChooser.getName().length(), 
          currentPath.length() - ProjectConstants.ZIP_FILE_SUFFIX.length());
    }
    return currentPath;
  }
  
  /**
   * Creates a dialog object if it wasn't created before.
   * 
   * @return An instance of the dialog which will allow users to save the package with modified files.
   */
  public static GenerateArchivePackageDialog getInstance(){
    if(instance == null){
      instance = new GenerateArchivePackageDialog((JFrame) PluginWorkspaceProvider.getPluginWorkspace().getParentFrame());
    }
    return instance;
  }
  
  /**
   * Initialize layout and variables and shows the dialog on the screen. 
   * 
   * @param modifiedRes An array with the modified resources.
   * @param rootMap     A reference to the dita map file.
   */
  public void showDialog(ArrayList<ResourceInfo> modifiedRes, File rootMap) {
    // Assign to field.
    this.modifiedResources = modifiedRes;
    this.rootMapFile = rootMap;
    
    reportFile = new File(rootMapFile.getParentFile(), ProjectConstants.getHTMLReportFile(rootMapFile));
    
    initModifiedFilesInfo();
    initReportFilesInfo();
    initArchiveLocation();
    
    getOkButton().setText(messages.getMessage(Tags.SAVE));
    
    setPreferredSize(new Dimension(1024, 330));
    setMinimumSize(getPreferredSize());
    pack();
    setResizable(true);
    
    instance.setVisible(true);
  }
  
  /**
   * Configure the text area which presents the informations about modified files.
   */
  @SuppressWarnings("unchecked")
  private void initModifiedFilesInfo() {
    StringBuilder text = new StringBuilder();
    text.append("(").append(modifiedResources.size()).append(")");
    text.append(" files were modified since ");
    text.append(new Date(reportFile.lastModified()));
    modifiedFilesLabel.setText(text.toString());
    text.setLength(0);
    text.append("More details...");
    moreDetailsLabel.setText(text.toString());
    moreDetailsLabel.setToolTipText("Click to see the list of files which will be archived.");
    
    Font font = moreDetailsLabel.getFont();
    Map attributes = font.getAttributes();
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
    moreDetailsLabel.setFont(font.deriveFont(attributes));
  }
  
  /**
   * Configure the components which presents the informations about report file.
   */
  private void initReportFilesInfo() {
    String text = messages.getMessage(Tags.REPORT_DIALOG_LABEL) + ProjectConstants.getHTMLReportFile(rootMapFile);
    textInfo.setText(text);
    textInfo.setWrapStyleWord(true);
    textInfo.setLineWrap(true);
    
    generateReportCheckbox.setSelected(true);
    generateReportCheckbox.setToolTipText(messages.getMessage(Tags.REPORT_DIALOG_CHECKBOX_TOOLTIP));
    
    generateReportCheckbox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED){
          textInfo.setText(messages.getMessage(Tags.REPORT_DIALOG_LABEL) + ProjectConstants.getHTMLReportFile(rootMapFile));         
        } else {
          textInfo.setText(messages.getMessage(Tags.REPORT_DIALOG_LABEL_TEXT2) + new Date(reportFile.lastModified()));
        }
      }
    });
  }
  
  private void initArchiveLocation() {
    
    // The default location of the package.
    defaultPackageLocation = new File(rootMapFile.getParent(), ProjectConstants.getZipFileName(rootMapFile));
    currentPath = defaultPackageLocation.getPath();
    archiveLocationCombobox.setEditable(true);
    archiveLocationCombobox.setMaximumRowCount(4);
    archiveLocationCombobox.setSelectedItem(currentPath);
    int selectionStart = currentPath.length() - defaultPackageLocation.getName().length();
    int selectionEnd = currentPath.length() - ProjectConstants.ZIP_FILE_SUFFIX.length();
    // Move caret to that position
    locationField.setCaretPosition(selectionStart);
    // Select text
    locationField.select(selectionStart,selectionEnd);
  }
    
  @Override
  protected void doOK() {
    // The selected path from the comboBox.
    currentPath = ((JTextComponent) archiveLocationCombobox.getEditor().getEditorComponent()).getText();
    chosenZip = new File(currentPath);
    //Add .zip at the package name if the user forgot.
    if(!chosenZip.getName().endsWith(ProjectConstants.ZIP_FILE_EXTENSION)){
      chosenZip = new File(chosenZip.getPath() + ".zip");
    }
    
    if (chosenZip.exists()) {
      int response = JOptionPane.showConfirmDialog(this, "Override " + chosenZip.getName() + "?", "Confirm Override", JOptionPane.YES_NO_OPTION);
      if (response == JOptionPane.NO_OPTION) {
        chosenZip = new File(showArchiveSaveLocationChooser());
      }
    }
    
    // Find out if the currentPath is in the comboBox model.
    boolean isInModel = false;
    ComboBoxModel<String> model = archiveLocationCombobox.getModel();
    int size = model.getSize();
    for (int i = 0; i < size; i++) {
      if(model.getElementAt(i).equals(currentPath)){
        isInModel = true;
        break;
      }
    }
    // If it's not, add it.
    if(!isInModel){
      archiveLocationCombobox.addItem(currentPath);
      ArrayList<ComboItem> loadedPaths = HistoryUtils.loadSelectedPaths();
      if (loadedPaths != null) {
        comboItems.addAll(loadedPaths);
      }

      comboItems.add(new ComboItem(currentPath));
      /*
       * Add the current model list to the object that will be serialized
       * and store the location.
       */
      HistoryUtils.storeSelectedPaths(new ComboHistory(comboItems));
    }
    
    // Generate report if the user selected the checkbox
    if(generateReportCheckbox.isSelected()){    
      new ReportGenerator(rootMapFile, modifiedResources, reportFile);
    }

    if(logger.isDebugEnabled()){
      logger.debug(messages.getMessage(Tags.REPORT_DIALOG_LOGGER_MESSAGE) + chosenZip.getAbsolutePath());
    }
    super.doOK();
  }
  
  /**
   * @return Zip file.
   */
  public File getChoosedLocation(){
    return chosenZip;
  }

  /**
   * @return <code>true</code> if an xhtml report should be created.
   */
  public boolean generateXHTMLReport() {
    return generateReportCheckbox.isSelected();
  }
  
}
