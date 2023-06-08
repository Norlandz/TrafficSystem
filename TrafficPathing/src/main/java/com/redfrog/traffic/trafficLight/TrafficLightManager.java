package com.redfrog.traffic.trafficLight;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.redfrog.traffic.exception.TypeError;
import com.redfrog.traffic.session.WindowSession;

import javafx.application.Platform;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class TrafficLightManager {

  private WindowSession windowSession_corr;

  public TrafficLightManager(WindowSession windowSession_corr) {
    super();
    this.windowSession_corr = windowSession_corr;
  }

  //_____________

  //__________________________________________________________________

  //______________________________________________________________
  private ScheduledExecutorService executor_TrafficLight = Executors.newScheduledThreadPool(10, new ThreadFactoryBuilder().setNameFormat("thd-TrafficLight-%d").build());

  //_____________

  private Paint color_StopMove = Color.rgb(200, 80, 80, 0.5);
  private Paint color_AllowMove = Color.rgb(80, 200, 80, 0.5);

  private TrafficLight create_TrafficLight(double width, double height, double posX, double posY) {
    TrafficLight trafficLight = new TrafficLight(windowSession_corr.pathingUtil);
    windowSession_corr.panel_SemanticRoot.getChildren().add(trafficLight.node_underlying);
    trafficLight.node_underlying.setBackground(Background.EMPTY);
    trafficLight.node_underlying.setLayoutX(posX);
    trafficLight.node_underlying.setLayoutY(posY);
    trafficLight.node_underlying.setViewOrder(-1);

    final Rectangle rectangle = new Rectangle();
    trafficLight.node_underlying.getChildren().add(rectangle);
    rectangle.setWidth(width);
    rectangle.setHeight(height);
    rectangle.setFill(color_StopMove);
    trafficLight.shape            = rectangle;

    trafficLight.collisionHandler = (unitObj_AA, unitObj_BB) -> {
      System.out.println("Testing collide, not a true handler, assign true handler later");
    };
    windowSession_corr.collisionManager.register(trafficLight);

    return trafficLight;
  }

  public TrafficLight create_TrafficLight_withRate(double width, double height, double posX_Begin, double posY_Begin,
                                                   long timeLength_AllowMove, long timeLength_StopMove) {

    if (timeLength_AllowMove < 0 || timeLength_StopMove < 0) { throw new Error(); }

    TrafficLight trafficLight = create_TrafficLight(width, height, posX_Begin, posY_Begin);

    //_________________________________________________
    //_________________________________________________________________________________________
    //________________________________________________________________________________________________________________________________
    //____________________________________________
    //___________________________________________________________________________________

    //_________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

    trafficLight.timeLength_StopMove  = timeLength_StopMove;
    trafficLight.timeLength_AllowMove = timeLength_AllowMove;

    executor_TrafficLight.schedule(new Runnable()
      {
        @Override
        public void run() {
          final Runnable _this = this; //__________________________________________________________________________________________________

          Platform.runLater(() -> {
            Long delay_curr = null;

            if (trafficLight.get_trafficLightInstruction() == null) {
              trafficLight.stop_Move();
              trafficLight.shape.setFill(color_StopMove);
              delay_curr = trafficLight.timeLength_StopMove;
            }
            else {
              if (trafficLight.get_trafficLightInstruction() == TrafficLightInstruction.StopMove) {
                trafficLight.allow_Move();
                trafficLight.shape.setFill(color_AllowMove);
                delay_curr = trafficLight.timeLength_AllowMove;
              }
              else if (trafficLight.get_trafficLightInstruction() == TrafficLightInstruction.AllowMove) {
                trafficLight.stop_Move();
                trafficLight.shape.setFill(color_StopMove);
                delay_curr = trafficLight.timeLength_StopMove;
              }
              else {
                throw new TypeError();
              }
            }

            if (!executor_TrafficLight.isShutdown()) { //______
              executor_TrafficLight.schedule(_this, delay_curr, TimeUnit.MILLISECONDS);
            }

          });
        }

      }, 0, TimeUnit.MILLISECONDS);

    return trafficLight;

  }

  //_____________

  public void shutDown() {
    executor_TrafficLight.shutdown(); //___________________________________________________________________
    try {
      boolean det_Terminated = executor_TrafficLight.awaitTermination(2000, TimeUnit.MILLISECONDS);
      if (det_Terminated == false) {
        throw new Error("Not_Terminated :: " + executor_TrafficLight); //
      }
    } catch (InterruptedException e) {
      throw new Error(e);
    }
  }

}
