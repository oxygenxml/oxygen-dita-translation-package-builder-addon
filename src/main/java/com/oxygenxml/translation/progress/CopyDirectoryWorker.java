package com.oxygenxml.translation.progress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import com.oxygenxml.translation.support.util.ArchiveBuilder;

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
    
    int counter = 0;
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
      archiveBuilder.copyDirectory(temDir, rootDir);

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
