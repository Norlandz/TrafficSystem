package com.redfrog.traffic.pathing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.redfrog.traffic.annotation.Config;
import com.redfrog.traffic.annotation.Debug;
import com.redfrog.traffic.annotation.Main;
import com.redfrog.traffic.annotation.Messy;
import com.redfrog.traffic.annotation.Todo;
import com.redfrog.traffic.collision.CollisionHandler;
import com.redfrog.traffic.collision.CollisionManager;
import com.redfrog.traffic.exception.TypeError;
import com.redfrog.traffic.model.UnitObj;
import com.redfrog.traffic.session.WindowSession;
import com.redfrog.traffic.shape.Point;
import com.redfrog.traffic.trafficLight.TrafficLight;
import com.redfrog.traffic.util.JavafxUtil;
import com.redfrog.traffic.util.MathUtil;
import com.redfrog.traffic.util.Tup4;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

public class PathingUtil {

  private final WindowSession windowSession_corr;

  public PathingUtil(WindowSession windowSession_corr) {
    this.windowSession_corr = windowSession_corr;

  }

  //_____________
  public static Pair<Double, Double> get_DiagonalDisplacement(final Node node_Move, final double posX_End, final double posY_End, final double diagonalInc) {
    final double posX_Begin = node_Move.getLayoutX();
    final double posY_Begin = node_Move.getLayoutY();

    //________________________________________________________
    //________________

    double lengthX = Math.abs(posX_Begin - posX_End);
    double lengthY = Math.abs(posY_Begin - posY_End);

    if (lengthX == 0 && lengthY != 0) {
      if (posY_Begin < posY_End) {
        return new ImmutablePair<>(0.0, diagonalInc);
      }
      else {
        return new ImmutablePair<>(0.0, -diagonalInc);
      }
    }
    else if (lengthX != 0 && lengthY == 0) { //_________________________________________________________________________________________________________________________________________
      if (posX_Begin < posX_End) {
        return new ImmutablePair<>(diagonalInc, 0.0);
      }
      else {
        return new ImmutablePair<>(-diagonalInc, 0.0);
      }
    }
    else if (lengthX == 0 && lengthY == 0) {
      return new ImmutablePair<>(0.0, 0.0);
    }
    else {
      double angle = Math.atan(lengthX / lengthY);
      double posXInc_abs = Math.sin(angle) * diagonalInc;
      double posYInc_abs = Math.cos(angle) * diagonalInc;

      if (Double.isNaN(posXInc_abs)) { throw new Error("posXInc_abs is NaN"); }
      if (Double.isNaN(posYInc_abs)) { throw new Error("posYInc_abs is NaN"); }

      final double posXInc;
      if (posX_Begin < posX_End) {
        posXInc = posXInc_abs;
      }
      else {
        posXInc = -posXInc_abs;
      }
      final double posYInc;
      if (posY_Begin < posY_End) {
        posYInc = posYInc_abs;
      }
      else {
        posYInc = -posYInc_abs;
      }

      return new ImmutablePair<>(posXInc, posYInc);
    }
  }

  //_____________

  //____

  public static enum GotoMode {
    Immediate_AbandonAllPrevGotoTargets,
    Last_AfterAllPrevGotoTargets,
    //___________________________________________
  }

  public class TargetPositionRegister {
    public HashMap<Point, Node> mpp__point_vs_node = new HashMap<Point, Node>();

    public void handle_WhenPointIsReached(Point point_Reach) {
      Node text_Waypoint = mpp__point_vs_node.get(point_Reach);
      if (text_Waypoint != null) {
        removeFrom_PathingDebugShape(text_Waypoint); //_
      }
    }

    private boolean det_AbandontIsRun = false;

    public void handle_WhenPointIsAbandont() {
      if (det_AbandontIsRun == true) { throw new Error("Cleaned already. Why run twice?..."); }
      det_AbandontIsRun = true;
      for (Node node : mpp__point_vs_node.values()) { removeFrom_PathingDebugShape(node); }
    }
  }

  //_____
  public void goTo_TargetLocation(final UnitObj unitObj_Selected_curr, final double posX_End, final double posY_End, final double speed) { goTo_TargetLocation(unitObj_Selected_curr, posX_End, posY_End, speed, GotoMode.Immediate_AbandonAllPrevGotoTargets); }

  @Main
  public void goTo_TargetLocation(final UnitObj unitObj_Selected_curr, final double posX_End, final double posY_End, final double speed, GotoMode gotoMode) {
    //
    final Point point_End_currTarget = new Point(posX_End, posY_End);
    final LinkedList<Point> queue_point = new LinkedList<>();
    queue_point.add(point_End_currTarget);

    //
    Text text_Waypoint = new Text();
    text_Waypoint.setLayoutX(posX_End);
    text_Waypoint.setLayoutY(posY_End);
    text_Waypoint.setText("X");
    text_Waypoint.setStrokeWidth(2.0);
    addTo_PathingDebugShape(text_Waypoint);

    //
    long sn_CollisionRecursive = -1;
    if (gotoMode == GotoMode.Immediate_AbandonAllPrevGotoTargets) {
      AnimationTimerMultiPhase animationTimer_existing = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
      if (animationTimer_existing == null || animationTimer_existing.det_Terminated == true) {
        //______________
        //__________________________________
      }
      else {
        animationTimer_existing.targetPositionRegister.handle_WhenPointIsAbandont();
      }
      //_______
      text_Waypoint.setStroke(JavafxUtil.color_Purple);
      TargetPositionRegister targetPositionRegister = new TargetPositionRegister();
      targetPositionRegister.mpp__point_vs_node.put(point_End_currTarget, text_Waypoint);
      goTo_TargetLocations_recursiveInit(unitObj_Selected_curr, queue_point, speed, sn_CollisionRecursive, false, targetPositionRegister, 0);
    }
    else if (gotoMode == GotoMode.Last_AfterAllPrevGotoTargets) {
      //______________________________________________________
      //___________________________________________________________________________________________________
      //______________________

      //___________________________________________________________________________________________
      //________________________________________________________
      //______________________
      //_____________________________

      //________________________________
      //______________________________________________________________________________________________________________________________________________________________________________________
      //___________
      //___________________________________________________
      //___________________________________________________________________________________________________

      //____________________________________________________________________________________
      //_____________________________________________________________________________________________________

      //___________________________________________________________________________________________________

      //__________________________________________________________________________________________

      //_______________________________________________________________________________

      //__________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
      //______________________________________________________________________________________________________________
      //_____________________________________________________
      //_________________
      //________________________________________________________________
      //______________________________
      //________________________________________________________________
      //________________________________________________________________________________________________
      //___________________________________________________

      AnimationTimerMultiPhase animationTimer_existing = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
      if (animationTimer_existing == null || animationTimer_existing.det_Terminated == true) {
        //_____________
        text_Waypoint.setStroke(Color.MAGENTA.deriveColor(0, 1, 1, 0.5));
        TargetPositionRegister targetPositionRegister = new TargetPositionRegister();
        targetPositionRegister.mpp__point_vs_node.put(point_End_currTarget, text_Waypoint);
        goTo_TargetLocations_recursiveInit(unitObj_Selected_curr, queue_point, speed, sn_CollisionRecursive, false, targetPositionRegister, 0);
      }
      else {
        text_Waypoint.setStroke(JavafxUtil.color_Blue);
        animationTimer_existing.targetPositionRegister.mpp__point_vs_node.put(point_End_currTarget, text_Waypoint);
        animationTimer_existing.add_NewTargetPoint(point_End_currTarget);
      }
    }
    //___________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
    else {
      throw new TypeError();
    }

  }

  //_____

  public void goTo_TargetLocations_NoRepathCollideHandler_debug(final UnitObj unitObj_Selected_curr, final double posX_End, final double posY_End, final double speed) {
    final Point point_End_currTarget = new Point(posX_End, posY_End);
    final LinkedList<Point> queue_point = new LinkedList<>();
    queue_point.add(point_End_currTarget);

    AnimationTimerMultiPhase animationTimer_prev = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
    if (!(animationTimer_prev == null || animationTimer_prev.det_Terminated == true)) { animationTimer_prev.terminate(); }

    Node node_Selected_curr = unitObj_Selected_curr.node_underlying;
    final AnimationTimerMultiPhase animationTimer = moveTo_TargetPoints_IncrementallyInAnimation(node_Selected_curr, queue_point, speed, null);
    mpp__unitObj_Move__vs__animationTimer.put(unitObj_Selected_curr, animationTimer);
  }

  //_____

  public static enum GotoStatus {
    Cancelled,
    Paused,
    Resumed,
  }

  public void cancel_goTo_TargetLocations(UnitObj unitObj_Selected_curr) {
    AnimationTimerMultiPhase animationTimer_existing = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
    if (!(animationTimer_existing == null || animationTimer_existing.det_Terminated == true)) {
      animationTimer_existing.targetPositionRegister.handle_WhenPointIsAbandont();
      animationTimer_existing.terminate();
    }
  }

  //________________________________________________________
  //_____________________________________________
  public void pause_goTo_TargetLocations(UnitObj unitObj_Selected_curr) {
    AnimationTimerMultiPhase animationTimer_existing = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
    if (!(animationTimer_existing == null || animationTimer_existing.det_Terminated == true)) {
      if (animationTimer_existing.det_Stopped != true) {
        animationTimer_existing.stop(); //_
      }
    }
  }

  public void resume_goTo_TargetLocations(UnitObj unitObj_Selected_curr) {
    AnimationTimerMultiPhase animationTimer_existing = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
    if (!(animationTimer_existing == null || animationTimer_existing.det_Terminated == true)) {
      if (animationTimer_existing.det_Stopped == true) {
        animationTimer_existing.start(); //_
      }
    }
  }

  //_____

  @Config
  public final static int limit_RetryGotoTarget = 4; //___________________________________________________________________________________________________________

  @Config
  public final static long delay_Retry = 500;

  @Config
  @Deprecated
  private static final boolean pause_WhenCollidedOnSameUnitObjMultiTimes = false;

  @Config
  private static final boolean lastRetry_WhenFinalTarget_IsOccupiedBy_AnotherCollider = true;

  @Config
  private static final double distance_ConsiderToBeCloseEnoughToFinalTarget = 55;

  //_____

  private HashMap<UnitObj, AnimationTimerMultiPhase> mpp__unitObj_Move__vs__animationTimer = new HashMap<>();

  private void goTo_TargetLocations_recursiveInit(final UnitObj unitObj_Selected_curr, final LinkedList<Point> queue_point, final double speed, long sn_CollisionRecursive, boolean det_NoRecursion, final TargetPositionRegister targetPositionRegister, final int sn_RetryGotoTarget) {
    goTo_TargetLocations_recursive(unitObj_Selected_curr, queue_point, speed, sn_CollisionRecursive, det_NoRecursion, targetPositionRegister, sn_RetryGotoTarget, new LinkedList<>(), null, false); //__
  }

