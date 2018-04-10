package com.oxygenxml.translation.ui;

import com.oxygenxml.translation.support.core.MilestoneUtil;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.MilestoneGeneratorUtil;
import com.oxygenxml.translation.support.util.PackageGeneratorUtil;
import com.oxygenxml.translation.ui.worker.AbstractWorker;
import com.oxygenxml.translation.ui.worker.GenerateMilestoneWorker;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.util.URLUtil;

/**
 * 
 * @author adrian_sorop
 */
public class PackageAndUpdateMilestoneDialog extends OKCancelDialog {

  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(PackageAndUpdateMilestoneDialog.class.getName());
  
  private static final String GENERATE_PACKAGE_WITH_THE_PROJECT_FILES = "Generate package with the project files";

  private static final String BROWSE_MILESTONE_FILE = "Browse for milestone file";

  private static final String GENERATE_MILESTONE_FILE = "Generate milestone file";

  private static final String LOAD_FROM = "Load from:";
  
  private static final String SAVE_TO = "Save to:";

  private static final String CHECKBOX_HINT = "If the project files need to be translated, use this option also to "
      + "generate a package containing all of them, to be sent to translation.";
  
  private String generateHint = "Use this option if this is the first time you create a translation package for this project, " + 
      "or if the previously generated milestone is not accessible anymore.";
  
  private String browseHint = "Use this option if the milestone file was renamed or moved to a different location.";

  private String notFoundHint = "Unable to detect and load the milestone file needed to compute the modified files to be included in the package.";
  
  /**
   * Offer access to oXygen API.
   */
  private StandalonePluginWorkspace access;

  private JTextField saveField;

  
  /**
   * The last location where the dialog was presented.
   */
  private static Point lastLocation;
  
  public static PackageAndUpdateMilestoneDialog instance;

  private URL map;

  private JRadioButton generateMilestoneRadio;

  private JRadioButton browseForMilestoneRadio;

  private JCheckBox generatePackageWithProjectFile;

  private JTextField loadField;

  private ToolbarButton browseForSaveButton;

  private ToolbarButton loadButton;

  private PackageAndUpdateMilestoneDialog() {
    super((JFrame)PluginWorkspaceProvider.getPluginWorkspace().getParentFrame(), 
        "Generate package/milestone",
        true);
    
    init();
    
    if (lastLocation != null) {
      setLocation(lastLocation);
    }
    setLocationRelativeTo(super.getParent());
    setResizable(true);
    setPreferredSize(new Dimension(640, 350));
    setMinimumSize(new Dimension(getPreferredSize().width - 100, getPreferredSize().height));
    setMaximumSize(new Dimension(getPreferredSize().width + 300, getPreferredSize().height));
    setSize(getPreferredSize());
    
    addComponentListener(new ComponentAdapter(){
      @Override
      public void componentResized(ComponentEvent e){
          Dimension d = PackageAndUpdateMilestoneDialog.this.getSize();
          Dimension minD = PackageAndUpdateMilestoneDialog.this.getMinimumSize();
          if(d.width<minD.width) {
            d.width=minD.width;
          }
          if(d.height<minD.height) {
            d.height=minD.height;
          }
          
          Dimension maxD = PackageAndUpdateMilestoneDialog.this.getMaximumSize();
          if (d.width > maxD.width) {
            d.width = maxD.width;
          }
          if (d.height > maxD.height) {
            d.height = maxD.height;
          }
          
//          PackageAndUpdateMilestoneDialog.this.setSize(d);
      }
  });
  }
  
  
  public static final PackageAndUpdateMilestoneDialog getInstance() {
    if (instance == null) {
      instance = new PackageAndUpdateMilestoneDialog();
    }
    return instance;
  }
  
  public void showDialog(final StandalonePluginWorkspace access, URL map) {
    this.access = access;
    this.map = map;
    
    loadProposals();
    
    pack();
    setVisible(true);
    lastLocation = getLocation();
  }
  
  /**
   * In the save field propose a name for the milestone.  
   */
  private void loadProposals() {
    generateMilestoneRadio.setSelected(true);
    File proposal = MilestoneUtil.getMilestoneFile(map);
    saveField.setText(proposal.toURI().toString());
    
    DocumentListener listener = new DocumentListener() {
      @Override
      public void removeUpdate(final DocumentEvent e) {
        checkSanity();
      }
      @Override
      public void insertUpdate(final DocumentEvent e) {
        checkSanity();
      }
      @Override
      public void changedUpdate(final DocumentEvent e) {
        checkSanity();
      }
    };
    
    saveField.getDocument().addDocumentListener(listener);
    loadField.getDocument().addDocumentListener(listener);
    
    generateMilestoneRadio.addActionListener(e -> setComponentsStatus(true));
    browseForMilestoneRadio.addActionListener(e -> setComponentsStatus(false));
    setComponentsStatus(generateMilestoneRadio.isSelected());
    
  }
  
