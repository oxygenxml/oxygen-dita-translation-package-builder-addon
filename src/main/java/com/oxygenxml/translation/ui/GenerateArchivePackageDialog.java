package com.oxygenxml.translation.ui;

import com.oxygenxml.translation.support.storage.ComboHistory;
import com.oxygenxml.translation.support.storage.ComboItem;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.FontProperties;
import com.oxygenxml.translation.support.util.HistoryUtils;
import com.oxygenxml.translation.support.util.ProjectConstants;
import com.oxygenxml.translation.support.util.UndoRedoUtils;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.apache.log4j.Logger;
import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarToggleButton;

/**
 * The dialog shown when the archive is prepared.
 */
public class GenerateArchivePackageDialog extends OKCancelDialog /*NOSONAR*/{
  
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
  private List<ResourceInfo> modifiedResources; /*NOSONAR*/
  
  /**
   *  Where the package location is displayed.
   */
  private JComboBox<String> archiveLocationCombobox = new JComboBox<>();
  
  /**
   * A list that contains all the chosen package locations.
   */
  private List<ComboItem> comboItems = new ArrayList<ComboItem>(); /*NOSONAR*/
  
  /**
   * The selected path, the one that appears in the comboBox.
   */
  private String currentPath;
  
  /**
   * Information message where the information regarding the modified files will be presented.
   */
  private JLabel moreDetailsLabel;
  /**
   * More info about the modified files.
   */
  private JLabel modifiedFilesLabel;
  
  /**
   * Where the information about XHTML report are presented.
   */
  private JLabel textInfo = new JLabel();
  
  /**
   * Archive location field.
   */
  final JTextField locationField;
  /**
   * The generate report check box.
   */
  private final JCheckBox generateReportCheckbox;
  
  /**
   * The format of the date.
   */
  private static final String DATE_FORMAT = "yyyy.MM.dd 'at' HH:mm"; 
  
