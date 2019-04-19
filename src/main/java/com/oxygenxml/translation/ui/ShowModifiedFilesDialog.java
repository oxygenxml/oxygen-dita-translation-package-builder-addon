package com.oxygenxml.translation.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import com.oxygenxml.translation.support.storage.ResourceInfo;

/**
 * Dialog with the modified files.
 * 
 * @author adrian_sorop
 */
@SuppressWarnings("serial")
public class ShowModifiedFilesDialog extends JPanel{
  
  /**
   * Instance of the dialog.
   */
   private static ShowModifiedFilesDialog instance;
   
   /**
    * Text area where the modified resources are presented as list.
    */
   JTextArea modifiedFiles;
  
  /**
   * Private constructor
   */
   private ShowModifiedFilesDialog() {
     setLayout(new GridBagLayout());
     modifiedFiles = new JTextArea(10, 40);
     modifiedFiles.setLineWrap(true);
     modifiedFiles.setWrapStyleWord(true);
     modifiedFiles.setEditable(false);

     JScrollPane scroll = new JScrollPane(modifiedFiles);
     scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

     GridBagConstraints gbc = new GridBagConstraints();
     gbc.gridx = 0;
     gbc.gridy = 1;
     gbc.gridwidth = 1;
     gbc.gridheight = 1;
     gbc.weightx = 0;
     gbc.weighty = 0;
     gbc.fill = GridBagConstraints.BOTH;
     gbc.anchor = GridBagConstraints.LINE_START;
     add(scroll , gbc);
   }
  
  /**
   * @return The singleton instace of the dialog.
   */
  public static ShowModifiedFilesDialog getInstance() {
    if (instance == null) {
      instance = new ShowModifiedFilesDialog();
    }
    return instance;
  }
  
  /**
   * Shows the dialog with the modified files.
   * 
   * @param parentFrame The parent frame for this dialog.
   * @param list The List with the modified resources.
   */
  public void showDialog(JFrame parentFrame, List<ResourceInfo> list) throws IOException {
    modifiedFiles.setText("");
    if (list != null && !list.isEmpty()) {
      modifiedFiles.append(list.get(0).getRelativePath());
      for(int i = 1; i < list.size(); i++){
        modifiedFiles.append("\n");
        modifiedFiles.append(list.get(i).getRelativePath());
      }
      modifiedFiles.setCaretPosition(0);

      JOptionPane.showMessageDialog(parentFrame, instance, 
          "Resources to pack", JOptionPane.INFORMATION_MESSAGE);
    } else {
      throw new IOException("The list containing the modified files is empty or null.");
    }
  }
}
