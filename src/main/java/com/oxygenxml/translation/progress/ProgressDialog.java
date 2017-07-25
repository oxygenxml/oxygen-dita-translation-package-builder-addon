package com.oxygenxml.translation.progress;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;
/**
 * Creates a dialog to show the progress of a time consuming task.
 * @author Bivolan Dalina
 *
 */
public class ProgressDialog extends OKCancelDialog implements ProgressChangeListener {

  private JProgressBar progressBar;
	private JLabel label;
	/**
	 *  True if the Cancel button is clicked.
	 */
	private boolean isCancelButtonPressed = false;
	/**
	 *  True if the background task was finished.
	 */
	private boolean isDone = false;

  public boolean isDone() {
    return isDone;
  }

  /**
  * @param parentFrame  The parent frame
  * @param title The dialog title
  * @param modal  True if modal
  */
  public ProgressDialog(Frame parentFrame, String title) {
    super(parentFrame, title, true);
    getOkButton().setVisible(false);
    
    JPanel mainPanel = new JPanel(new GridBagLayout());
    progressBar = new JProgressBar();
    label = new JLabel("Here you'll see informations about the progress.");
    
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    progressBar.setSize(300, 100);
    
    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.gridx = 0;
    gbc1.gridy = 0;
    gbc1.gridwidth = 1;
    gbc1.gridheight = 1;
    gbc1.weightx = 1;
    gbc1.weighty = 1;
    gbc1.fill = GridBagConstraints.BOTH;
    gbc1.anchor = GridBagConstraints.CENTER;
    gbc1.ipadx = 10;
    gbc1.ipady = 10;
    
    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.gridx = 0;
    gbc2.gridy = 1;
    gbc2.gridwidth = 1;
    gbc2.gridheight = 1;
    gbc2.weightx = 1;
    gbc2.weighty = 1;
    gbc2.fill = GridBagConstraints.BOTH;
    gbc2.anchor = GridBagConstraints.CENTER;
    gbc2.ipadx = 10;
    gbc2.ipady = 10;
    
    mainPanel.add(label, gbc1);
    mainPanel.add(progressBar, gbc2);
    
    getContentPane().add(mainPanel, BorderLayout.CENTER);
    
    getCancelButton().addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        
        isCancelButtonPressed = true;
      }
    });
    
    pack();
    setResizable(false);
    
  }
  /**
   *  Modifies the dialog content while receiving events from the ProgressChangeListener listeners.
   */
  public void change(ProgressChangeEvent progress) {
    
    progressBar.setValue(progress.getCounter());
    label.setText(progress.getMessage());
    // If we don't now the maximum value for the progress bar we set it to indeterminate mode.
    if(progress.getTotalFiles() == -1){
      progressBar.setIndeterminate(true);
      progressBar.setStringPainted(false);
    }
    // else we set the progress bar's maximum value.
    else{
      progressBar.setMaximum(progress.getTotalFiles());
    }
  }
  /**
   *  Returns true if the Cancel button was pressed by the user.
   */
  public boolean isCanceled() {
    if(isCancelButtonPressed){
      return true;      
    }
    return false;
  }
  /**
   *  Closes the dialog and sets isDone to true.
   *  This method is called after the time consuming task it's over.
   */
  public void done() {
    setVisible(false);
    isDone = true;
  }

}
