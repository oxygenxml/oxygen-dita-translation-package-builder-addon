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
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

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
   * @param filePaths    The relative paths of all the unpacked files.
   * @param filesOnDisk   Where to copy the unpacked files.
   * @param translatedFiles  Where to extract the archive. These files will be copied in rootDir.
   */
  public PreviewDialog(final Frame parentFrame, String title, ArrayList<String> filePaths, final File filesOnDisk, final File translatedFiles) {
    super(parentFrame, title, false);
    this.filePaths = filePaths;
    
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
    for (String string : filePaths) {
      paths.addElement(string);
    }

    relativePaths = new JList<String>(paths);

    relativePaths.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
   
    relativePaths.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 2) {
          String selectedPath = relativePaths.getSelectedValue();
          
          if (logger.isDebugEnabled()) {
            logger.debug(relativePaths.getSelectedValue());
          }
         
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

    final JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(relativePaths);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setPreferredSize(new Dimension(500, 200));

    JLabel label = new JLabel("Double click on an Oxygen supported FILE to see the diferences.");
    
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(filesOnDisk.getName());
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    final JTree tree = new JTree(treeModel);
    
    for (String data : filePaths) {
      buildTreeFromString(treeModel, data);
    }
    
    tree.setEditable(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setShowsRootHandles(true);
    
    final JPanel panel = new JPanel(new GridBagLayout());
    
    final JButton treeButton = new JButton("Switch to Tree View");
    
    treeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(treeButton.getText().equals("Switch to List View")){
          scrollPane.setViewportView(relativePaths);
          treeButton.setText("Switch to Tree View");
        }else{
          treeButton.setText("Switch to List View");
          scrollPane.setViewportView(tree);
        }
        
      }
    });
    
    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 2) {
          Object[] selectedPath = tree.getSelectionPath().getPath();
          int length = selectedPath.length;
          String relativePath = "";
          for (int i = 1; i < length-1; i++) {
            relativePath = relativePath + selectedPath[i].toString();
          }
          relativePath = relativePath + "/" + selectedPath[length-1];
          
          if (logger.isDebugEnabled()) {
            logger.debug(tree.getSelectionPath());
          }
         
          URL leftURL = null;
          try {
            leftURL = new File(filesOnDisk, relativePath).toURI().toURL();
          } catch (MalformedURLException e2) {
            e2.printStackTrace();
          }

          URL rightURL = null;
          try {
            rightURL = new File(translatedFiles, relativePath).toURI().toURL();
          } catch (MalformedURLException e2) {
            e2.printStackTrace();
          }
          ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).openDiffFilesApplication(leftURL, rightURL);
        }
      }
    });
    
    panel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(scrollPane, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));
    panel.add(treeButton, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
   
    
    getContentPane().add(panel, BorderLayout.CENTER);

    setLocationRelativeTo(parentFrame);
    pack();
    setResizable(true);
    setVisible(true);
  }
  /**
   * Builds a tree from a given forward slash delimited string.
   * 
   * @param model The tree model.
   * @param data The string to build the tree from.
   */
  private void buildTreeFromString(final DefaultTreeModel model, final String data) {
    // Fetch the root node
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

    // Split the string around the delimiter
    String [] strings = data.split("/");

    // Create a node object to use for traversing down the tree as it 
    // is being created
    DefaultMutableTreeNode node = root;

    // Iterate of the string array
    for (String s: strings) {
      // Look for the index of a node at the current level that
      // has a value equal to the current string
      int index = childIndex(node, s);

      // Index less than 0, this is a new node not currently present on the tree
      if (index < 0) {
        // Add the new node
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(s);
        node.insert(newChild, node.getChildCount());
        node = newChild;
      }
      // Else, existing node, skip to the next string
      else {
        node = (DefaultMutableTreeNode) node.getChildAt(index);
      }
    }
  }
  
  /**
   * Returns the index of a child of a given node, provided its string value.
   * 
   * @param node The node to search its children.
   * @param childValue The value of the child to compare with.
   * @return The index.
   */
  private int childIndex(final DefaultMutableTreeNode node, final String childValue) {
    @SuppressWarnings("unchecked")
    Enumeration<DefaultMutableTreeNode> children = node.children();
    DefaultMutableTreeNode child = null;
    int index = -1;

    while (children.hasMoreElements() && index < 0) {
      child = children.nextElement();

      if (child.getUserObject() != null && childValue.equals(child.getUserObject())) {
        index = node.getIndex(child);
      }
    }

    return index;
  }


}
