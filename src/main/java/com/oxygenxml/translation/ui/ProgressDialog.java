package com.oxygenxml.translation.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.oxygenxml.translation.ui.worker.AbstractWorker;

import ro.sync.exml.workspace.api.PluginResourceBundle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;
/**
 * Creates a dialog to show the progress of a time consuming task.
 * 
 * @author Bivolan Dalina
 *
 */
public class ProgressDialog extends OKCancelDialog implements ProgressChangeListener {
  /**
   *  Resource bundle.
   */
  private final static PluginResourceBundle resourceBundle = ((StandalonePluginWorkspace)PluginWorkspaceProvider.getPluginWorkspace()).getResourceBundle();
  /**
   *  A swing progress bar.
   */
  private JProgressBar progressBar;
  /**
   *  A swing label.
   */
  private JLabel label;
	/**
	 *  True if the Cancel button is clicked.
	 */
	private boolean isCancelButtonPressed = false;
	/**
	 *  True if the background task was finished.
	 */
	private boolean isTaskDone = false;
	/**
	 *  True if the dialog is displayed to the user.
	 */
  protected boolean isDialogVisible = false;
	
  private boolean isTaskDone() {
    return isTaskDone;
  }
  
  /**
   * Binds a progress dialog to the given worker.
   * 
   * @param worker Thread worker.
  * @param parentFrame  The parent frame.
  * @param title The dialog title.
   */
  public static void install(AbstractWorker worker, Frame parentFrame, String title) {
    ProgressDialog progressDialog = new ProgressDialog(parentFrame, title);
    worker.addProgressListener(progressDialog);
  }

  /**
  * @param parentFrame  The parent frame.
  * @param title The dialog title.
  * @param modal  True if you want a modal dialog.
  */
  private ProgressDialog(Frame parentFrame, String title) {
    //----------------------- modal
    super(parentFrame, title, true);
    
    setLocationRelativeTo(parentFrame);
    
    getOkButton().setVisible(false);
    
    JPanel mainPanel = new JPanel(new GridBagLayout());
    progressBar = new JProgressBar();
    label = new JLabel(resourceBundle.getMessage(Tags.PROGRESS_DIALOG_LABEL));
    
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
    
    pack();
    setResizable(true);

    scheduleStart();
  }
  /**
   *  A 2 seconds timer. After the 2 seconds the dialog is visible to the user.
   */
  private void scheduleStart() {
    Timer timer = new Timer(2000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(!isTaskDone()){
          setVisible(true);
          isDialogVisible = true;
        }
      }
    });
    timer.start();
    timer.setRepeats(false); 
  }
  

  /**
   *  Modifies the dialog content while receiving events from the ProgressChangeListener listeners.
   */
  public void change(ProgressChangeEvent progress) {
    //If the dialog is not visible don't update.
    if(isShowing()){
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
  }
  /**
   *  Returns true if the Cancel button was pressed by the user.
   */
  public boolean isCanceled() {
    boolean result = false;
    if(isCancelButtonPressed){
      result = true; 
      isCancelButtonPressed = false;
    }
    return result;
  }
  /**
   *  Closes the dialog and sets isTaskDone to true.
   *  This method is called after the time consuming task it's over.
   */
  public void done() {
    setVisible(false);
    isTaskDone = true;
  }
  /**
   *  The watched operation has failed.
   */
  public void operationFailed(Exception ex) {
    // Just close the dialog.
    done();
  }
  
  @Override
  protected void doCancel() {
    isCancelButtonPressed = true;
    
    super.doCancel();
  }

}
