package com.redfrog.traffic.status;

import com.redfrog.traffic.session.WindowSession;

public class MouseStatus {

  private final WindowSession windowSession_corr;

  public MouseStatus(WindowSession windowSession_corr) {
    super();
    this.windowSession_corr = windowSession_corr;
  }

  //_________

  public StatusPhase drag;

}
