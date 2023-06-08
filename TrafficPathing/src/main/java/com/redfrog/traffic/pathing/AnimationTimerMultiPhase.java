package com.redfrog.traffic.pathing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWarDeployment;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;

import com.redfrog.traffic.pathing.PathingUtil.TargetPositionRegister;
import com.redfrog.traffic.shape.Point;

import javafx.scene.Node;

public class AnimationTimerMultiPhase extends AnimationTimerR {

  //________________________________________________________
  //_______________________________________________________________________________________________________

  //________________________________________
  //________________________________________________
  //__________________________________________________

  private final Node node_Move;
  private final LinkedList<Point> queue_point;   //__________________________________________
  private final double diagonalInc;
  private final double speed;

  public AnimationTimerMultiPhase(Node node_Move, LinkedList<Point> queue_point, double diagonalInc, double speed) {
    super();
    this.node_Move   = node_Move;
    this.queue_point = queue_point;
    this.diagonalInc = diagonalInc;
    this.speed       = speed;
  }

  //_________

  //________________________________________________________________________________________________________________________________________________________________________________________________________________________________
  //____________________________________________

  public TargetPositionRegister targetPositionRegister;

  //_________

  @Override
  protected void run_BeforeSuperStart_sync() {
    super.run_BeforeSuperStart_sync();
    run_NextSegment(true);
  }

  private double posX_End_currPoint;
  private double posY_End_currPoint;

  private double posXInc_currPoint;
  private double posYInc_currPoint;

  //___________________________________________________________

  private void run_NextSegment(boolean firstRun) {
    //__________________________________
    //_____________________________________
    if (!firstRun) {
      Point point_Reach = queue_point.removeFirst();
      //____________________________________________________________________________
      //______________________________________________________________________________________________________________________________________
      //________________________________________________________________________________________________________________________________________________________
      //________________________________________________________________
      //___________________________________________________________________________
      //_______
      //_____
      if (targetPositionRegister != null) {
        targetPositionRegister.handle_WhenPointIsReached(point_Reach); //_
      }
    }

    if (!queue_point.isEmpty()) {     //________________________________
      Point point_curr = queue_point.getFirst();
      posX_End_currPoint = point_curr.getX();
      posY_End_currPoint = point_curr.getY();

      final Pair<Double, Double> pair = PathingUtil.get_DiagonalDisplacement(node_Move, posX_End_currPoint, posY_End_currPoint, diagonalInc);
      posXInc_currPoint         = pair.getLeft();
      posYInc_currPoint         = pair.getRight();

      time_LastExec             = -1;

      //___________________________________
      //___________________________
      //_________________________________________________________
      det_PosXReached_currPoint = false;
      det_PosYReached_currPoint = false;

    }
    else {
      this.terminate(); //____________________________________________________
    }
  }

  @Override
  public void handle(long now) {
    if (det_PosXReached_currPoint && det_PosYReached_currPoint) { //______________________________________________________________________________________
      //_____________________________________________________________________________________________
      run_NextSegment(false); //_
    }

    if (!det_Terminated) {
      move_Linearly_WithSpeed_LoopToFillUpGapInFrame(now); //_
    }
    else {
      //______________________________________________________________________
      //____________________________________________
    }
  }

  //_______
  @Override
  protected void run_AfterSuperTerminate() {
    super.run_AfterSuperTerminate();
    //_______________________________________________________________________________________________________________________________
    //_________________________________________________
    //_____
  }

  //_____

  private long time_LastExec = -1;

  private void move_Linearly_WithSpeed_LoopToFillUpGapInFrame(long now) {
    if (time_LastExec == -1) {
      move_Linearly();
    }
    else {
      double timeGap = (now - time_LastExec) / 1E6; //_______________________________
      double distanceGap = timeGap * speed;
      double amountGap_Loop = distanceGap / diagonalInc;

      //__________________________________________________________________________________________________________________________________________________________________________________________________________
      int amountGap_Loop_roundDown = (int) amountGap_Loop;

      //__________________________________
      if (amountGap_Loop_roundDown == 0) {
        move_Linearly();
      }
      //_____________________________________________________________________________________________
      //_____________________________________________________________________________
      else {
        for (int i = 1; i <= amountGap_Loop_roundDown; i++) {
          //______________________________________________________________________________________________________________________________
          //___________________________________________________________
          //________________________________________________________________________________
          //________________________________________________________
          //_____________________________________________
          //__________________________________________________
          //____________________________________________________________________
          if (det_Stopped) {
            System.out.println("// said Frame & Speed & Loop messy; should stopped. Recover is to do... but seems just works fine idk..."); 
            //__________________________________________________________________________
          }
          if (!det_Terminated) { //________
            move_Linearly();
          }
          else {
            //___________________
            break;
          }
        }
      }
    }

    time_LastExec = now;
  }

  //_____

  private boolean det_PosXReached_currPoint = false;
  private boolean det_PosYReached_currPoint = false;

  private void move_Linearly() {
    double posX_curr = node_Move.getLayoutX();
    double posY_curr = node_Move.getLayoutY();
    double posX_next = posX_curr + posXInc_currPoint;
    double posY_next = posY_curr + posYInc_currPoint;

    //______________________________________________________________________________
    //_________________________
    if ((posXInc_currPoint > 0 && posX_next - posX_End_currPoint > 0)
        || (posXInc_currPoint < 0 && posX_next - posX_End_currPoint < 0)) {
      posX_next = posX_End_currPoint; //__
    }
    if ((posYInc_currPoint > 0 && posY_next - posY_End_currPoint > 0)
        || (posYInc_currPoint < 0 && posY_next - posY_End_currPoint < 0)) {
      posY_next = posY_End_currPoint; //_
    }
    if (!det_PosXReached_currPoint) {
      if (!det_Terminated) {
//__________________________________________________________________________
        //______________________________________________
        //________________________________________________________
        //__________________
        //_____________________________________________________________________________________________
        arr_callback_RevertToLastMove.add(() -> node_Move.setLayoutX(posX_curr));
        node_Move.setLayoutX(posX_next);
        if (posX_next == posX_End_currPoint) { det_PosXReached_currPoint = true; }
      }
      else {
        //___________________________________________________________
        //______________________________________________
        //___________________________________________________
        return;
      }
    }
    if (!det_PosYReached_currPoint) {
      if (!det_Terminated) { //________
        arr_callback_RevertToLastMove.add(() -> node_Move.setLayoutY(posY_curr));
        node_Move.setLayoutY(posY_next);
        //__________________________________
        //_______________________________________________
        if (posY_next == posY_End_currPoint) { det_PosYReached_currPoint = true; }
      }
      else {
        //__________________________________________________________________________
        return;
      }
    }

    //____________________________________________
    //___________________________________________________________
    //_______________________________________
    //____________________________________________________________________________________________________________________________________
    if (det_PosXReached_currPoint && det_PosYReached_currPoint) {
      //_____________________________________________________________________________________________________________________________________
    }

    //_________________________________________________________________
    //________________________________________________________________________________________________________
  }

  //_____________

  public void add_NewTargetPoint(Point point_End_currTarget) { queue_point.add(point_End_currTarget); }

}