  @Main
  private void goTo_TargetLocations_recursive(final UnitObj unitObj_Selected_curr, final LinkedList<Point> queue_point, final double speed, long sn_CollisionRecursive, boolean det_NoRecursion, final TargetPositionRegister targetPositionRegister, final int sn_RetryGotoTarget,
                                              @Deprecated LinkedList<UnitObj> arr_unitObj_CollidedOn,
                                              @Deprecated Long time_LastCollide,
                                              boolean det_FinalTouch_TryGoNearOccupiedLocation) {
    if (sn_RetryGotoTarget != 0) {
      System.err.println("Retry No :: " + sn_RetryGotoTarget); //_
    }
    if (sn_RetryGotoTarget > limit_RetryGotoTarget) {
      System.err.println("Retry limit reached :: " + limit_RetryGotoTarget);
      System.err.println("queue_point :: " + queue_point);

      if (!lastRetry_WhenFinalTarget_IsOccupiedBy_AnotherCollider) {
        return;
      }
      else {
        //________________________________________________________________________________________________
        //______________________________________
        //_____________________________________________________
        //_______
        if (!det_FinalTouch_TryGoNearOccupiedLocation) {
          if (queue_point.size() == 1) { //________________________________________________
            Point point_Target = queue_point.getFirst();

            //__________________________________________________________________________________________________

            Point point_CurrPosition = new Point(unitObj_Selected_curr.node_underlying.getLayoutX(), unitObj_Selected_curr.node_underlying.getLayoutY());

            if (point_Target.distance(point_CurrPosition) < distance_ConsiderToBeCloseEnoughToFinalTarget) {
              //__________________________________
            }
            else {
              Shape boundsShapeUnion = windowSession_corr.pathingUtil.get_BoundsShapeUnion_OfAllUnitObj_AdjLv2Scm_debug(windowSession_corr.pathingUtil.get_distance_FromSelfCenter_messy(unitObj_Selected_curr));
              if (boundsShapeUnion.getParent() != windowSession_corr.panel_SemanticRoot) { throw new Error(); }
              int i = 0;
              while (true) {
                i++;
                if (i == 500) {
                  System.err.println("Really? Tried 500 times, still no valid point near?");
                  break;
                }
                //________________________________________________________________________
                //_________________________________________________________________
                //______________________________________
                double max = i * 2;
                double min = -i * 2;
                double posX = point_Target.getX() + ((Math.random() * (max - min)) + min);
                double posY = point_Target.getY() + ((Math.random() * (max - min)) + min);
                if (!boundsShapeUnion.contains(posX, posY)) {
                  Point2D point_NearTarget = boundsShapeUnion.localToParent(posX, posY);
                  //________________________________________________
                  queue_point.removeFirst();
                  queue_point.add(new Point(point_NearTarget.getX(), point_NearTarget.getY()));
                  goTo_TargetLocations_recursive(unitObj_Selected_curr, queue_point, speed, sn_CollisionRecursive, det_NoRecursion, targetPositionRegister, sn_RetryGotoTarget + 1, arr_unitObj_CollidedOn, time_LastCollide, true);
                  break;
                }
              }
            }
            //_________________________________
          }
          else {
            System.out.println("Retry reached & this is not the final destination (so, not occupied by another Movable Collider) -- so no final attempt");
          }
          return; //_______
        }
        else {
          System.err.println("This is final touch, continue below to see if success to go near. In either way, no more retry, & will return after this final recursion (to the code line above).");
        }
      }
    }

    //____
    long sn_CollisionRecursive_Final = ++sn_CollisionRecursive; //___________________________
    System.out.println("sn_CollisionRecursive (inc every time collides to a new unitObj / retry) :: " + sn_CollisionRecursive_Final);

    //_______________________________________________________________________________________
    //________________________________________________________
    //_______________________________________________________________________________________________________
    //______________________________________________________________________________________________________________________________________________________________________________________
    //______________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________________________________________________________________________________________________
    //_____________________
    //______________________________________________________________________________________________________________________________________________
    //________
    //___________________________________________________________________________
    //__________________________________________________________
    //________
    //________________________________________________________________________________
    //_______________________________________________________________
    //________________________________________________________
    //______________________
    //___________________________________
    //__________________
    //___________________________
    //___________________________________________________________
    //______________________
    //_______________
    //_______________________________________________________________
    //_________________________________________________________________________________________
    //________________________________________________________________________________
    //____________________________________________________________
    //______________________
    //_______________
    //_____________
    //________
    //______________________________________________________________
    //____________________________________________________________________________________________________________________________________________________
    //________________________________________________________________________________________________________________________________________
    //___________________________________________________________________________________________________
    //______________________________________________________________
    //________
    //________________________________________________________________________________________________________________________________________________________________________________
    //_______________________________________
    //_________________________________________
    //______________________________________________________________________________________________________________________________________
    //_______________________________________________________________________________________
    //___________________________________________
    //______________________________________________________________________________________________________________________________________________________________________________________________________________________________
    //_____________________
    //__________________________
    //____________________________________________________________________________
    //_____________________
    //___________________
    //________________________
    //______________________________________________________________________________________________________
    //___________________
    //________
    //______________________________________
    //___________________
    //______________________________________________
    //_____________________
    //_____________
    //____________________________________________________________________
    //_______________________________________
    //_____________
    //__________________
    //____________________________________
    //_____________
    //___________
    //_________

    //_____________________________________________________
    //___________________________________________________
    //_____________________________________________
    //___________________________________________________________________
    //____________________________________________________________________________________________
    //___________________________________________________________
    //___________________________________________________________

    //_____________________________________________
    final AnimationTimerMultiPhase animationTimer_prev = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
    if (animationTimer_prev != null) {    //__________________________________________________________________________________________________________________________
      if (animationTimer_prev.det_Terminated != true) {
        animationTimer_prev.terminate(); //_
      }
      else {
        //_______________________________
      }
    }

    Node node_Selected_curr = unitObj_Selected_curr.node_underlying;
    //_________________________________________
    //_________________________________________
    final AnimationTimerMultiPhase animationTimer = moveTo_TargetPoints_IncrementallyInAnimation(node_Selected_curr, queue_point, speed, targetPositionRegister);
    //_____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
    mpp__unitObj_Move__vs__animationTimer.put(unitObj_Selected_curr, animationTimer);

    if (animationTimer_prev != null) {
      //__________________________________________________________________
      //_____________________________________________________________________
      //______________________________________________________________________________________________________________
      //___________________________________________
      //_______
      animationTimer.arr_callback_RevertToLastMove.addAll(animationTimer_prev.arr_callback_RevertToLastMove);
    }

    //____
    unitObj_Selected_curr.collisionHandler = new CollisionHandler()
      {

        private long sn_Collide = 0;

        @Main
        @Messy
        @Override
        public void handle(UnitObj unitObj_AA, UnitObj unitObj_BB) {

          //_____
          //________________________________________________________________
          if (TrafficLight.class.isAssignableFrom(unitObj_BB.getClass())) {
            unitObj_BB.collisionHandler.handle(unitObj_BB, unitObj_AA); //______
            return;
          }

          long time_LastCollide_final = System.currentTimeMillis();
          sn_Collide++;
          System.out.println("collisionHandler.sn_collide (inc when stuck in same place -- cuz same collisionHandler is kept being invoked) :: " + sn_Collide);
          final long sn_Collide_final = sn_Collide;
          if (sn_Collide_final > 500) { throw new Error("Stucked? Keeps colliding in the same place?"); }
          animationTimer.terminate();

          arr_unitObj_CollidedOn.add(unitObj_BB);

          //__________________________________________________________________________________________________________________________________

          //______________________________________________________________________________________________________________________________________________
          //_______________________________________________________________________________________
          //_______________________________________________________________________________
          //_________________________________________________________

          LinkedList<Runnable> arr_callback_RevertToLastMove = animationTimer.arr_callback_RevertToLastMove;
          boolean det_OutofLv2CollisionZone = false;
          int sn_Revert = 0;
          while (!arr_callback_RevertToLastMove.isEmpty()) {
            sn_Revert++;
            if (sn_Revert > limit_Revert_OutOfLv2CollisionZone) { break; }
            //___________________________________________
            Runnable callback_RevertToLastMove_curr = arr_callback_RevertToLastMove.pollLast();
            //_______________________________________________________________________________________________________
            callback_RevertToLastMove_curr.run();

            Bounds bounds_AA = unitObj_AA.node_underlying.getBoundsInParent();
            BoundingBox bounds_BB_AdjLv2 = CollisionManager.get_BoundingBox_WithThreshold(unitObj_BB.node_underlying.getBoundsInParent(), CollisionManager.collusionDistanceThresholdLv2);
            if (!bounds_AA.intersects(bounds_BB_AdjLv2)) {
              det_OutofLv2CollisionZone = true;
              break;
            }
          }

          //____________________________________________________________________________________________________
          //_______________________________________________________________
          //______________________________________________________________________________________________________________________________________________________________
          //_________________________
          //________________
          //___________________________________________________________________________________________________________________________________
          //_______________________________________________________________
          //_____________________________________________________________________________________
          //_________________________________________________________________________________________________________________________________________________________________________________________________
          //_______________________________________________________________________________________________________________________________________________________________________________________________
          if (sn_Collide_final < sn_Collide) {
            //_____________________________________________________________________________________________________________________________________________________________
            System.out.println("Abandon sn_Collide :: " + sn_Collide_final);
            return;
          }
          //_______________________________________________________
          //______________________________________________________________________________________________________________________
          //________________________________________

          if (arr_callback_RevertToLastMove.isEmpty() && det_OutofLv2CollisionZone == false) {
            System.err.println("All revert tried, still in Lv2CollisionZone. Why? (Unless the CollideOn unitObj is dynamic)"); //_
          }
          if (det_OutofLv2CollisionZone == false) {
            System.out.println("[Debug 85%] Revert Limit reached, still not det_OutofLv2CollisionZone == true . :: " + limit_Revert_OutOfLv2CollisionZone); //_
          }
          if (unitObj_AA.node_underlying.getBoundsInParent().intersects(CollisionManager.get_BoundingBox_WithThreshold(unitObj_BB.node_underlying.getBoundsInParent(), CollisionManager.collusionDistanceThreshold))) {
            System.err.println("[Error 90%] Should Never, as long as 1 revert. Unless the CollideOn unitObj is dynamic (moving). (or precision pb, but kinda solved (in width margin) by rounding); Dyanmic should resolved by abandon prev collide handle .. but still has this pb.., seems all revert tried -- which can happen cuz the prev AnimationTimer is discard"); //_
          }
          //__________________________________________________________________________________________________________________
          //_________________________________________________________
          //___________________________________________________________________________________________________________________________
          //______________________________________________________________________________________________________________________________________________________________________
          //___________________________________________________________________________________________________________________________________________________________________________________________________
          //_____________________________________________________________________________________

          //_______________________
          //_________________________________
          System.out.println("This revert will cause speed pb. well Revert All out of Lv2CollisionZone increase reliability of not getting stuck & thus init Begin Path Intersect & fail search; but that shouldnt happen in the first place... still dk why; (currently just use line width BUTT (instead of ROUND) already can have good detection, but not perfect); prefer not to use this;");
          //____________________________________________

          //______________________________________________________

          final Point point_End_currSegment = queue_point.getFirst(); //__________________________________________________________________________________________________________________________________________________________
          //______________________________________________________________________________________________________________________________________________________________
          LinkedList<Point> arrPath_ShortestPath = repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine(unitObj_AA, unitObj_BB, point_End_currSegment);
          if (arrPath_ShortestPath == null || arrPath_ShortestPath.isEmpty()) {
            System.err.println("Empty, seems no Path found.");
            System.err.println("Retry later, cuz the CollideOn unitObj may be dynamic.");
            if (!det_FinalTouch_TryGoNearOccupiedLocation) { //________________________________________________________
              //________________________________________________________________
              final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("thd-RetryGotoTarget-%d").build());
              executor.schedule(() -> {
                Platform.runLater(() -> {
                  //________________________________________________________________________________________________________________________________________
                  //_________________________________________
                  //______
                  //____________________________________________
                  //_________________________________________________________________________
                  //_____________________________________________________________________________________________________________________________________________________________________________________________________
                  //____________________________________________________________________________________
                  //___________________________________________
                  //___________________________________________________________________________________________
                  //____________________
                  //_________________________________________________
                  //________________________________________________________
                  AnimationTimerMultiPhase animationTimer_existing = mpp__unitObj_Move__vs__animationTimer.get(unitObj_Selected_curr);
                  //________________________________________________________________________________________________________
                  if (animationTimer_existing == animationTimer) { //_________________________________________________________________________________________
                    if (!det_NoRecursion) {
                      System.out.println("//TODO retry does not respond to cancel");
                      goTo_TargetLocations_recursive(unitObj_Selected_curr, queue_point, speed, sn_CollisionRecursive_Final, false, targetPositionRegister, sn_RetryGotoTarget + 1, arr_unitObj_CollidedOn, time_LastCollide_final, det_FinalTouch_TryGoNearOccupiedLocation);
                    }
                    else {
                      System.err.println("Testing, det_NoRecursion = true");
                    }
                  }
                  else {
                    System.out.println("Retry for prev abandont, cuz curr already has a new Target.");
                  }

                  executor.shutdown();
                });
              }, delay_Retry, TimeUnit.MILLISECONDS);
            }
            else {
              System.err.println("This is already the Final Touch, No more retry.");
            }
          }
          else {
            if (arrPath_ShortestPath.size() < 3) { throw new Error("Shouldnt, shoud at least 3 (begin, redirect, end); 3 is rare, normally 4."); }
            arrPath_ShortestPath.removeFirst();
            arrPath_ShortestPath.removeLast(); //___________________________________________________________________________________________________
            queue_point.addAll(0, arrPath_ShortestPath);
            if (!det_NoRecursion) {
              goTo_TargetLocations_recursive(unitObj_Selected_curr, queue_point, speed, sn_CollisionRecursive_Final, false, targetPositionRegister, sn_RetryGotoTarget, arr_unitObj_CollidedOn, time_LastCollide_final, det_FinalTouch_TryGoNearOccupiedLocation); //_________
            }
            else {
              System.err.println("Testing, det_NoRecursion = true");
            }
          }
        }

      };
  }

  //______________________________________________________________________________________________
  //____________________________________________________________________________________
  //__________________________________________________________________________________________________________________________
  //______________________________________________________________________________________________________
  //______________________________________________________________________________________
  //________________________________________________________________________________
  /**
________________________________________________
___________________________________
__*/
  private static AnimationTimerMultiPhase moveTo_TargetPoints_IncrementallyInAnimation(final Node node_Move, LinkedList<Point> queue_point, final double speed, TargetPositionRegister targetPositionRegister) {
    //__________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

    //______________________________________________________________________
    AnimationTimerMultiPhase animationTimer = new AnimationTimerMultiPhase(node_Move, queue_point, diagonalInc, speed);
    animationTimer.targetPositionRegister = targetPositionRegister;
    animationTimer.start();
    return animationTimer;
  }

  //_____________

  private HashSet<Node> gp_PathingDebugShape = new HashSet<>();

  public final static boolean DebugMode = true;

  public void clear_PathingDebugShape() {
    ObservableList<Node> panel_SemanticRoot_getChildren = windowSession_corr.panel_SemanticRoot.getChildren();
    //___________________________________________
    //_____________________
    //______________________________________________________________________________
    //_____
    for (Node shape_curr : gp_PathingDebugShape) {
      panel_SemanticRoot_getChildren.remove(shape_curr); //
    }
    gp_PathingDebugShape.clear();
  }

  public void addTo_PathingDebugShape(Node node) {
    if (node == null) { throw new Error("Null"); }
    windowSession_corr.panel_SemanticRoot.getChildren().add(node);
    gp_PathingDebugShape.add(node);
  }

  public void removeFrom_PathingDebugShape(Node node) {
    if (node == null) { throw new Error("Null"); }
    windowSession_corr.panel_SemanticRoot.getChildren().remove(node);
    gp_PathingDebugShape.remove(node);
  }

  //__________________________

  @Deprecated
  private void repath_by_SavedSimpleAlg() {
    //_________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
  }

  //_____________

  //_____________
  //________________________________________________________________________________________________________________________________________________________
  //__________________________________

  private final static double diagonalInc = 1;

  @Deprecated
  private static final int limit_Revert_OutOfLv2CollisionZone = 1; //______________________________________________________________________________________________________

  @Deprecated
  private static final double adjust_BeginPathIntersectionWidth = 0; //____________________________________________________________________________________

  @Messy //_______
  public double get_distance_FromSelfCenter_messy(UnitObj unitObj_AA) {
    //_______________________________________________________________________________________________________________________________
    //_______________________________________________________________________________________________________________________________________________________________________________________________________________________
    //________________________________________________________
    //________________________________________________________________________
    //________________________________________________________________________________________________________________
    //______________________________________________________________________________________________________________________________
    //___________________________________________________________________________
    //___________________________________________________________________________________
    //_______________________________________________________________________________________________________________________________________________________________
    //_____________
    //________________________________________________________________________________________________________________________________________________________
    //_____________
    //_________________________________________________________________________________________________________
    //__________________________________________________________________________________________________________
    //____________________________________________________________
    //_______________________________________________________________________
    //_________________________________________________________________________________________________
    //______________________________________________
    //__________________________________________________________________________
    //______________
    //_____________
    //___________________________________________________________________________________________________________________________________________
    //____________________________________
    //____________________________________
    //_______________________________________________________________
    //_____________
    //_________________________________________________________________________________________________________
    //________________________________________________________
    //__________________________________________________________________________________________

    //___________________________________________________________________

    Bounds bounds_AA = unitObj_AA.node_underlying.getBoundsInParent();
    double ww = bounds_AA.getWidth();
    double hh = bounds_AA.getHeight();

    double radius_ori_debug = 13;
    String msg = "precision pb";
    if (ww != radius_ori_debug * 2) { System.out.print(msg + " :: " + ww); }
    if (hh != radius_ori_debug * 2) { System.out.print(msg + " :: " + hh); }

    double distance_FromSelfCenter_messy = Math.max(round(ww, 4), round(hh, 4)) / 2;
    //_____________________________________________________________________________________
    if (distance_FromSelfCenter_messy != radius_ori_debug) { System.out.print(msg + " :: " + distance_FromSelfCenter_messy); }
    System.out.println();
    return distance_FromSelfCenter_messy;
  }

  public static double round(double value, int scale) {
    return new BigDecimal(value).setScale(scale, RoundingMode.HALF_DOWN).doubleValue(); //_
  }

  //__________________________

  @Config
  public final static boolean det_ShortCircuit = true; //________________________________________________________________________
  //______________________________________________________________________
  //__________________________________________________________________________________________________________________________________________________________________________

  @Main
  private LinkedList<Point> repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine(final UnitObj unitObj_AA, final UnitObj unitObj_BB, Point point_End_currSegment) {
    //_____________________________
    final UnitObj unitObj_Selected_curr = unitObj_AA;
    Node node_Selected_curr = unitObj_Selected_curr.node_underlying;

    final double posX_Begin_currSegment = node_Selected_curr.getLayoutX();
    final double posY_Begin_currSegment = node_Selected_curr.getLayoutY(); //______________
    final Point point_Begin_currSegment = new Point(posX_Begin_currSegment, posY_Begin_currSegment); //______________________________________________
    System.out.println("point_Begin_currSegment :: " + point_Begin_currSegment);
    System.out.println("point_End_currSegment :: " + point_End_currSegment);

    final double posX_End_currSegment = point_End_currSegment.getX();
    final double posY_End_currSegment = point_End_currSegment.getY(); //______________

    final double distance_FromSelfCenter_messy = get_distance_FromSelfCenter_messy(unitObj_AA);

    //________________________________________________
    Triple<Shape, Shape, LinkedList<Point>> resultTup2 = get_BoundShapeUnion_and_Vertex(unitObj_AA, unitObj_BB, distance_FromSelfCenter_messy);
    final Shape boundsShapeUnion_AllBlockNear_AdjLv1 = resultTup2.getLeft();
    final Shape boundsShapeUnion_AllBlockNear_AdjLv2 = resultTup2.getMiddle();
    LinkedList<Point> arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin = resultTup2.getRight();

    //_____________________________________________________________________________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________________________________________
    final List<Point> arrUm_vertex_AllBlockNear_AdjLv2_SelfCenterMargin = Collections.unmodifiableList(arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin);

    //____
    HashSet<Point> gp_vertex_CandidatesNextToEnd = new HashSet<>();
    HashSet<Point> gp_vertex_CandidatesNextToBegin = new HashSet<>();
    HashSet<Point> gp_vertex_CandidatesOneShotToBeginEnd = new HashSet<>();
    int i = -1;
    for (Point vertex_curr : arrUm_vertex_AllBlockNear_AdjLv2_SelfCenterMargin) {
      i++;
      final Line line_currVertexNextToEnd = new Line(vertex_curr.getX(), vertex_curr.getY(), posX_End_currSegment, posY_End_currSegment);
      final Line line_currVertexNextToBegin = new Line(vertex_curr.getX(), vertex_curr.getY(), posX_Begin_currSegment, posY_Begin_currSegment);
      addTo_PathingDebugShape(line_currVertexNextToEnd);  //______________________________________________________________________
      addTo_PathingDebugShape(line_currVertexNextToBegin);
      line_currVertexNextToEnd.setStrokeWidth(distance_FromSelfCenter_messy * 2);
      line_currVertexNextToBegin.setStrokeWidth(distance_FromSelfCenter_messy * 2 + adjust_BeginPathIntersectionWidth); //_______________________________________________________________
      line_currVertexNextToEnd.setStroke(JavafxUtil.color_Green_dim);
      line_currVertexNextToBegin.setStroke(JavafxUtil.color_Green_dim);

      //
      boolean det_ShapeIntersect_currVertexNextToEnd = det_ShapeIntersect(line_currVertexNextToEnd, boundsShapeUnion_AllBlockNear_AdjLv2);
      //_______________________
      line_currVertexNextToBegin.setStrokeLineCap(StrokeLineCap.BUTT);
      boolean det_ShapeIntersect_currVertexNextToBegin = det_ShapeIntersect(line_currVertexNextToBegin, boundsShapeUnion_AllBlockNear_AdjLv1);

      //
      line_currVertexNextToEnd.setStrokeWidth(1.0); //__________________________
      line_currVertexNextToBegin.setStrokeWidth(1.0);
      //_______________
      if (!det_ShapeIntersect_currVertexNextToEnd) { gp_vertex_CandidatesNextToEnd.add(vertex_curr); }
      else { removeFrom_PathingDebugShape(line_currVertexNextToEnd); }
      if (!det_ShapeIntersect_currVertexNextToBegin) { gp_vertex_CandidatesNextToBegin.add(vertex_curr); }
      else { removeFrom_PathingDebugShape(line_currVertexNextToBegin); }
      //______________
      if (!det_ShapeIntersect_currVertexNextToEnd && !det_ShapeIntersect_currVertexNextToBegin) {
        gp_vertex_CandidatesOneShotToBeginEnd.add(vertex_curr);
        //______________________________________
      }
    }

    //_____
    if (gp_vertex_CandidatesNextToBegin.isEmpty()) {
      System.err.println("How can this be empty though? (-- No Path found)");
      return null;
    }
    //___________________________________________________________________________________________________________________________________
    if (gp_vertex_CandidatesNextToEnd.isEmpty()) {
      System.err.println("No Path found");
      return null;
    }

    //__________________
    if (gp_vertex_CandidatesOneShotToBeginEnd.isEmpty()) {
      final Set<Point> gpUm_vertex_CandidatesNextToEnd = Collections.unmodifiableSet(gp_vertex_CandidatesNextToEnd);
      final Set<Point> gpUm_vertex_CandidatesNextToBegin = Collections.unmodifiableSet(gp_vertex_CandidatesNextToBegin);

      //____
      //__________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
      LinkedList<Point> pathArr_ShortestPath = repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_Permutation(point_Begin_currSegment, point_End_currSegment, gpUm_vertex_CandidatesNextToBegin, gpUm_vertex_CandidatesNextToEnd, arrUm_vertex_AllBlockNear_AdjLv2_SelfCenterMargin, boundsShapeUnion_AllBlockNear_AdjLv2, distance_FromSelfCenter_messy, det_ShortCircuit);
      return pathArr_ShortestPath;
    }
    //__________________
    else {
      //_________________
      //_________________________
      //_______________________________________________
      //_______________________________________________________________
      //_________________________________________________________________________________________
      //____________________________________________________________________________
      //____________________________________________________________________________________

      //______________________________
      //_____________________________________________________________
      //________________________________________________________________

      //_____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

      //_________________________________________________________________________
      //__________________________________________________________________________________________
      //_____________________________________________________________________________________________________________
      //__________________________________________________________________________________________

      //_______________________________________________________
      //_______________________________________________________________

      LinkedList<LinkedList<Point>> arr_pathArr_Planning = new LinkedList<>();

      for (Point point_Oneshot_curr : gp_vertex_CandidatesOneShotToBeginEnd) {
        LinkedList<Point> pathArr_Planning = new LinkedList<Point>();
        pathArr_Planning.add(point_Begin_currSegment);
        pathArr_Planning.add(point_Oneshot_curr);
        pathArr_Planning.add(point_End_currSegment);

        arr_pathArr_Planning.add(pathArr_Planning);
      }

      LinkedList<Point> pathArr_ShortestPath = get_ShortestPath(arr_pathArr_Planning);
      return pathArr_ShortestPath;
    }

  }

  //________________

  @Todo
  private void repath_by_WrapVertexSearchAlg_by_StraightLineCorner_with_DirectionalTangentLine_with_Permutation() {
    //____________________________________
    //_________________________________________________________
    //___________________________
    //______________________
    //________________
  }

  //________________

  @Main
  private LinkedList<Point> repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_Permutation(final Point point_Begin_currSegment, Point point_End_currSegment,
                                                                                                     final Set<Point> gpUm_vertex_CandidatesNextToBegin,
                                                                                                     final Set<Point> gpUm_vertex_CandidatesNextToEnd,
                                                                                                     final List<Point> arrUm_vertex_AllBlockNear_AdjLv2_SelfCenterMargin,
                                                                                                     final Shape boundsShapeUnion_AllBlockNear_AdjLv2, final double distance_FromSelfCenter_messy,
                                                                                                     final boolean det_ShortCircuit) {

    LinkedList<LinkedList<Point>> arr_pathArr_Planning = new LinkedList<>();
    for (Point vertex_CandidatesNextToBegin_curr : gpUm_vertex_CandidatesNextToBegin) {
      LinkedList<Point> pathArr_Planning = new LinkedList<Point>();
      pathArr_Planning.add(point_Begin_currSegment);
      pathArr_Planning.add(vertex_CandidatesNextToBegin_curr);

      int sn_Recursion = 0;

      repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_Permutation_recursive(point_Begin_currSegment, vertex_CandidatesNextToBegin_curr, pathArr_Planning,
                                                                                         arrUm_vertex_AllBlockNear_AdjLv2_SelfCenterMargin, gpUm_vertex_CandidatesNextToEnd,
                                                                                         sn_Recursion,
                                                                                         boundsShapeUnion_AllBlockNear_AdjLv2, distance_FromSelfCenter_messy,
                                                                                         arr_pathArr_Planning,
                                                                                         det_ShortCircuit);

    }

    //_____________________________________________________
    //______________________
    //______________________________________________________________________________________
    //_________________________________________________________________________________
    if (arr_pathArr_Planning.isEmpty()) {
      System.err.println("Empty, No Path found?");
      return null;
    }
    else {

      for (LinkedList<Point> pathArr_Planning_curr : arr_pathArr_Planning) {
        pathArr_Planning_curr.add(point_End_currSegment); //___________________
      }

      //__________________
      LinkedList<Point> pathArr_ShortestPath = get_ShortestPath(arr_pathArr_Planning);
      return pathArr_ShortestPath;
    }

  }

  @Main
  private Boolean repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_Permutation_recursive(Point point_currPlanToGoInPath, Point point_nextPlanToGoInPath_Pivot,
                                                                                                     LinkedList<Point> pathArr_Planning_buildOn_prev,
                                                                                                     final List<Point> arrUm_vertex_AllBlockNear_NotInsideBlock,
                                                                                                     final Set<Point> gpUm_vertex_CandidatesNextToEnd,
                                                                                                     int sn_Recursion,
                                                                                                     final Shape boundsShapeUnion_AllBlockNear_AdjLv2, final double distance_FromSelfCenter_messy,
                                                                                                     LinkedList<LinkedList<Point>> arr_pathArr_Planning_MissingEnd,
                                                                                                     final boolean det_ShortCircuit) {
    sn_Recursion++;
    //_____________________________________

    //____
    Pair<Function<Tup4<Double, Double, Double, Double>, Tup4<Point, Double, Double, Boolean>>, Function<Tup4<Point, Double, Double, Boolean>, Boolean>> results = get_Calculator_of_DirectionalTangentLine_and_LineSide(point_currPlanToGoInPath, point_nextPlanToGoInPath_Pivot, pathArr_Planning_buildOn_prev, sn_Recursion);
    Function<Tup4<Double, Double, Double, Double>, Tup4<Point, Double, Double, Boolean>> detm_OnLeftSideOfTan_ForSideOO = results.getLeft();
    Function<Tup4<Point, Double, Double, Boolean>, Boolean> detm_OnOppSideOfTan_RelToSideOO = results.getRight();

    //____

    //_______________________
    Tup4<Point, Double, Double, Boolean> resultTup4__detm_OnLeftSideOfTan_ForSideOO = detm_OnLeftSideOfTan_ForSideOO.apply(null);

    //________________________________
    boolean det_EndReached = false;
    //________________________________________________________________________
    for (Point vertex_currPathPoint : arrUm_vertex_AllBlockNear_NotInsideBlock) {
      //_______________________________________________________________________________________
      //________________________________________________________________________________________________________________________________________________________________________________________________
      //____________________________________________________________________________________________________
      if (pathArr_Planning_buildOn_prev.contains(vertex_currPathPoint)) {
        //___________________________________
        continue;
      }
      else {
        //__________________________________________
        resultTup4__detm_OnLeftSideOfTan_ForSideOO.i1 = vertex_currPathPoint;
        Boolean det_OnOppSideOfTan_RelToSideOO = detm_OnOppSideOfTan_RelToSideOO.apply(resultTup4__detm_OnLeftSideOfTan_ForSideOO);

        @Debug
        LinkedList<Point> arr_vertex_OppSideTan = new LinkedList<>();

        if (det_OnOppSideOfTan_RelToSideOO) {
          if (det_EndReached == true && !gpUm_vertex_CandidatesNextToEnd.contains(vertex_currPathPoint)) {
            //____________________________________________________________________________________________________________________________________
            continue; //________
            //______________________________________________________________________________________________________________________
            //__________________________________________________________________________________________________________________
          }

          //________________________
          final Line line_currInPath = new Line(point_nextPlanToGoInPath_Pivot.getX(), point_nextPlanToGoInPath_Pivot.getY(), vertex_currPathPoint.getX(), vertex_currPathPoint.getY());
          line_currInPath.setStrokeWidth(distance_FromSelfCenter_messy * 2);
          addTo_PathingDebugShape(line_currInPath); //__________________________________
          line_currInPath.setStroke(JavafxUtil.color_Green_dim_opaLight);
          boolean det_Intersect = det_ShapeIntersect(line_currInPath, boundsShapeUnion_AllBlockNear_AdjLv2);
          if (det_Intersect) {
            //_____________________________
            //__________________________________________________________
            continue;
          }
          else {
            //_________________
            arr_vertex_OppSideTan.add(vertex_currPathPoint);

            LinkedList<Point> pathArr_Planning_buildOn_curr = new LinkedList<>(pathArr_Planning_buildOn_prev);
            pathArr_Planning_buildOn_curr.add(vertex_currPathPoint);

            //__________________________

            //__________________________________________________________________________________________________________________________________________________________
            if (gpUm_vertex_CandidatesNextToEnd.contains(vertex_currPathPoint)) {
              //___________________________________________________________________________________
              det_EndReached = true;

              arr_pathArr_Planning_MissingEnd.add(pathArr_Planning_buildOn_curr);
              //__________________

              //
              if (det_ShortCircuit) {
                return true; //___________________________________
              }
            }
            else {
              //_________________________________________________________________________________________
              Boolean det_OneFound = repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_Permutation_recursive(point_nextPlanToGoInPath_Pivot, vertex_currPathPoint, pathArr_Planning_buildOn_curr,
                                                                                                                        arrUm_vertex_AllBlockNear_NotInsideBlock, gpUm_vertex_CandidatesNextToEnd,
                                                                                                                        sn_Recursion, boundsShapeUnion_AllBlockNear_AdjLv2, distance_FromSelfCenter_messy,
                                                                                                                        arr_pathArr_Planning_MissingEnd,
                                                                                                                        det_ShortCircuit); //__
              if (det_ShortCircuit && det_OneFound != null && det_OneFound == true) { //_________
                return true; //_________________________________________________________________________
              }

            }
          }
        }
      }
    }
    return null; //______

    //_________________________________________________
    //______________________________________________________________________________________________________________________________
    //_______________________________
    //_____________
    //_____

  }

  //________________

  @Deprecated
  private LinkedList<Point> repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_ShortestGotoPoint(final Point point_Begin_currSegment, Point point_End_currSegment,
                                                                                                           final Set<Point> gpUm_vertex_CandidatesNextToBegin,
                                                                                                           final Set<Point> gpUm_vertex_CandidatesNextToEnd,
                                                                                                           final List<Point> arrUm_vertex_AllBlockNear_AdjLv2_SelfCenterMargin,
                                                                                                           final Shape boundsShapeUnion_AllBlockNear_AdjLv2, final double distance_FromSelfCenter_messy) {

    LinkedList<LinkedList<Point>> arr_pathArr_Planning = new LinkedList<>();
    for (Point vertex_CandidatesNextToBegin_curr : gpUm_vertex_CandidatesNextToBegin) {
      LinkedList<Point> pathArr_Planning = new LinkedList<Point>();
      pathArr_Planning.add(point_Begin_currSegment);
      pathArr_Planning.add(vertex_CandidatesNextToBegin_curr);

      //______________________________
      //___________________________________________________________________________
      //__________________________________________________________________
      //____________________________________________________________________________

      int sn_Recursion = 0;

      repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_ShortestGotoPoint_recursive(point_Begin_currSegment, vertex_CandidatesNextToBegin_curr, pathArr_Planning,
                                                                                               arrUm_vertex_AllBlockNear_AdjLv2_SelfCenterMargin, gpUm_vertex_CandidatesNextToEnd,
                                                                                               sn_Recursion, boundsShapeUnion_AllBlockNear_AdjLv2, distance_FromSelfCenter_messy);

      //__________________
      if (pathArr_Planning.isEmpty()) { //____________________________________
        //_____________________________________________________________________________________________________________________
        //___________________________________________
        //________________________________________________
        //_________________________________________________
        //______________________________________________________________________________________
        //___________________________________
        //_______________________________________________________________________________________________________________________
        //__________________________
        System.out.println("[Debug 20%] pathArr_Planning == null -- This is Not a valid path.");
        continue;
      }
      //__________________
      else {
        if (!gpUm_vertex_CandidatesNextToEnd.contains(pathArr_Planning.getLast())) {
          throw new Error("Not_Reachable Sth is WRONG. why? Not this Path. Coulbe be the End-Candidate Shortest path Overwrite Pb (Should be Sovled though)");
          //________________________________________________________________________________________________________________________________
        }

        arr_pathArr_Planning.add(pathArr_Planning);
        pathArr_Planning.add(point_End_currSegment); //___________________
      }

    }

    if (arr_pathArr_Planning.isEmpty()) { throw new Error("No path found?"); }

    //__________________
    LinkedList<Point> pathArr_ShortestPath = get_ShortestPath(arr_pathArr_Planning);
    return pathArr_ShortestPath;
  }

  @Main
  private Pair<Function<Tup4<Double, Double, Double, Double>, Tup4<Point, Double, Double, Boolean>>, Function<Tup4<Point, Double, Double, Boolean>, Boolean>> get_Calculator_of_DirectionalTangentLine_and_LineSide(Point point_currPlanToGoInPath, Point point_nextPlanToGoInPath_Pivot, LinkedList<Point> pathArr_Planning, int sn_Recursion) {
    //____

    //__________________
    final double posX_currPlanToGo = point_currPlanToGoInPath.getX();
    final double posYn_currPlanToGo = -point_currPlanToGoInPath.getY(); //__________
    //____________
    final double posX_nextPlanToGo = point_nextPlanToGoInPath_Pivot.getX();
    final double posYn_nextPlanToGo = -point_nextPlanToGoInPath_Pivot.getY(); //__________

    //_____________
    double displacementX = posX_nextPlanToGo - posX_currPlanToGo;
    double displacementY = posYn_nextPlanToGo - posYn_currPlanToGo;

    //_____________________________________________________________________________________
    //___________________________________________________________________________________________________________________________________________
    //__________________________________________________________________________________________________________________________________________
    if (sn_Recursion == 1) {
      if (pathArr_Planning.size() != 2) { throw new Error("Should equal 2..."); }
      if (displacementX != 0 && displacementY != 0) {
        double m_CalibrateForBeginSegment = Math.abs(displacementY / displacementX);
        if (m_CalibrateForBeginSegment < 1) {
          displacementY = 0;
        }
        else if (m_CalibrateForBeginSegment > 1) { //_____________________________________________________
          displacementX = 0;
        }
        else {
          System.err.println("[Rare 60%] When_Equal -- can happen cuz the way the Wall Align up in 45deg. :: m_CalibrateForBeginSegment=" + m_CalibrateForBeginSegment);
        }
      }
      else {
        //_____________________________________________________________
      }
    }

    final double displacementX_final = displacementX;
    final double displacementY_final = displacementY;

    //____
    Function<Tup4<Double, Double, Double, Double>, Tup4<Point, Double, Double, Boolean>> detm_OnLeftSideOfTan_ForSideOO = null;
    Function<Tup4<Point, Double, Double, Boolean>, Boolean> detm_OnOppSideOfTan_RelToSideOO = null;

    if (displacementX_final == 0 && displacementY_final == 0) {
      throw new Error("Not_Reachable -- Means you clicked on same position where the UnitObj is currently at -- should not trigger any Collision & RePathing.");
    }
    else if (displacementX_final == 0) {
      detm_OnLeftSideOfTan_ForSideOO  = t -> {
                                        //____________________________________________________________________________________
                                        //____________________________________________________________________

                                        Boolean det_OnUpperSideOfTan_ForSideOO = null;
                                        if (displacementY_final > 0) { //________________________________
                                          det_OnUpperSideOfTan_ForSideOO = false;
                                        }
                                        else if (displacementY_final < 0) {
                                          det_OnUpperSideOfTan_ForSideOO = true;
                                        }
                                        else {
                                          throw new Error("Not_Reachable");
                                        }

                                        return new Tup4<>(null, null, null, det_OnUpperSideOfTan_ForSideOO);
                                      };

      detm_OnOppSideOfTan_RelToSideOO = t -> {
                                        Point vertex_currPathPoint = t.i1;
                                        Boolean det_OnUpperSideOfTan_ForSideOO = t.i4;

                                        final double posX_vertex_currPathPoint = vertex_currPathPoint.getX();
                                        final double posYn_vertex_currPathPoint = -vertex_currPathPoint.getY(); //_______________________________________________

                                        Boolean det_OnOppSideOfTan_RelToSideOO = null;
                                        if (!det_OnUpperSideOfTan_ForSideOO) {
                                          det_OnOppSideOfTan_RelToSideOO = posYn_vertex_currPathPoint >= posYn_nextPlanToGo;
                                        }
                                        else {
                                          det_OnOppSideOfTan_RelToSideOO = posYn_vertex_currPathPoint <= posYn_nextPlanToGo;
                                        }

                                        //__________________________________________________________________________________________

                                        //_____________________________________________
                                        //__________________________________________________________

                                        return det_OnOppSideOfTan_RelToSideOO;
                                      };

    }
    else if (displacementY_final == 0) {
      detm_OnLeftSideOfTan_ForSideOO  = t -> {
                                        Boolean det_OnLeftSideOfTan_ForSideOO = null;
                                        if (displacementX_final > 0) { //___________________________________
                                          det_OnLeftSideOfTan_ForSideOO = true;
                                        }
                                        else if (displacementX_final < 0) {
                                          det_OnLeftSideOfTan_ForSideOO = false;
                                        }
                                        else {
                                          throw new Error("Not_Reachable");
                                        }

                                        return new Tup4<>(null, null, null, det_OnLeftSideOfTan_ForSideOO);
                                      };

      detm_OnOppSideOfTan_RelToSideOO = t -> {
                                        Point vertex_currPathPoint = t.i1;
                                        Boolean det_OnLeftSideOfTan_ForSideOO = t.i4;

                                        final double posX_vertex_currPathPoint = vertex_currPathPoint.getX();
                                        final double posYn_vertex_currPathPoint = -vertex_currPathPoint.getY(); //__________

                                        Boolean det_OnOppSideOfTan_RelToSideOO = null;
                                        if (det_OnLeftSideOfTan_ForSideOO == true) {
                                          det_OnOppSideOfTan_RelToSideOO = posX_vertex_currPathPoint >= posX_nextPlanToGo;
                                        }
                                        else {
                                          det_OnOppSideOfTan_RelToSideOO = posX_vertex_currPathPoint <= posX_nextPlanToGo;
                                        }

                                        return det_OnOppSideOfTan_RelToSideOO;
                                      };

    }
    else {
      detm_OnLeftSideOfTan_ForSideOO  = resultTup4 -> {
                                        //_____________________________________________
                                        //_____________________________________________
                                        //__________________________________________________
                                        //_________________________________________________

                                        final double m = displacementY_final / displacementX_final; //_________________________________________________________________________________________________________
                                        final double b = posYn_currPlanToGo - posX_currPlanToGo * m;
                                        if (m == 0 || Double.isNaN(m)) { throw new Error("Not_Reachable"); }

                                        //___________________________________________________
                                        //____________________________________________________________________________________________________________________________
                                        double m_tan = -1 / m;
                                        double b_tan = posYn_nextPlanToGo - m_tan * posX_nextPlanToGo;

                                        //_____________________________________________________________________
                                        //____________________________________________________________________
                                        //_____________________________________________________________________________________

                                        double posX_OnTangentLine_VertexBegin = (posYn_currPlanToGo - b_tan) / m_tan;
                                        double displacementX_FromTangentLine_VertexBegin = posX_currPlanToGo - posX_OnTangentLine_VertexBegin;

                                        Boolean det_OnLeftSideOfTan_ForSideOO = null;
                                        if (displacementX_FromTangentLine_VertexBegin < 0) {
                                          det_OnLeftSideOfTan_ForSideOO = true;
                                        }
                                        else if (displacementX_FromTangentLine_VertexBegin > 0) {
                                          det_OnLeftSideOfTan_ForSideOO = false;
                                        }
                                        else {
                                          throw new Error("Not_Reachable");
                                        }

                                        return new Tup4<>(null, m_tan, b_tan, det_OnLeftSideOfTan_ForSideOO);
                                      };

      detm_OnOppSideOfTan_RelToSideOO = resultTup4 -> {
                                        Point vertex_currPathPoint = resultTup4.i1;
                                        Double m_tan = resultTup4.i2;
                                        Double b_tan = resultTup4.i3;
                                        Boolean det_OnLeftSideOfTan_ForSideOO = resultTup4.i4;

                                        final double posX_vertex_currPathPoint = vertex_currPathPoint.getX();
                                        final double posYn_vertex_currPathPoint = -vertex_currPathPoint.getY(); //__________

                                        double posX_OnTangentLine_currWrapLoop = (posYn_vertex_currPathPoint - b_tan) / m_tan;
                                        double displacementX_FromTangentLine_currWrapLoop = posX_vertex_currPathPoint - posX_OnTangentLine_currWrapLoop;

                                        double posY_OnTangentLine_currWrapLoop = (posX_vertex_currPathPoint * m_tan + b_tan);
                                        double displacementY_FromTangentLine_currWrapLoop = posYn_vertex_currPathPoint - posY_OnTangentLine_currWrapLoop;

                                        //_____________________________________________________________________________________________________
                                        Line line_TangentToDirection = new Line(posX_OnTangentLine_currWrapLoop, -posYn_vertex_currPathPoint, posX_vertex_currPathPoint, -posY_OnTangentLine_currWrapLoop); //_______________
                                        addTo_PathingDebugShape(line_TangentToDirection);
                                        line_TangentToDirection.setStroke(JavafxUtil.color_Cyan);

                                        Boolean det_OnOppSideOfTan_RelToSideOO = null; //_______________________________
                                        if (det_OnLeftSideOfTan_ForSideOO == true) {
                                          det_OnOppSideOfTan_RelToSideOO = displacementX_FromTangentLine_currWrapLoop >= 0; //________________________________________
                                        }
                                        else {
                                          det_OnOppSideOfTan_RelToSideOO = displacementX_FromTangentLine_currWrapLoop <= 0;
                                        }
                                        if (displacementX_FromTangentLine_currWrapLoop == 0) {
                                          if (displacementY_FromTangentLine_currWrapLoop == 0) {
                                            System.out.print(vertex_currPathPoint + " :: ");
                                            System.out.println("[Debug 20%; Rare 5%] . Already On the Tangent Line? Will be a Candidate too.");
                                          }
                                          else {
                                            System.err.println("Should both 0, dk precision pb :: " + displacementY_FromTangentLine_currWrapLoop);
                                          }
                                        }

                                        return det_OnOppSideOfTan_RelToSideOO;
                                      };
    }

    return new ImmutablePair<>(detm_OnLeftSideOfTan_ForSideOO, detm_OnOppSideOfTan_RelToSideOO);
  }

  @Deprecated
  private void repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_ShortestGotoPoint_recursive(Point point_currPlanToGoInPath, Point point_nextPlanToGoInPath_Pivot,
                                                                                                        LinkedList<Point> pathArr_Planning,
                                                                                                        final List<Point> arrUm_vertex_AllBlockNear_NotInsideBlock,
                                                                                                        final Set<Point> gpUm_vertex_CandidatesNextToEnd,
                                                                                                        int sn_Recursion,
                                                                                                        final Shape boundsShapeUnion_AllBlockNear_AdjLv2, final double distance_FromSelfCenter_messy) {
    sn_Recursion++;

    //____
    Pair<Function<Tup4<Double, Double, Double, Double>, Tup4<Point, Double, Double, Boolean>>, Function<Tup4<Point, Double, Double, Boolean>, Boolean>> results = get_Calculator_of_DirectionalTangentLine_and_LineSide(point_currPlanToGoInPath, point_nextPlanToGoInPath_Pivot, pathArr_Planning, sn_Recursion);
    Function<Tup4<Double, Double, Double, Double>, Tup4<Point, Double, Double, Boolean>> detm_OnLeftSideOfTan_ForSideOO = results.getLeft();
    Function<Tup4<Point, Double, Double, Boolean>, Boolean> detm_OnOppSideOfTan_RelToSideOO = results.getRight();

    //____

    //_______________________
    Tup4<Point, Double, Double, Boolean> resultTup4__detm_OnLeftSideOfTan_ForSideOO = detm_OnLeftSideOfTan_ForSideOO.apply(null);

    //________________________________
    Point vertex_OppSideTan_ClosestToTan = null;
    boolean det_EndReached = false;
    Double distance_ToPivot_Closest = null;
    for (Point vertex_currPathPoint : arrUm_vertex_AllBlockNear_NotInsideBlock) {
      if (pathArr_Planning.contains(vertex_currPathPoint)) { //_____________________________________________________________
        //___________________________________
        continue;
      }
      else {
        //__________________________________________
        resultTup4__detm_OnLeftSideOfTan_ForSideOO.i1 = vertex_currPathPoint;
        Boolean det_OnOppSideOfTan_RelToSideOO = detm_OnOppSideOfTan_RelToSideOO.apply(resultTup4__detm_OnLeftSideOfTan_ForSideOO);

        LinkedList<Point> arr_vertex_OppSideTan = new LinkedList<>();
        if (det_OnOppSideOfTan_RelToSideOO) {
          if (det_EndReached == true && !gpUm_vertex_CandidatesNextToEnd.contains(vertex_currPathPoint)) {
            continue; //________
            //______________________________________________________________________________________________________________________
            //__________________________________________________________________________________________________________________
          }

          //________________________

          //_____________________________________________________________________________________________________________
          //____________________________________________________________________
          //__
          //_____________________________________________________________
          //_______________________________________________________________________________
          //
          //___________________________________________________________________________________________________
          //____________________________________________________________________________________________________________________________________________
          //____________________________________________________
          //_________________________________________________________________
          final Line line_currInPath = new Line(point_nextPlanToGoInPath_Pivot.getX(), point_nextPlanToGoInPath_Pivot.getY(), vertex_currPathPoint.getX(), vertex_currPathPoint.getY());
          //_____________________________________________________________________________________________________________________________________________________________________________________________
          line_currInPath.setStrokeWidth(distance_FromSelfCenter_messy * 2);
          addTo_PathingDebugShape(line_currInPath); //__________________________________
          line_currInPath.setStroke(JavafxUtil.color_Green_dim_opaLight);
          boolean det_Intersect = det_ShapeIntersect(line_currInPath, boundsShapeUnion_AllBlockNear_AdjLv2);
          if (det_Intersect) {
            //_____________________________
            //__________________________________________________________
            continue;
          }
          else {
            //_______________________________________________

            arr_vertex_OppSideTan.add(vertex_currPathPoint);

            //_______________________________________________________________________________________________________________________________________________________________________
            //_____________________________________________________________________________________________________________________________________________________
            //____________________________________________________________________________________________________________________________________________________________________________________________________
            //______________________________________________________________________________________________________________

            double distance_ToPivot = vertex_currPathPoint.distance(point_nextPlanToGoInPath_Pivot);

            if (distance_ToPivot_Closest == null || distance_ToPivot < distance_ToPivot_Closest) {
              vertex_OppSideTan_ClosestToTan = vertex_currPathPoint;
              distance_ToPivot_Closest       = distance_ToPivot;
            }
            else if (distance_ToPivot == distance_ToPivot_Closest) { System.err.println("[Rare 0.5%] When_Equal :: " + vertex_currPathPoint + " :: " + vertex_OppSideTan_ClosestToTan); }

            //__________________________

            //__________________________________________________________________________________________________________________________________________________________
            //________________________________________________________________________________________________________
            if (det_EndReached == false && gpUm_vertex_CandidatesNextToEnd.contains(vertex_currPathPoint)) {
              //_______
              //___________________________________________________________________________________
              //________________________________________________________________
              //_____________________________________________________
              //______________________________________________________
              //_________________
              det_EndReached                 = true;
              //___________________________________________________________________________________________________________________________
              vertex_OppSideTan_ClosestToTan = vertex_currPathPoint;
              distance_ToPivot_Closest       = distance_ToPivot;
            }
          }

        }
        //___________________________________________________________________________
      }
    }

    if (vertex_OppSideTan_ClosestToTan == null) {
      System.out.println("[Debug 20%] vertex_OppSideTan_ClosestToTan is null... didnt find any? May need check another path");
      //___________________________________________
      //_________________________________________________________________________
      //________________________________
      //____________________________________
      pathArr_Planning.clear();
      return;
    }

    pathArr_Planning.add(vertex_OppSideTan_ClosestToTan);

    //
    if (!det_EndReached) {
      repath_by_WrapVertexSearchAlg_by_DirectionalTangentLine_with_ShortestGotoPoint_recursive(point_nextPlanToGoInPath_Pivot, vertex_OppSideTan_ClosestToTan, pathArr_Planning,
                                                                                               arrUm_vertex_AllBlockNear_NotInsideBlock, gpUm_vertex_CandidatesNextToEnd, sn_Recursion, boundsShapeUnion_AllBlockNear_AdjLv2, distance_FromSelfCenter_messy); //__
    }
    return; //

    //__________________________________
  }

  //__________________________

  @Main
  private LinkedList<Point> repath_by_WrapVertexSearchAlg_by_PermutationAndIntersection(final UnitObj unitObj_AA, final UnitObj unitObj_BB, Point point_End_currSegment) {
    //_____
    final UnitObj unitObj_Selected_curr = unitObj_AA;
    Node node_Selected_curr = unitObj_Selected_curr.node_underlying;

    final double posX_Begin_currSegment = node_Selected_curr.getLayoutX();
    final double posY_Begin_currSegment = node_Selected_curr.getLayoutY();
    final Point point_Begin_currSegment = new Point(posX_Begin_currSegment, posY_Begin_currSegment);
    System.out.println("point_Begin_currSegment :: " + point_Begin_currSegment);
    System.out.println("point_End_currSegment :: " + point_End_currSegment);

    final double posX_End_currSegment = point_End_currSegment.getX();
    final double posY_End_currSegment = point_End_currSegment.getY();

    final double distance_FromSelfCenter_messy = get_distance_FromSelfCenter_messy(unitObj_AA);

    //__________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

    //________________________________________________
    Triple<Shape, Shape, LinkedList<Point>> resultTup2 = get_BoundShapeUnion_and_Vertex(unitObj_AA, unitObj_BB, distance_FromSelfCenter_messy);
    final Shape boundsShapeUnion_AllBlockNear_AdjLv1 = resultTup2.getLeft();
    final Shape boundsShapeUnion_AllBlockNear_AdjLv2 = resultTup2.getMiddle();
    LinkedList<Point> arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin = resultTup2.getRight();

    LinkedList<Point> arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin__plus_point_BeginEnd = new LinkedList<>(arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin);
    arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin__plus_point_BeginEnd.addFirst(point_End_currSegment);
    arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin__plus_point_BeginEnd.addFirst(point_Begin_currSegment);

    //____________________________________
    final BiPredicate<LinkedList<Point>, Point> pred_AbandonCurrPath = (arr_BuildOn_prev, point_currInPath) -> {
      if (arr_BuildOn_prev == null || arr_BuildOn_prev.isEmpty()) { //________________________________________________________________
        return point_currInPath != point_Begin_currSegment;
      }
      else {
        final Point point_prevInPath = arr_BuildOn_prev.getLast();
        final Line line_currInPath = new Line(point_prevInPath.getX(), point_prevInPath.getY(), point_currInPath.getX(), point_currInPath.getY());
        //______________________
        //________________________________________________________________________
        //_____________________________________________________
        //________________________________________________________________________________________________________________________________________________________
        //_________________________________________________________
        //__________________________________________
        //______________________________________________________________________
        //_____________________________________
        //_________________________________________

        //________________________________________________________________________
        //__________________________________________________
        //_____________
        //__________________
        //________________________________________________________________________________
        //_______________________________________________________________________________________________________
        //_____________
        //__________________________________________________________________________________________
        //____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
        //______________________________________________________________________________________________________________

        //______________________________________________________________________________________
        //_____________________________________
        //______________________________________________________________________
        if (point_prevInPath == point_Begin_currSegment) {
          //___________________________________________________________________________________
          //________________________________
          //_________________________________________
          //________________________
          //________________________________________________________________________
          //________________________________________________________________________________________________________________________________________________________________________________________________________
          line_currInPath.setStrokeWidth(distance_FromSelfCenter_messy * 2 + adjust_BeginPathIntersectionWidth);
          line_currInPath.setStrokeLineCap(StrokeLineCap.BUTT);
        }
        else {
          line_currInPath.setStrokeWidth(distance_FromSelfCenter_messy * 2);
        }
        line_currInPath.setStroke(JavafxUtil.color_Green_dim_opaLight);
        addTo_PathingDebugShape(line_currInPath); //__________________________________

        boolean det_Intersect;
        if (point_prevInPath == point_Begin_currSegment) {
          //______________________________________________________________________________________________
          //______________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
          det_Intersect = det_ShapeIntersect(line_currInPath, boundsShapeUnion_AllBlockNear_AdjLv1);
        }
        else {
          det_Intersect = det_ShapeIntersect(line_currInPath, boundsShapeUnion_AllBlockNear_AdjLv2);
        }
        if (det_Intersect) {
          removeFrom_PathingDebugShape(line_currInPath); //________________________________________________________________
        }
        return det_Intersect;
        //________________________________________________________________________________________________________________________________________
        //___________________________________________________________________________________________________________
      }
    };
    final BiPredicate<LinkedList<Point>, Point> pred_AbandonAfterCurrPath = (arr_BuildOn_prev, point_currInPath) -> {
      return point_currInPath == point_End_currSegment; //________________________________________________________________
    }; //_____________________________________________________________________________________________________________
    LinkedList<LinkedList<Point>> arr_pathArr_AllNotIntersectingPaths_IncludePartialPathSegments = MathUtil.get_Permuataion_Scattered(arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin__plus_point_BeginEnd, pred_AbandonCurrPath, pred_AbandonAfterCurrPath);
    //_________________________________________________________________________________________________________________________________

    LinkedList<LinkedList<Point>> arr_pathArr_AllValidPaths = new LinkedList<>();
    for (LinkedList<Point> pathArr_curr : arr_pathArr_AllNotIntersectingPaths_IncludePartialPathSegments) {
      //________________________________________________________________________________________
      //__________________________________________________________________________________________________________________________________________
      if (pathArr_curr.size() == 1) { continue; }
      if (pathArr_curr.size() == 2) { continue; }

      //_______________________________________________________________________________________
      //______________________________________________________________________________________________________________
      //________________________________________
      //___________________________________________________
      //_________________________________________________________________
      //______________________________________________________________________________
      if (pathArr_curr.getFirst() != point_Begin_currSegment) { continue; }
      if (pathArr_curr.getLast() != point_End_currSegment) { continue; }

      arr_pathArr_AllValidPaths.add(pathArr_curr); //_______________________________________________________________
    }

    //__________________
    LinkedList<Point> pathArr_ShortestPath = get_ShortestPath(arr_pathArr_AllValidPaths);
    return pathArr_ShortestPath;
  }

  //_____

  //_________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

  //______________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
  //____________________________________
  //___________________________________
  //___________________________________________
  //__________________________________________
  //_____________________________________________________
  @Main
  private Triple<Shape, Shape, LinkedList<Point>> get_BoundShapeUnion_and_Vertex(UnitObj unitObj_AA, UnitObj unitObj_BB, double distance_FromSelfCenter) {
    //__________________________
    //____________________________________________________________________________________________________________________________________________
    //________________________________________________________________________________________
    //________________________________
    //________________________________________________________________________________________
    LinkedList<BoundingBox> arr_bounds_AllBlockNear_AdjLv1 = new LinkedList<>();
    LinkedList<BoundingBox> arr_bounds_AllBlockNear_AdjLv2 = new LinkedList<>();
    LinkedList<Point> arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin_HaveIntersect = new LinkedList<>();
    Shape boundsShapeUnion_AllBlockNear_AdjLv1_curr = null;
    Shape boundsShapeUnion_AllBlockNear_AdjLv2_curr = null;

    @Messy
    HashSet<UnitObj> gp_unitObj_ToExclude = new HashSet<UnitObj>();
    for (UnitObj unitObj_curr : windowSession_corr.collisionManager.gp_unitObj) {
      if (TrafficLight.class.isAssignableFrom(unitObj_curr.getClass())) {
        gp_unitObj_ToExclude.add(unitObj_curr); //
      }
    }

    //_____
    final HashSet<UnitObj> gp_unitObj_BlockNear = get_NearbyBlocksEsNearByToo(unitObj_BB, CollisionManager.collusionDistanceThresholdLv2 + distance_FromSelfCenter, unitObj_AA, true, gp_unitObj_ToExclude); //_______________________________________
    for (UnitObj unitObj_BlockNear_curr : gp_unitObj_BlockNear) {
      Bounds bounds_BlockNear = unitObj_BlockNear_curr.node_underlying.getBoundsInParent();

      //_____
      final BoundingBox bounds_BlockNear_AdjLv1_curr = CollisionManager.get_BoundingBox_WithThreshold(bounds_BlockNear, CollisionManager.collusionDistanceThreshold);
      final BoundingBox bounds_BlockNear_AdjLv2_curr = CollisionManager.get_BoundingBox_WithThreshold(bounds_BlockNear, CollisionManager.collusionDistanceThresholdLv2);
      arr_bounds_AllBlockNear_AdjLv1.add(bounds_BlockNear_AdjLv1_curr);
      arr_bounds_AllBlockNear_AdjLv2.add(bounds_BlockNear_AdjLv2_curr);
      //_____
      boundsShapeUnion_AllBlockNear_AdjLv1_curr = union_CurrBoundsShape(bounds_BlockNear_AdjLv1_curr, boundsShapeUnion_AllBlockNear_AdjLv1_curr);
      boundsShapeUnion_AllBlockNear_AdjLv2_curr = union_CurrBoundsShape(bounds_BlockNear_AdjLv2_curr, boundsShapeUnion_AllBlockNear_AdjLv2_curr);
      //_____
      final BoundingBox bounds_BlockNear_AdjLv2_SelfCenterMargin = CollisionManager.get_BoundingBox_WithThreshold(bounds_BlockNear, CollisionManager.collusionDistanceThresholdLv2 + distance_FromSelfCenter);
      LinkedList<Point> arr_vertex_BlockNear_AdjLv2_SelfCenterMargin_curr = JavafxUtil.get_VerticesFromBoundingBox(bounds_BlockNear_AdjLv2_SelfCenterMargin);
      arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin_HaveIntersect.addAll(arr_vertex_BlockNear_AdjLv2_SelfCenterMargin_curr);
    }
    final Shape boundsShapeUnion_AllBlockNear_AdjLv1 = boundsShapeUnion_AllBlockNear_AdjLv1_curr;
    boundsShapeUnion_AllBlockNear_AdjLv1.setFill(JavafxUtil.color_Red_light);
    //____________________________________________________________________________________________________________________________________________
    final Shape boundsShapeUnion_AllBlockNear_AdjLv2 = boundsShapeUnion_AllBlockNear_AdjLv2_curr;
    boundsShapeUnion_AllBlockNear_AdjLv2.setFill(JavafxUtil.color_Yellow_dim);

    //_____
    LinkedList<Point> arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin = new LinkedList<>();
    for (Point vertex_AllBlockNear_AdjLv2_SelfCenterMargin_curr : arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin_HaveIntersect) {
      //_____________________________________________________________________________________________________________________________________________________________________________
      //__________________________________________________________________________________________________________________
      //_______
      //___________________________________________________________________________________________
      //_______________________________
      boolean det_Contains_direct = boundsShapeUnion_AllBlockNear_AdjLv2.contains(vertex_AllBlockNear_AdjLv2_SelfCenterMargin_curr.getX(), vertex_AllBlockNear_AdjLv2_SelfCenterMargin_curr.getY());
      Point2D pt = boundsShapeUnion_AllBlockNear_AdjLv2.parentToLocal(vertex_AllBlockNear_AdjLv2_SelfCenterMargin_curr.getX(), vertex_AllBlockNear_AdjLv2_SelfCenterMargin_curr.getY());
      boolean det_Contains_CoordinateConverted = boundsShapeUnion_AllBlockNear_AdjLv2.contains(pt.getX(), pt.getY());
      if (!det_Contains_CoordinateConverted) {
        arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin.add(vertex_AllBlockNear_AdjLv2_SelfCenterMargin_curr); //_
      }
      if (det_Contains_direct != det_Contains_CoordinateConverted) { System.err.println("Diff, but why both works .. which one should i use..."); }
    }

    return new ImmutableTriple<>(boundsShapeUnion_AllBlockNear_AdjLv1, boundsShapeUnion_AllBlockNear_AdjLv2, arr_vertex_AllBlockNear_AdjLv2_SelfCenterMargin);
  }

  private Shape union_CurrBoundsShape(Bounds bounds_BlockNear_AdjLv2_curr, Shape boundsShapeUnion_AllBlockNear_AdjLv2_curr) {
    Rectangle boundsRectangle_curr = JavafxUtil.convert_Bounds2Rectangle(bounds_BlockNear_AdjLv2_curr);
    addTo_PathingDebugShape(boundsRectangle_curr);
    boundsRectangle_curr.setFill(JavafxUtil.color_Teal);
    if (boundsShapeUnion_AllBlockNear_AdjLv2_curr == null) {
      boundsShapeUnion_AllBlockNear_AdjLv2_curr = boundsRectangle_curr;
    }
    else {
      Shape boundsShape_union_prev = boundsShapeUnion_AllBlockNear_AdjLv2_curr;
      boundsShapeUnion_AllBlockNear_AdjLv2_curr = Shape.union(boundsShapeUnion_AllBlockNear_AdjLv2_curr, boundsRectangle_curr);
      removeFrom_PathingDebugShape(boundsShape_union_prev);
      removeFrom_PathingDebugShape(boundsRectangle_curr); //____________________________________________________________________________________________________
      JavafxUtil.set_RelativeOffsetInNode(boundsShapeUnion_AllBlockNear_AdjLv2_curr, windowSession_corr.panel_SemanticRoot);
      addTo_PathingDebugShape(boundsShapeUnion_AllBlockNear_AdjLv2_curr); //_______________________________________________________________________________
    }

    return boundsShapeUnion_AllBlockNear_AdjLv2_curr;
  }

  @Debug
  public Shape get_BoundsShapeUnion_OfAllUnitObj_AdjLv2Scm_debug(double distance_FromSelfCenter) {
    Shape boundsShapeUnion_AllBlockNear_AdjLv2_curr = null;

    //_____
    final HashSet<UnitObj> gp_unitObj = windowSession_corr.collisionManager.gp_unitObj;
    for (UnitObj unitObj_BlockNear_curr : gp_unitObj) {
      Bounds bounds_BlockNear = unitObj_BlockNear_curr.node_underlying.getBoundsInParent();
      final BoundingBox bounds_BlockNear_AdjLv2_SelfCenterMargin = CollisionManager.get_BoundingBox_WithThreshold(bounds_BlockNear, CollisionManager.collusionDistanceThresholdLv2 + distance_FromSelfCenter);
      boundsShapeUnion_AllBlockNear_AdjLv2_curr = union_CurrBoundsShape(bounds_BlockNear_AdjLv2_SelfCenterMargin, boundsShapeUnion_AllBlockNear_AdjLv2_curr);     //_____________________
    }
    boundsShapeUnion_AllBlockNear_AdjLv2_curr.setFill(Color.RED.deriveColor(0, 1, 1, 0.05));
    return boundsShapeUnion_AllBlockNear_AdjLv2_curr;
  }

  //_____

  public static boolean det_ShapeIntersect(Shape shape_AA, Shape shape_BB) {
    Shape shape = Shape.intersect(shape_AA, shape_BB); //____________________________________________________________________________________________________________________________________________________________________________________________________________________________________
    boolean det_Intersect = shape.getBoundsInLocal().getWidth() != -1;
    return det_Intersect;
  }

  //_____

  private LinkedList<Point> get_ShortestPath(LinkedList<LinkedList<Point>> arr_pathArr_AllValidPaths) {
    if (arr_pathArr_AllValidPaths == null || arr_pathArr_AllValidPaths.isEmpty()) { throw new Error("Empty"); }
    if (arr_pathArr_AllValidPaths.size() == 1) { return arr_pathArr_AllValidPaths.getFirst(); }

    LinkedList<Path> arr_path_NoIntersect = new LinkedList<>();
    LinkedList<Point> pathArr_ShortestPath_overall = null;
    Path pathJfx_ShortestPath_overall = null;
    Double length_Shortest = null;
    for (LinkedList<Point> pathArr_curr : arr_pathArr_AllValidPaths) {
      //____
      //____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

      if (pathArr_curr == null || pathArr_curr.isEmpty()) { throw new Error("Empty"); }
      if (pathArr_curr.size() == 1) { System.err.println("Are you sure? A path with only 1 point?"); }

      Path pathJfx_curr = convert_arrPath2pathJfx(pathArr_curr);
      arr_path_NoIntersect.add(pathJfx_curr);

      //_____
      double length_curr = JavafxUtil.calc_LengthOfArrPath(pathArr_curr);
      if (length_Shortest == null || length_curr < length_Shortest) {
        pathArr_ShortestPath_overall = pathArr_curr;
        length_Shortest              = length_curr;
        pathJfx_ShortestPath_overall = pathJfx_curr;
      }
      else if (length_curr == length_Shortest) {
        //___________________________________________________________________________
        System.out.println("[Debug 50%] When_Equal :: " + pathArr_curr + " :: " + pathArr_ShortestPath_overall);
        //____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
      }
      else {
        //_____________
      }
    }
    addTo_PathingDebugShape(pathJfx_ShortestPath_overall);
    visualize_arrPath_with_TextCoordinate(pathArr_ShortestPath_overall, JavafxUtil.color_Grey, 0);
    return pathArr_ShortestPath_overall;
  }

  //_________

  public Path convert_arrPath2pathJfx(LinkedList<Point> pathArr) {
    Path path = new Path();
    path.setStrokeWidth(1.0);
    path.setStroke(JavafxUtil.color_Blue);

    ObservableList<PathElement> pele = path.getElements();
    int j = 0;
    for (Point point_Goto_curr : pathArr) {
      j++;
      if (j == 1) {
        pele.add(new MoveTo(point_Goto_curr.getX(), point_Goto_curr.getY()));
      }
      else {
        pele.add(new LineTo(point_Goto_curr.getX(), point_Goto_curr.getY()));
      }
    }

    return path;
  }

  public void visualize_arrPath_with_TextCoordinate(LinkedList<Point> pathArr, Paint paint, double offset) {
    int j = 0;
    for (Point point_Goto_curr : pathArr) {
      j++;
      Text text = new Text("" + j);
      addTo_PathingDebugShape(text);
      text.setLayoutX(point_Goto_curr.getX() + offset + Math.random() * 2);
      text.setLayoutY(point_Goto_curr.getY() + offset + Math.random() * 2);
      text.setStroke(paint);
    }
  }

  //_____________

  private HashSet<UnitObj> get_NearbyBlocks(final UnitObj unitObj_BB, final double gap_CountAsNearby_biDir_threshold, final UnitObj unitObj_AA_ToExclude, final boolean det_IncludeSelf, final Set<UnitObj> gp_unitObj_ToExclude) {
    final HashSet<UnitObj> gp_BlockNear = new HashSet<UnitObj>();
    if (det_IncludeSelf) { gp_BlockNear.add(unitObj_BB); }
    final HashSet<UnitObj> gp_unitObj = windowSession_corr.collisionManager.gp_unitObj;
    int totalUnitObj = gp_unitObj.size();
    int sn_Recursion = 0;
    get_NearbyBlocksEsNearByToo_recursive(unitObj_BB, gap_CountAsNearby_biDir_threshold, unitObj_AA_ToExclude, false, gp_BlockNear, gp_unitObj, sn_Recursion, totalUnitObj, false, gp_unitObj_ToExclude);
    return gp_BlockNear;
  }

  private HashSet<UnitObj> get_NearbyBlocksEsNearByToo(final UnitObj unitObj_BB, final double gap_CountAsNearby_biDir_threshold, final UnitObj unitObj_AA_ToExclude, final boolean det_IncludeSelf, final Set<UnitObj> gp_unitObj_ToExclude) {
    final HashSet<UnitObj> gp_BlockNear = new HashSet<UnitObj>();
    if (det_IncludeSelf) { gp_BlockNear.add(unitObj_BB); }
    final HashSet<UnitObj> gp_unitObj = windowSession_corr.collisionManager.gp_unitObj;
    int totalUnitObj = gp_unitObj.size();
    int sn_Recursion = 0;
    get_NearbyBlocksEsNearByToo_recursive(unitObj_BB, gap_CountAsNearby_biDir_threshold, unitObj_AA_ToExclude, false, gp_BlockNear, gp_unitObj, sn_Recursion, totalUnitObj, true, gp_unitObj_ToExclude);
    return gp_BlockNear;
  }

  @Main
  private void get_NearbyBlocksEsNearByToo_recursive(final UnitObj unitObj_BB, final double gap_CountAsNearby_biDir_threshold, final UnitObj unitObj_AA_ToExclude, final boolean det_IncludeSelf,
                                                     final HashSet<UnitObj> gp_BlockNear, final HashSet<UnitObj> gp_unitObj, int sn_Recursion, final int totalUnitObj,
                                                     final boolean det_Recursive,
                                                     final Set<UnitObj> gp_unitObj_ToExclude) {
    sn_Recursion++;
    if (sn_Recursion > totalUnitObj) { throw new Error("Inf Recursion? Or dynamically adding new Unitobj? :: sn_Recursion > totalUnitObj" + sn_Recursion); }

    final Bounds bounds_BB = unitObj_BB.node_underlying.getBoundsInParent();
    final BoundingBox bounds_BB_AdjLv2 = CollisionManager.get_BoundingBox_WithThreshold(bounds_BB, gap_CountAsNearby_biDir_threshold);

    for (UnitObj unitObj_curr : gp_unitObj) {
      if (gp_unitObj_ToExclude.contains(unitObj_curr) || unitObj_curr == unitObj_AA_ToExclude) {
        //_____________
      }
      else if (unitObj_curr == unitObj_BB && det_IncludeSelf) {
        gp_BlockNear.add(unitObj_curr); //_____________
      }
      else if (gp_BlockNear.contains(unitObj_curr)) { //__________________________________________________
        //_____________________________________________________
      }
      else {
        final Bounds bounds_curr = unitObj_curr.node_underlying.getBoundsInParent();
        final BoundingBox bounds_curr_AdjLv2 = CollisionManager.get_BoundingBox_WithThreshold(bounds_curr, gap_CountAsNearby_biDir_threshold);
        if (bounds_BB_AdjLv2.intersects(bounds_curr_AdjLv2)) {
          gp_BlockNear.add(unitObj_curr);
          if (det_Recursive) {
            get_NearbyBlocksEsNearByToo_recursive(unitObj_curr, gap_CountAsNearby_biDir_threshold, unitObj_AA_ToExclude, false, gp_BlockNear, gp_unitObj, sn_Recursion, totalUnitObj, det_Recursive, gp_unitObj_ToExclude); //_
          }
        }
      }
    }
  }

  //__________________________

  @Deprecated
  private LinkedList<Point> get_Points_AtCloserDirection_ByAreaDet(Bounds bounds, Point point_begin, Point point_end, WindowSession windowSession_corr) {
    //____
    final double posX_Begin_currSegment = point_begin.getX();
    final double posY_Begin_currSegment = -point_begin.getY();
    final double posX_End_currSegment = point_end.getX();
    final double posY_End_currSegment = -point_end.getY();

    //____________________________________
    //__________________________________________________________________________________
    final double displacementX = posX_End_currSegment - posX_Begin_currSegment;
    final double displacementY = posY_End_currSegment - posY_Begin_currSegment;
    final double m = displacementY / displacementX; //_________________________________________________________________________________________________________
    final double b = posY_Begin_currSegment - posX_Begin_currSegment * m;
    if (m == 0 || Double.isNaN(m)) { throw new Error("//TODO" + m); }
    //_____________________________________________

    System.out.println(String.format("%f = %f * %f + %f", posY_Begin_currSegment, m, posX_Begin_currSegment, b));
    System.out.println(String.format("%f = %f * %f + %f", posY_End_currSegment, m, posX_End_currSegment, b));

    final double radius_ori = 5;
    final double opacity = 0.5;

    Circle circle_Begin = new Circle();
    addTo_PathingDebugShape(circle_Begin);
    circle_Begin.setRadius(radius_ori);
    circle_Begin.setFill(Color.rgb(80, 255, 255, opacity));
    circle_Begin.setCenterX(posX_Begin_currSegment);
    circle_Begin.setCenterY(-posY_Begin_currSegment);
    //_________________________________________________________________________________________
    //________________________________________________________________________
    //__________________________________________________
    //___________________________________________________

    Circle circle_End = new Circle();
    addTo_PathingDebugShape(circle_End);
    circle_End.setRadius(radius_ori);
    circle_End.setFill(Color.rgb(255, 80, 255, opacity));
    circle_End.setCenterX(posX_End_currSegment);
    circle_End.setCenterY(-posY_End_currSegment);
    //___________________________________________________________________________________
    //______________________________________________________________________
    //______________________________________________
    //_______________________________________________

    //______________________________________
    //______________________________________
    //______________________________________
    //______________________________________

    double posXMin = bounds.getMinX();
    double posYMin = -bounds.getMaxY();
    double posXMax = bounds.getMaxX();
    double posYMax = -bounds.getMinY(); //______________
    //___________________________________________________________________

    double width = bounds.getWidth();
    double height = bounds.getHeight();
    double area_Bound_half = width * height / 2.0;

    double posY_Itc_XMin = (posXMin * m + b);
    double posY_Itc_XMax = (posXMax * m + b);
    double posX_Itc_YMin = (posYMin - b) / m;
    double posX_Itc_YMax = (posYMax - b) / m;

    //_________________________________________________________
    //_________________________________________________________
    //_________________________________________________________
    //_________________________________________________________
    //_________________________

    //____
    int bit = 0b0;
    int count = 0;

    //___________________________________________________
    //________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
    //______________________
    final LinkedList<Tup4<Point, Integer, Double, Double>> arr_point_Itc_InBound = new LinkedList<>();

    Circle circle1 = new Circle();
    addTo_PathingDebugShape(circle1);
    System.out.println(posYMin + " :: " + posY_Itc_XMin + " :: " + posY_Itc_XMin + " :: " + posYMax);
    if (posYMin <= posY_Itc_XMin && posY_Itc_XMin <= posYMax) { //________
      double lengthY_Upper_XMin = Math.abs(posYMax - posY_Itc_XMin);
      double lengthY_Lower_XMin = Math.abs(posY_Itc_XMin - posYMin);
      //________________________________________________________________________________________________________
      //________________________________________________________________________________________________________
      arr_point_Itc_InBound.addLast(new Tup4<>(new Point(posXMin, posY_Itc_XMin), 0b1, lengthY_Upper_XMin, lengthY_Lower_XMin));
      bit = bit | 0b1;
      count++;

      circle1.setRadius(radius_ori);
      circle1.setFill(Color.rgb(255, 80, 80, opacity));
      circle1.setCenterX(posXMin);
      circle1.setCenterY(-posY_Itc_XMin);
    }
    else {
      //________________________________________________________________________________
      arr_point_Itc_InBound.addLast(null); //____________________________________________
    }

    Circle circle2 = new Circle();
    addTo_PathingDebugShape(circle2);
    System.out.println(posYMin + " :: " + posY_Itc_XMax + " :: " + posY_Itc_XMax + " :: " + posYMax);
    if (posYMin <= posY_Itc_XMax && posY_Itc_XMax <= posYMax) { //________
      double lengthY_Upper_XMax = Math.abs(posYMax - posY_Itc_XMax);
      double lengthY_Lower_XMax = Math.abs(posY_Itc_XMax - posYMin);
      //________________________________________________________________________________________________________
      //________________________________________________________________________________________________________
      arr_point_Itc_InBound.addLast(new Tup4<>(new Point(posXMax, posY_Itc_XMax), 0b10, lengthY_Upper_XMax, lengthY_Lower_XMax));
      bit = bit | 0b10;
      count++;

      circle2.setRadius(radius_ori);
      circle2.setFill(Color.rgb(80, 255, 80, opacity));
      circle2.setCenterX(posXMax);
      circle2.setCenterY(-posY_Itc_XMax);
    }
    else {
      arr_point_Itc_InBound.addLast(null);
    }

    Circle circle3 = new Circle();
    addTo_PathingDebugShape(circle3);
    System.out.println(posXMin + " :: " + posX_Itc_YMin + " :: " + posX_Itc_YMin + " :: " + posXMax);
    if (posXMin <= posX_Itc_YMin && posX_Itc_YMin <= posXMax) { //________
      double lengthX_Left_YMin = Math.abs(posX_Itc_YMin - posXMin);
      double lengthX_Right_YMin = Math.abs(posXMax - posX_Itc_YMin);
      arr_point_Itc_InBound.addLast(new Tup4<>(new Point(posX_Itc_YMin, posYMin), 0b100, lengthX_Left_YMin, lengthX_Right_YMin));
      bit = bit | 0b100;
      count++;

      circle3.setRadius(radius_ori);
      circle3.setFill(Color.rgb(80, 80, 255, opacity));
      circle3.setCenterX(posX_Itc_YMin);
      circle3.setCenterY(-posYMin);
    }
    else {
      arr_point_Itc_InBound.addLast(null);
    }

    Circle circle4 = new Circle();
    addTo_PathingDebugShape(circle4);
    System.out.println(posXMin + " :: " + posX_Itc_YMax + " :: " + posX_Itc_YMax + " :: " + posXMax);
    if (posXMin <= posX_Itc_YMax && posX_Itc_YMax <= posXMax) { //________
      double lengthX_Left_YMax = Math.abs(posX_Itc_YMax - posXMin);
      double lengthX_Right_YMax = Math.abs(posXMax - posX_Itc_YMax);
      arr_point_Itc_InBound.addLast(new Tup4<>(new Point(posX_Itc_YMax, posYMax), 0b1000, lengthX_Left_YMax, lengthX_Right_YMax));
      bit = bit | 0b1000;
      count++;

      circle4.setRadius(radius_ori);
      circle4.setFill(Color.rgb(255, 255, 80, opacity));
      circle4.setCenterX(posX_Itc_YMax);
      circle4.setCenterY(-posYMax);
    }
    else {
      arr_point_Itc_InBound.addLast(null);
    }
    if (count != 2) { throw new Error("Sth is wrong with the size //TODO when equality  :: " + arr_point_Itc_InBound + " :: " + count); }

    //____
    final LinkedList<Point> arr_vertex = JavafxUtil.get_VerticesFromBoundingBox(bounds);
    final LinkedList<Point> arr_Point_GoTo = new LinkedList<Point>();

    //____________________________________________________________________

    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________
    //________________________________________

    System.out.println(Integer.toBinaryString(bit) + " :: " + String.format("y = %f * x + %f", m, b));
    System.out.println(arr_point_Itc_InBound);

    if ((bit ^ 0b11) == 0) { //_________________________
      double lengthY_Upper_XMin = arr_point_Itc_InBound.get(0).i3;
      double lengthY_Upper_XMax = arr_point_Itc_InBound.get(1).i3;
      //_____________________________________________________________________________________________

      double area_Trapezoid = (lengthY_Upper_XMin + lengthY_Upper_XMax) * width / 2.0;

      //______________________________________
      //_______________________________________________________________
      if (area_Trapezoid < area_Bound_half) {
        //_____________________________________________________________________
        if (displacementX < 0) { //_____________________________________________________
          //___________________________________
          arr_Point_GoTo.addFirst(arr_vertex.get(0));
          arr_Point_GoTo.addFirst(arr_vertex.get(2)); //____________________________________________________________________________________________________________________________________________________________________________________________________________________
          //_____________________________________________________
          //_____________________________________________________________
        }
        else {
          arr_Point_GoTo.addFirst(arr_vertex.get(2));
          arr_Point_GoTo.addFirst(arr_vertex.get(0));
        }
      }
      else {
        if (displacementX < 0) {
          arr_Point_GoTo.addFirst(arr_vertex.get(1));
          arr_Point_GoTo.addFirst(arr_vertex.get(3));
        }
        else {
          arr_Point_GoTo.addFirst(arr_vertex.get(3));
          arr_Point_GoTo.addFirst(arr_vertex.get(1));
        }
      }
    }
    else if ((bit ^ 0b101) == 0) { //_________________________
      arr_Point_GoTo.addFirst(arr_vertex.get(1)); //____________________________________________________________________________________________________________
    }
    else if ((bit ^ 0b1001) == 0) { //_________________________
      arr_Point_GoTo.addFirst(arr_vertex.get(0));
    }
    else if ((bit ^ 0b110) == 0) { //_________________________
      arr_Point_GoTo.addFirst(arr_vertex.get(3));
    }
    else if ((bit ^ 0b1010) == 0) { //_________________________
      arr_Point_GoTo.addFirst(arr_vertex.get(2));
    }
    else if ((bit ^ 0b1100) == 0) { //_________________________
      double lengthX_Left_YMin = arr_point_Itc_InBound.get(2).i3;
      double lengthX_Left_YMax = arr_point_Itc_InBound.get(3).i3;

      double area_Trapezoid = (lengthX_Left_YMin + lengthX_Left_YMax) * height / 2.0;

      if (area_Trapezoid < area_Bound_half) {
        if (displacementY < 0) {
          arr_Point_GoTo.addFirst(arr_vertex.get(1));
          arr_Point_GoTo.addFirst(arr_vertex.get(0));
        }
        else {
          arr_Point_GoTo.addFirst(arr_vertex.get(0));
          arr_Point_GoTo.addFirst(arr_vertex.get(1));
        }
      }
      else {
        if (displacementY < 0) {
          arr_Point_GoTo.addFirst(arr_vertex.get(3)); //___________________________________
          arr_Point_GoTo.addFirst(arr_vertex.get(2));
        }
        else {
          arr_Point_GoTo.addFirst(arr_vertex.get(2));
          arr_Point_GoTo.addFirst(arr_vertex.get(3));
        }
      }
    }

    int i = 0;
    for (Point point_GoTo_curr : arr_Point_GoTo) {
      i++;
      Circle circle_GoTo = new Circle();
      addTo_PathingDebugShape(circle_GoTo);
      circle_GoTo.setRadius(5);
      circle_GoTo.setFill(Color.rgb(128 - i * 60, i * 60, i * 60, opacity));
      circle_GoTo.setCenterX(point_GoTo_curr.getX());
      circle_GoTo.setCenterY(point_GoTo_curr.getY());

      Text text_GoTo = new Text("" + i);
      addTo_PathingDebugShape(text_GoTo);
      text_GoTo.setLayoutX(point_GoTo_curr.getX());
      text_GoTo.setLayoutY(point_GoTo_curr.getY());
    }

    return arr_Point_GoTo;
  }

}
