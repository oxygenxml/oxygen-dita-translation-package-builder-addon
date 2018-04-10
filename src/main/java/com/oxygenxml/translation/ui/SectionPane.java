package com.oxygenxml.translation.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Section pane(JLabel + JSeparator). 
 *
 */
public class SectionPane extends JPanel{

  /**
   * Constructor.
   * @param title The title of the section pane.
   */
  public SectionPane(String title) {
    super(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    // Add the label.
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new Insets(20, 0, 5, 5);
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
    add(titleLabel, c);
    
    // Add the separator.
    c.gridx ++;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 1;
    c.insets = new Insets(20, 0, 5, 0);
    add(new JSeparator(), c);
  }
}
