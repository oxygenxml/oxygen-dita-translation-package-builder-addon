package com.oxygenxml.translation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import com.oxygenxml.translation.support.TranslationPackageBuilderPlugin;
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
  private final File report;
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
    report = new File(rootDir, REPORT_FILE_NAME);
    // The default location of the package.
    choosedZip = new File(rootDir, ZIP_FILE_NAME);
    
    getOkButton().setText("Save");

    JLabel label = new JLabel("Package Location : ");
    checkbox = new JCheckBox("Generate report");
    //Show the user informations about the report creation.
    final JTextArea textInfo = new JTextArea(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL) + rootDir.getPath());
    textInfo.setWrapStyleWord(true);
    textInfo.setLineWrap(true);
   
    final JTextField packageLocation = new JTextField();
    packageLocation.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    packageLocation.setText(choosedZip.getPath());
    packageLocation.select(choosedZip.getPath().length() - choosedZip.getName().length(), choosedZip.getPath().length());
    packageLocation.setFocusable(true);
    
    final UndoManager undo = new UndoManager();
    Document textFieldDocument = packageLocation.getDocument();
    // Listen for undo and redo events
    textFieldDocument.addUndoableEditListener(new UndoableEditListener() {
        public void undoableEditHappened(UndoableEditEvent evt) {
            undo.addEdit(evt.getEdit());
        }
    });
    
    // Create an undo action and add it to the text component
    packageLocation.getActionMap().put("Undo",
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
    packageLocation.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
    
    // Create a redo action and add it to the text component
    packageLocation.getActionMap().put("Redo",
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
    packageLocation.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    
    textFieldDocument.addDocumentListener(new DocumentListener() {
      public void removeUpdate(DocumentEvent e) { }
      public void insertUpdate(DocumentEvent e) {
        //Update the package location if the user types another location in the text field.
        choosedZip = new File(packageLocation.getText());
        logger.debug("Choosed file from document listener insert : " + choosedZip.getPath());
      }
      public void changedUpdate(DocumentEvent e) { }
    });
    
    File imageFolderLocation = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), "Open16.png");
    ImageIcon image = new ImageIcon(imageFolderLocation.getPath());
    final ToolbarToggleButton folderButton = new ToolbarToggleButton(image);
    // Show a file chooser when the user clicks on the folder image.
    folderButton.addMouseListener(new MouseAdapter() {  
      public void mousePressed(MouseEvent e) {
        //Get  the location from the file chooser.
        choosedZip = pluginWorkspace.chooseFile(resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_TITLE),
            new String[] {"zip"},
            resourceBundle.getMessage(Tags.ACTION2_CHOOSE_FILE_DESCRIPTOR), 
            true);
        //Update the package location field with the choosed location from the file chooser.
        if(choosedZip != null){
          logger.debug("Clicked on folder image and choosed this archive : " + choosedZip.getPath());
          packageLocation.setText(choosedZip.getPath());
          packageLocation.select(choosedZip.getPath().length() - choosedZip.getName().length(), choosedZip.getPath().length());
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
          textInfo.setText(resourceBundle.getMessage(Tags.REPORT_DIALOG_LABEL_TEXT2) + new Date(report.lastModified()));
        }
      }
    });
    
    File imageInfoLocation = new File(TranslationPackageBuilderPlugin.getInstance().getDescriptor().getBaseDir(), "InlineHelp16.png");
    ImageIcon imageInfo = new ImageIcon(imageInfoLocation.getPath());
    JLabel infoLabel = new JLabel(imageInfo, JLabel.HORIZONTAL);
    
    JPanel mainPanel = new JPanel(new GridBagLayout());
    JPanel bottomPanel = new JPanel(new GridBagLayout());

    mainPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(packageLocation, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(folderButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(checkbox, new GridBagConstraints(0, 1, 3, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    mainPanel.add(bottomPanel, new GridBagConstraints(0, 2, 3, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 1, 1));
    
    bottomPanel.add(infoLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    bottomPanel.add(textInfo, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 1, 1));
    
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
    isSaveButtonPressed = true;
    
    if(checkbox.isSelected()){    
      shouldCreateReport = true;
      new ReportGenerator(rootDir, modifiedResources, report);
    }
    
    if(!choosedZip.getName().endsWith(".zip")){
      logger.debug("The choosed file doesn't end with .zip");
      choosedZip = new File(choosedZip.getPath() + ".zip");
    }
    if(logger.isDebugEnabled()){
      logger.debug(resourceBundle.getMessage(Tags.REPORT_DIALOG_LOGGER_MESSAGE) + choosedZip.getAbsolutePath());
    }
    
    super.doOK();
  }
}