  /**
   * Checks if all the required input was provided. Enables/disables the OK button
   * based on that.
   */
  private void checkSanity() {
    boolean ok = 
        generateMilestoneRadio.isSelected() && 
        saveField.isEnabled() &&
        saveField.getText().trim().length() > 0;
        
    boolean ok2 = 
        browseForMilestoneRadio.isSelected() &&
        loadField.isEnabled() && 
        loadField.getText().trim().length() > 0;
        
    getOkButton().setEnabled(ok || ok2);   
  }
  
  private void setComponentsStatus(boolean status) {
    saveField.setEnabled(status);
    generatePackageWithProjectFile.setEnabled(status);
    browseForSaveButton.setEnabled(status);
    
    loadField.setEnabled(!status);
    loadButton.setEnabled(!status);
    // Default
    generatePackageWithProjectFile.setSelected(true);
    
    checkSanity();
  }

  /**
   * Initialize the layout.
   */
  private void init() {
    getContentPane().setLayout(new GridBagLayout());
    
    GridBagConstraints c = new GridBagConstraints();
    
    // "Unable to detect [...]" info message
    c.gridx = 0; 
    c.gridy = 0;
    c.gridwidth = 3;
    c.insets = new Insets(12, 2, 0, 2);
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(new WrappableLabel(notFoundHint), c);
//    getContentPane().add(addHintWithIcon(notFoundHint), c);
    
    // Generate milestone / Browse for milestone radio buttons
    ButtonGroup grup = new ButtonGroup();
    generateMilestoneRadio = new JRadioButton(GENERATE_MILESTONE_FILE);
    browseForMilestoneRadio = new JRadioButton(BROWSE_MILESTONE_FILE);
    grup.add(generateMilestoneRadio);
    grup.add(browseForMilestoneRadio);
    
    // Generate milestone radio button
    c.gridy ++;
    c.insets.top = 5;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    getContentPane().add(generateMilestoneRadio, c);
    
    // Generate milestone hint
    c.gridy ++;
    c.insets.left = 17;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
//    getContentPane().add(addHintWithIcon(generateHint), c);
    getContentPane().add(new WrappableLabel(generateHint), c);
    
    // "Save to" label
    c.gridy ++;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    getContentPane().add(new JLabel(SAVE_TO), c);
    
    // "Save to" input field
    c.gridx ++;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets.left = 2;
    saveField = new JTextField();
    saveField.setPreferredSize(new Dimension(300, saveField.getPreferredSize().height));
    getContentPane().add(saveField, c);
    
    // Browse button
    browseForSaveButton = new ToolbarButton(browseSaveFileAction(), false);
    Icon folderIcon = IconsUtil.getFolderIcon();
    browseForSaveButton.setIcon(folderIcon);
    c.gridx ++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    getContentPane().add(browseForSaveButton, c);
    
    // "Generate package [...]" check box
//    generatePackageWithProjectFile = new JCheckBox(GENERATE_PACKAGE_WITH_THE_PROJECT_FILES);
//    c.gridx = 0;
//    c.gridy ++;
//    c.gridwidth = 3;
//    c.insets.left = 17;
//    getContentPane().add(generatePackageWithProjectFile, c);
    
    // "Generate package [...]" hint
    
//    c.gridx = 0;
//    c.gridy ++;
//    c.insets.left = 18;
//    c.fill = GridBagConstraints.HORIZONTAL;
//    getContentPane().add(addHintWithIcon(CHECKBOX_HINT), c);
    
//    c.gridy ++;
//    c.weightx = 1;
//    c.insets.left = 35;
//    c.fill = GridBagConstraints.HORIZONTAL;
//    getContentPane().add(new WrappableLabel(CHECKBOX_HINT), c);
    
    /////////////// "Browse for milestone file" radio button //////////////
    c.gridx = 0;
    c.gridy ++;
    c.insets.left = 2;
    c.gridwidth = 3;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    getContentPane().add(browseForMilestoneRadio, c);
    
    // Hint
    c.gridy ++;
    c.insets.left = 17;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(new WrappableLabel(browseHint), c);
//    getContentPane().add(addHintWithIcon(browseHint), c);
    
    // "Load" label
    c.gridx = 0;
    c.gridy ++;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    getContentPane().add(new JLabel(LOAD_FROM), c);
    
    // "Load" input field
    c.gridx ++;
    c.insets.left = 2;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    loadField = new JTextField();
    loadField.setPreferredSize(new Dimension(300, loadField.getPreferredSize().height));
    getContentPane().add(loadField, c);
    
    // "Load" browse button
    c.gridx ++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    loadButton = new ToolbarButton(browseLoadFileAction(), false);
    loadButton.setIcon(folderIcon);
    getContentPane().add(loadButton, c);
    
    // Check box
    c.gridx = 0;
    c.gridy ++;
    c.gridwidth = 3;
    c.insets.left = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(new SectionPane("Package Generation"), c);
    
    generatePackageWithProjectFile = new JCheckBox(GENERATE_PACKAGE_WITH_THE_PROJECT_FILES);
    c.gridx = 0;
    c.gridy ++;
    c.gridwidth = 3;
    getContentPane().add(generatePackageWithProjectFile, c);

    // "Generate package [...]" hint
    
//    c.gridx = 0;
//    c.gridy ++;
//    c.insets.left = 18;
//    c.insets.bottom = 17;
//    c.fill = GridBagConstraints.HORIZONTAL;
//    getContentPane().add(addHintWithIcon(CHECKBOX_HINT), c);

    c.gridy ++;
    c.weightx = 1;
    c.insets.left = 17;
    c.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(new WrappableLabel(CHECKBOX_HINT), c);
    
    
  }


