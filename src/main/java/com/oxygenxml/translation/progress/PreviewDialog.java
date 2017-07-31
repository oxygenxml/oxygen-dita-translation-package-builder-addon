package com.oxygenxml.translation.progress;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.oxygenxml.translation.progress.worker.CopyDirectoryWorker;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;
/**
 *  The dialog that shows the preview before applying a package.
 * 
 * @author Bivolan Dalina
 *
 */
public class PreviewDialog extends OKCancelDialog {
  /**
   * Logger for logging.
   */
  private static Logger logger = Logger.getLogger(PreviewDialog.class); 
  /**
   *  The list that contains the relative paths of every file in the package.
   */
  @SuppressWarnings("unused")
  private ArrayList<String> filePaths = new ArrayList<String>();
  /**
   *  The relative paths of the unzipped files.
   */
  private JList<String> relativePaths;
  /**
   * 
   * @param parentFrame   The parent frame of the dialog.
   * @param title   The title of the dialog.
   * @param list    The relative paths of all the unpacked files.
   * @param filesOnDisk   Where to copy the unpacked files.
   * @param translatedFiles  Where to extract the archive. These files will be copied in rootDir.
   */
  public PreviewDialog(final Frame parentFrame, String title, ArrayList<String> list, final File filesOnDisk, final File translatedFiles) {
    super(parentFrame, title, false);
    this.filePaths = list;
    
    JButton cancel = getCancelButton();
    // Delete the translatedFiles directory if the user presses the Cancel button.
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        try {
          FileUtils.deleteDirectory(translatedFiles);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
    
    JButton apply = getOkButton();
    apply.setText("Apply");
    // 1. Start the processing. (the CopyDirectoryWorker)
    // 2. Show the dialog. 
    // 3. The CopyDirectoryWorker notifies the dialog.
    apply.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        ProgressDialog dialog = new ProgressDialog(parentFrame, "Applying files");
        ArrayList<ProgressChangeListener> listeners = new ArrayList<ProgressChangeListener>();
        listeners.add(dialog);
        final CopyDirectoryWorker copyDirTask = new CopyDirectoryWorker(filesOnDisk, translatedFiles, listeners);
        
        listeners.add(new ProgressChangeListener() {
          public boolean isCanceled() {
            return false;
          }
          public void done() {
            ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).showInformationMessage("The translated files have been applied.");
            try {
              FileUtils.deleteDirectory(translatedFiles);
            } catch (IOException e) {
              logger.error(e, e);
            }
          }
          
          public void change(ProgressChangeEvent progress) { }
          // Show an error message and delete the translatedFiles directory when the watched operation has failed.
          public void operationFailed(Exception ex) {
            ((StandalonePluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace()).showErrorMessage(
                "Couldn't apply files because of: " + ex.getMessage());
            
            try {
              FileUtils.deleteDirectory(translatedFiles);
            } catch (IOException e) {
              logger.error(e, e);
            }
          }
        });
        copyDirTask.execute();
      }
    });


    DefaultListModel<String> paths = new DefaultListModel<String>();
    for (String string : list) {
      paths.addElement(string);
    }

    relativePaths = new JList<String>(paths);

    relativePaths.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
   
    relativePaths.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 2) {
          String selectedPath = relativePaths.getSelectedValue();
          
          System.out.println("selected "+relativePaths.getSelectedValue());
          
          URL leftURL = null;
          try {
            leftURL = new File(filesOnDisk, selectedPath).toURI().toURL();
          } catch (MalformedURLException e2) {
            e2.printStackTrace();
          }

          URL rightURL = null;
          try {
            rightURL = new File(translatedFiles, selectedPath).toURI().toURL();
          } catch (MalformedURLException e2) {
            e2.printStackTrace();
          }
          ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).openDiffFilesApplication(leftURL, rightURL);
        }
      }
    });

    JScrollPane pathsScrollPane = new JScrollPane(relativePaths); 
    pathsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    pathsScrollPane.setPreferredSize(new Dimension(500, 200));

    JLabel label = new JLabel("Double click on a file to see the diferences.");
    JPanel panel = new JPanel(new GridBagLayout());
    
    panel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(pathsScrollPane, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));
    
    getContentPane().add(panel, BorderLayout.CENTER);

    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
    setVisible(true);
  }
}
