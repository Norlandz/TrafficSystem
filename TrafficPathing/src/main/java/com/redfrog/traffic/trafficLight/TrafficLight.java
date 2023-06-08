package com.redfrog.traffic.trafficLight;

import java.util.LinkedList;

import com.redfrog.traffic.collision.CollisionManager;
import com.redfrog.traffic.model.UnitObj;
import com.redfrog.traffic.pathing.PathingUtil;

import javafx.scene.shape.Rectangle;

public class TrafficLight extends UnitObj {

  private transient final PathingUtil pathingUtil_corr;

  public TrafficLight(PathingUtil pathingUtil_corr) {
    //__________________________________
    this.pathingUtil_corr = pathingUtil_corr;
  }

  //___________________
  private TrafficLight() {
    this.pathingUtil_corr = null;

  }

  //_________

  public Long timeLength_StopMove;
  public Long timeLength_AllowMove;

  private TrafficLightInstruction trafficLightInstruction;

  //_________

  //__________________________________
  private transient LinkedList<UnitObj> arr_unitObj_Stopped = new LinkedList<UnitObj>();

  public void allow_Move() {
    trafficLightInstruction = TrafficLightInstruction.AllowMove;

    //_____________________________________________________________________
    //____________________________________________________
    //_______________________________________________________________
    //______

    //______________________
    collisionHandler        = CollisionManager.NullCollisionHandler;

    while (!arr_unitObj_Stopped.isEmpty()) {
      pathingUtil_corr.resume_goTo_TargetLocations(arr_unitObj_Stopped.pollFirst()); //_
    }
  }

  public void stop_Move() {
    trafficLightInstruction = TrafficLightInstruction.StopMove;

    collisionHandler        = (unitObj_AA, unitObj_BB) -> {
                              arr_unitObj_Stopped.add(unitObj_BB);
                              pathingUtil_corr.pause_goTo_TargetLocations(unitObj_BB);
                            };
  }

  public TrafficLightInstruction get_trafficLightInstruction() { return trafficLightInstruction; }

  @Override
  public String toString() { return "‘" + super.toString() + " :: " + trafficLightInstruction + " :: " + timeLength_StopMove + " :: " + timeLength_AllowMove + "’"; }

}
