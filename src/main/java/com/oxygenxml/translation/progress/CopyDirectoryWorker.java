package com.oxygenxml.translation.progress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import com.oxygenxml.translation.support.util.ArchiveBuilder;
/**
 *  Creates a SwingWorker for copying the  files from a source directory to a destination directory.
 *  
 * @author Bivolan Dalina
 *
 */
public class CopyDirectoryWorker extends SwingWorker<Void, Void> {
    /**
     *  The location of the destination directory.
     */
    private File rootDir;
    /**
     *  The location of the source directory.
     */
    private File temDir;
    /**
     *  A listener for notifying the changes.
     */
    private ArrayList<ProgressChangeListener> listeners;
        
    public CopyDirectoryWorker(File rootDir, File tempDir, ArrayList<ProgressChangeListener> listeners) {
      this.rootDir = rootDir;
      this.temDir = tempDir;
      this.listeners = listeners;
    }

    /**
     * Main task. Executed in background thread.
     */
    @Override
    public Void doInBackground() throws IOException, StoppedByUserException {
      ArchiveBuilder archiveBuilder = new ArchiveBuilder();
      for (ProgressChangeListener l : listeners) {
        archiveBuilder.addListener(l);
      }
      archiveBuilder.copyDirectory(temDir, rootDir, new int[] {0});

      return null;
    }

    /**
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
      for(ProgressChangeListener listener : listeners){
        listener.done();
      }
     
    }
  }