  /**
   * Mouse Listener
   */
  private MouseListener mouseListener = new MouseAdapter() /*NOSONAR*/ {
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
    
    
    modifiedFilesLabel = new JLabel();
    moreDetailsLabel = new JLabel();
    // Add the listener once 
    moreDetailsLabel.addMouseListener(mouseListener);
    
    // Add into the comboBox the chosen paths stored in the optionsFile.
    List<ComboItem> savedPaths = HistoryUtils.loadSelectedPaths();
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
    
    URL resource = PluginWorkspaceProvider.class.getClassLoader().getResource(Icons.OPEN_DIRECTOR_ICON);
    Icon image = null;
    if(resource != null) {
       image = (Icon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities().loadIcon(resource);
    }
    
    ToolbarToggleButton folderButton = null;
    if(image != null) {
      folderButton = new ToolbarToggleButton(image);
      // Show a file chooser when the user clicks on the folder image.
      folderButton.addActionListener(e -> {
        File location = showArchiveSaveLocationChooser();
        if (location != null) {
          chosenZip = location;
          currentPath = chosenZip.getPath();
          archiveLocationCombobox.setSelectedItem(currentPath);
          locationField.select(currentPath.length() - location.getName().length(), 
              currentPath.length() - ProjectConstants.ZIP_FILE_SUFFIX.length());
        }
      });
    }
    
    // The default location of the report file.
    generateReportCheckbox = new JCheckBox("Generate report");

    getContentPane().setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    
    gbc.gridx = 0; 
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    getContentPane().add(new JLabel("Package Location: "), gbc);
    
    gbc.gridx = 1; 
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    getContentPane().add(archiveLocationCombobox, gbc);
    
    gbc.gridx = 2;
    gbc.weightx = 0;
    if(folderButton != null) {
      getContentPane().add(folderButton, gbc);
    }
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 3;
    generateReportCheckbox.setBorder(null);
    gbc.insets = new Insets(5, 0, 5, 0);
    getContentPane().add(generateReportCheckbox, gbc);
    
    gbc.gridy++;
    gbc.weighty = 1;    
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.NONE;
    getContentPane().add(createInfoPanel(), gbc);
    
  }


  /**
   * Create a panel that contains the information about the modified files.
   * @return The panel.
   */
  private JPanel createInfoPanel() {
    ImageIcon infoIcon = null;
    URL resource = getClass().getClassLoader().getResource(Icons.INFO_ICON);
    if(resource != null) {
      infoIcon = (ImageIcon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities().loadIcon(resource);
    }
    
    JPanel infoPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    
    // add the info icon.
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 3;
    gbc.insets.right = 5;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    if(infoIcon != null) {
      infoPanel.add(new JLabel(infoIcon), gbc);
    }

    gbc.gridx = 1;
    gbc.insets.right = 0;
    gbc.gridheight = 1;
    infoPanel.add(textInfo, gbc);

    gbc.gridy++;
    infoPanel.add(modifiedFilesLabel, gbc);

    gbc.gridy++;
    infoPanel.add(moreDetailsLabel, gbc);

    return infoPanel;
  }
  
  
  /**
   * @return The location where the archive will be saved.
   */
  private File showArchiveSaveLocationChooser() {
    //Get the location from the file chooser.
    return PluginWorkspaceProvider.getPluginWorkspace().chooseFile(
        messages.getMessage(Tags.PACKAGE_LOCATION),
        new String[] {"zip"},
        messages.getMessage(Tags.ZIP_FILES), 
        true);
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
  public void showDialog(List<ResourceInfo> modifiedRes, File rootMap) {
    // Assign to field.
    this.modifiedResources = modifiedRes;
    this.rootMapFile = rootMap;
    
    File parentFile = rootMapFile.getParentFile();
    reportFile = new File(parentFile, ProjectConstants.getHTMLReportFile(rootMapFile));
    
    initModifiedFilesInfo();
    initReportFilesInfo();
    initArchiveLocation();
    
    getOkButton().setText(messages.getMessage(Tags.SAVE));
    setResizable(true);
    pack();
    setMinimumSize(getSize());
    // Set location
    setLocationRelativeTo(super.getParent());
    
    instance.setVisible(true);
  }
  
  /**
   * Configure the text area which presents the informations about modified files.
   */
  private void initModifiedFilesInfo() {
    StringBuilder text = new StringBuilder();
    text.append("(").append(modifiedResources.size()).append(")");
    text.append(" files were modified since ");
    Date date = new Date(reportFile.lastModified());
    SimpleDateFormat dataFormat = new SimpleDateFormat(DATE_FORMAT);
    text.append(dataFormat.format(date));
    modifiedFilesLabel.setText(text.toString());
    
    text.setLength(0);
    text.append("More details...");
    moreDetailsLabel.setText(text.toString());
    moreDetailsLabel.setToolTipText("Click to see the list of files which will be archived.");
    
    Font font = moreDetailsLabel.getFont();
    moreDetailsLabel.setFont(font.deriveFont(FontProperties.UNDERLINED_TEXT_ATTRIBUTES_MAP));
  }
  
  /**
   * Configure the components which presents the informations about report file.
   */
  private void initReportFilesInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append(messages.getMessage(Tags.XHTML_REPORT_LOCATION)).append(" ");
    sb.append(ProjectConstants.getHTMLReportFile(rootMapFile));
    textInfo.setText(sb.toString());

    generateReportCheckbox.setSelected(true);
    generateReportCheckbox.setToolTipText(messages.getMessage(Tags.REPORT_DIALOG_CHECKBOX_TOOLTIP));
    
    generateReportCheckbox.addItemListener(e -> {
      if(e.getStateChange() == ItemEvent.SELECTED){
        textInfo.setText(messages.getMessage(Tags.XHTML_REPORT_LOCATION) + ProjectConstants.getHTMLReportFile(rootMapFile));         
      } else {
        Date date = new Date(reportFile.lastModified());
        SimpleDateFormat dataFormat = new SimpleDateFormat(DATE_FORMAT);
        textInfo.setText(messages.getMessage(Tags.LAST_REPORT_CREATION_TIME) +" "+ dataFormat.format(date));
      }
    });
  }
  
  private void initArchiveLocation() {
    // The default location of the package.
    File defaultPackageLocation = new File(rootMapFile.getParent(), ProjectConstants.getZipFileName(rootMapFile));
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
    // TODO Adrian Check that we have a file specified.
    currentPath = ((JTextComponent) archiveLocationCombobox.getEditor().getEditorComponent()).getText();
    
    chosenZip = new File(currentPath);
    
    //Add .zip at the package name if the user forgot.
    if(!chosenZip.getName().endsWith(ProjectConstants.ZIP_FILE_EXTENSION)){
      chosenZip = new File(chosenZip.getPath() + ".zip");
    }
    
    int response = -1;
    if (chosenZip.exists()) {
      response = JOptionPane.showConfirmDialog(
          this, 
          "Override " + chosenZip.getName() + "?", 
          "Confirm Override", 
          JOptionPane.YES_NO_OPTION);
    }
    
    // Find out if the currentPath is in the comboBox model.
    boolean isInModel = false;
    ComboBoxModel<String> model = archiveLocationCombobox.getModel();
    int size = model.getSize();
    for (int i = 0; i < size; i++) {
      String elementAt = model.getElementAt(i);
      if(elementAt != null && elementAt.equals(currentPath)){
        isInModel = true;
        break;
      }
    }
    // If it's not, add it.
    if(!isInModel){
      archiveLocationCombobox.addItem(currentPath);
      List<ComboItem> loadedPaths = HistoryUtils.loadSelectedPaths();
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
    if(generateReportCheckbox.isSelected() && getResult() == RESULT_OK){    
      new ReportGenerator(rootMapFile, modifiedResources, reportFile);
    }

    if(logger.isDebugEnabled()){
      logger.debug(messages.getMessage(Tags.REPORT_DIALOG_LOGGER_MESSAGE) + chosenZip.getAbsolutePath());
    }
    
    
    if (response == JOptionPane.YES_OPTION || 
        response == -1) {
      super.doOK();
    }
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