  private JPanel addHintWithIcon(String hintMessage) {
    Icon infoIcon = IconsUtil.getInfoIcon();
    JPanel jPanel = new JPanel();
    jPanel.setLayout(new GridBagLayout());
    GridBagConstraints gc = new GridBagConstraints();
    gc.anchor = GridBagConstraints.WEST;
    gc.gridx = 0; 
    gc.gridy = 0;
    gc.gridwidth = 2;
    jPanel.add(new JLabel(infoIcon), gc);
    
    gc.gridx ++;
    gc.weightx = 2;
    gc.insets.left = 20;
    gc.fill = GridBagConstraints.HORIZONTAL;
    jPanel.add(new WrappableLabel(hintMessage), gc);
    return jPanel;
  }

  private Action browseSaveFileAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        File chooseFile = access.chooseFile("Save", new String[] {"xml"}, "XML Files", true);
        if (chooseFile != null) {
          String decodeURIComponent = URLUtil.decodeURIComponent(chooseFile.toURI().toString());
          saveField.setText(decodeURIComponent);
        }
      }
    };
  }

  private Action browseLoadFileAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        File chooseFile = access.chooseFile("Open", new String[] {"xml"}, "XML Files");
        if (chooseFile != null) {
          String decodeURIComponent = URLUtil.decodeURIComponent(chooseFile.toURI().toString());
          loadField.setText(decodeURIComponent);
        }
      }
    };
  }
  
  @Override
  protected void doOK() {
    
    if (generateMilestoneRadio.isSelected()) {
      generateMilestone();
    } else {
      // Load milestone -> show modified files -> update milestone??
      String text = loadField.getText();
      URL milestoneURL = createFileFromUserInput(text, map);
      if (milestoneURL != null) {
        try {
          File milestoneFile = access.getUtilAccess().locateFile(milestoneURL);
          AbstractWorker worker = PackageGeneratorUtil.createModifiedFilesPackage(access, map, milestoneFile);
          worker.get();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }
      
    }
    
    super.doOK();
  }


  private void generateMilestone() {
    String text = saveField.getText();
    URL milestoneURL = createFileFromUserInput(text, map);
    if (milestoneURL != null) {
      File milestoneFile = access.getUtilAccess().locateFile(milestoneURL);
      GenerateMilestoneWorker generateMilestone = MilestoneGeneratorUtil.generateMilestone(access, map, milestoneFile, true);
      try {
        generateMilestone.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error(e, e);
      }
      
      if (generatePackageWithProjectFile.isSelected()) {
        createArchive(milestoneFile);
      }
    }
  }


  /**
   * Creates a file relative to current DITA map or a new file if the user the "browse" action.
   * @param text    
   * @param ditaMap
   * @return
   */
  private URL createFileFromUserInput(String text, URL ditaMap) {
    URL milestoneURL = null;
    try {
      milestoneURL = URLUtil.correct(new URL(text));
    } catch (MalformedURLException e) {
      try {
        if (!text.toLowerCase().endsWith(".xml")) {
          text = text + ".xml";
        }
        milestoneURL = URLUtil.correct(new URL(new URL(ditaMap.toExternalForm()), text));
      } catch (MalformedURLException e1) {
      }
    }
    return milestoneURL;
  }


  private void createArchive(File milestoneFile) {
    List<ResourceInfo> milestoneLinkedFiles = readMilestoneAndMarkAllAsModified(milestoneFile);
    String removeExtension = FilenameUtils.removeExtension(milestoneFile.getAbsolutePath()) + ".zip";
    PackageGeneratorUtil.createPackage(
        map, 
        new File(removeExtension), 
        access, 
        false, 
        milestoneLinkedFiles, 
        false);
  }

  private List<ResourceInfo> readMilestoneAndMarkAllAsModified(File milestoneFile) {
    List<ResourceInfo> milestoneLinkedFiles = MilestoneUtil.loadMilestoneContentFromFile(milestoneFile);
    if (milestoneLinkedFiles != null) {
      for (ResourceInfo resourceInfo : milestoneLinkedFiles) {
        resourceInfo.setMd5("");
      }
    }
    return milestoneLinkedFiles;
  }
}
