package com.oxygenxml.translation.support.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for Undo and Redo actions.
 * 
 * @author adrian_sorop
 */
public class UndoRedoUtils {
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = LoggerFactory.getLogger(UndoRedoUtils.class.getName());
  
  /**
   * Private constructor to avoid instantiation.
   */
  private UndoRedoUtils() {
    // Nothing
  }
  
  /**
   * Sets Undo/Redo actions over a {@link JTextComponent}} and binds them to 
   * the "control z"/"control y" key stroke.
   * 
   * @param component The component on which to set the undo action. 
   * 
   * @param undo Undo manager.
   */
  public static void installUndoRedoSupport(final JTextComponent component) {
    
    final UndoManager undo = new UndoManager();
    Document textFieldDocument = component.getDocument();
    
    // Listen for undo and redo events
    textFieldDocument.addUndoableEditListener(evt -> undo.addEdit(evt.getEdit()));
    
    component.getActionMap().put("Undo", new AbstractAction("Undo") {
      public void actionPerformed(ActionEvent evt) {
        try {
          if (undo.canUndo()) {
            undo.undo();
          }
        } catch (CannotUndoException e) {
          logger.error(String.valueOf(e), e);
        }
      }
    });
    // Bind the undo action to ctl-Z
    component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
    
    // Create a redo action and add it to the text component
    component.getActionMap().put("Redo", new AbstractAction("Redo") {
      public void actionPerformed(ActionEvent evt) {
        try {
          if (undo.canRedo()) {
            undo.redo();
          }
        } catch (CannotRedoException e) {
          logger.error(String.valueOf(e), e);
        }
      }
    });
    
    // Bind the redo action to ctl-Y
    component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
  }
}
