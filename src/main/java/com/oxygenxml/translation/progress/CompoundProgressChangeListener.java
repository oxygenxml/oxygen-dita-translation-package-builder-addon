package com.oxygenxml.translation.progress;

public class CompoundProgressChangeListener implements ProgressChangeListener {
  
  private ProgressChangeListener a;
  private ProgressChangeListener b;

  public CompoundProgressChangeListener(ProgressChangeListener a, ProgressChangeListener b) {
    this.a = a;
    this.b = b;
    
  }

  public void change(ProgressChangeEvent progress) {
    if (a != null) {
      a.change(progress);
    }
    
    if (b != null) {
      b.change(progress);
    }
  }

  public boolean isCanceled() {
    boolean c = false;
    if (a != null) {
      c = a.isCanceled();
    }
    
    
    if (!c && b != null) {
      c = b.isCanceled();
    }
    
    return c;
  }

  public void done() {
    if (a != null) {
      a.done();
    }
    
    if (b != null) {
      b.done();
    }
  }

  public void operationFailed(Exception ex) {
    if (a != null) {
      a.operationFailed(ex);
    }
    
    if (b != null) {
      b.operationFailed(ex);
    }
  }

}
