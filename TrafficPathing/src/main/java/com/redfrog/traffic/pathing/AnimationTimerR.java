package com.redfrog.traffic.pathing;

import java.util.LinkedList;

import javafx.animation.AnimationTimer;

public abstract class AnimationTimerR extends AnimationTimer {
  public volatile boolean det_Terminated = false; //______________________________________________________________________________________________________________
  public volatile boolean det_Stopped = false;

  //_____________________
  public volatile Runnable callback_AfterTerminate;

  //___________________________________________________________
  //_____________
  //________________________________________________________

  public final LinkedList<Runnable> arr_callback_RevertToLastMove = new LinkedList<>();

  protected void run_BeforeSuperStart_sync() {}

  protected void run_AfterSuperTerminate() {}

  @Override
  public void start() {
    if (!det_Terminated) { //_______
      det_Stopped = false;
      run_BeforeSuperStart_sync();
      super.start();
    }
    else {
      throw new Error("Already Terminated");
    }
  }

  /**
______________________
_____________________________
__*/
  public void terminate() {
    stop();
    det_Terminated = true;

    if (callback_AfterTerminate != null) {
      callback_AfterTerminate.run();

    }
    run_AfterSuperTerminate(); //______________________________________________________________________________
  }

  @Override
  public void stop() {
    super.stop();
    det_Stopped = true; //_________________________
  }

}
