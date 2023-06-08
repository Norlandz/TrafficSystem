package com.redfrog.traffic.session;

import org.springframework.stereotype.Component;

import com.redfrog.traffic.collision.CollisionManager;
import com.redfrog.traffic.pathing.PathingUtil;
import com.redfrog.traffic.service.TrafficItemControlService;
import com.redfrog.traffic.status.MouseStatus;
import com.redfrog.traffic.status.SelectStatus;
import com.redfrog.traffic.trafficLight.TrafficLightManager;
import com.redfrog.traffic.util.JavafxUtil;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

@Component
//___________________
public class WindowSession {

  public static int seqNum = 0;

  public final String name;

  //_____________________________________
  //_____________
  //_____________________
  //___

  public WindowSession() {
    seqNum++;
    this.name = "Init";
  }

  public Stage javafxStage;
  public Pane pane_JavafxRoot;
  public Pane panel_SemanticRoot;

  public SessionManager sessionManager;

  //_____________

  public final SelectStatus selectStatus = new SelectStatus(this);
  public final CollisionManager collisionManager = new CollisionManager(this);
  public final PathingUtil pathingUtil = new PathingUtil(this);
  public final JavafxUtil javafxUtil = new JavafxUtil(this);
  public final MouseStatus mouseStatus = new MouseStatus(this);
  //____________________________________________________________________________________________________________
  public final TrafficLightManager trafficLightManager = new TrafficLightManager(this);

  //_______________________________________________________________
  //____________________________________________________
  //____________________________________________________________
  //__________________________________________________
  //________________________________________________
  //__________________________________________________
  //______________________________________________________________________________
  //__________________________________________________________________

  //_____________

  @Override
  public String toString() {
    return "‘" + super.toString() + " :: " + this.name + "’"; //_
  }

  //_____________

}
