package com.oxygenxml.translation.support.core;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.CheckBoxTree;
import com.oxygenxml.translation.support.tree.CheckBoxTreeFileSystemModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class CheckBoxTreeFileSystemMain extends JFrame {
  private CheckBoxTree fileTree;

  private CheckBoxTreeFileSystemModel fileSystemModel;

  public CheckBoxTreeFileSystemMain(String directory) {
    super("JTree FileSystem Viewer");
    
    fileSystemModel = new CheckBoxTreeFileSystemModel(new File(directory));
    fileTree = new CheckBoxTree(fileSystemModel) {
      public String convertValueToText(Object value, boolean selected,
          boolean expanded, boolean leaf, int row, boolean hasFocus) {
        return ((File)value).getName();
      }
    };
    
    ImageIcon image = createImageIcon("images/Open16@2x.png");
    ImageIcon text = createImageIcon("images/TxtIcon16.png");
    
    
    DefaultTreeCellRenderer defaultRenderer = (DefaultTreeCellRenderer) fileTree.getActualCellRenderer();
    
    if (image != null) {
      defaultRenderer.setOpenIcon(image);
      defaultRenderer.setLeafIcon(text);
      defaultRenderer.setClosedIcon(image);
    }
    
    
    fileTree.setRowHeight(40);
    fileTree.setEditable(false);
    fileTree.getCheckBoxTreeSelectionModel().setDigIn(true);
    getContentPane().add(new JScrollPane(fileTree));
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    
    fileTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 2) {
          TreePath[] selectionPaths = fileTree.getCheckBoxTreeSelectionModel().getSelectionPaths();
          for (TreePath treePath : selectionPaths) {
            System.out.println(treePath);
          }
        }
      }
    });
    
    
    setSize(1280, 720);
    
    setLocationRelativeTo(null);
    setVisible(true);
  }
  public static void main(String args[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          LookAndFeelFactory.installJideExtension();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        File path = TestUtil.getPath(".");
        new CheckBoxTreeFileSystemMain(path.getAbsolutePath());
      }
    });
  }
  
  
  /** Returns an ImageIcon, or null if the path was invalid. */
  protected static ImageIcon createImageIcon(String path) {
      java.net.URL imgURL = PluginWorkspaceProvider.class.getClassLoader().getResource(path);
      if (imgURL != null) {
          return new ImageIcon(imgURL);
      } else {
          System.err.println("Couldn't find file: " + path);
          return null;
      }
  }
}
