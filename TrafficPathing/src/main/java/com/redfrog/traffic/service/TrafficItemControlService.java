package com.redfrog.traffic.service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.redfrog.traffic.collision.CollisionManager;
import com.redfrog.traffic.model.UnitObj;
import com.redfrog.traffic.model.Widget;
import com.redfrog.traffic.pathing.PathingUtil.GotoMode;
import com.redfrog.traffic.session.WindowSession;
import com.redfrog.traffic.trafficLight.TrafficLight;
import com.redfrog.traffic.util.JavafxUtil;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

@Service
public class TrafficItemControlService {

  private final WindowSession windowSession_corr;

  @Autowired
  public TrafficItemControlService(WindowSession windowSession_corr) {
    this.windowSession_corr = windowSession_corr;

  }

  //_________________

  private Widget createCollider(double radius, Paint paint_Fill) {
    final Widget collider = Widget.init();
    collider.node_underlying.setBackground(Background.EMPTY);

    final Circle circle = new Circle();
    collider.node_underlying.getChildren().add(circle);
    circle.setRadius(radius);
    circle.setFill(paint_Fill);
    collider.shape            = circle;

    //_________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

    //______________________________________________________________________
    //________________
    //___
    //___________________________________
    //_
    //______
    //_____________________
    //_________________________________________________
    //____________________________________________________________________________________
    //___________________________________________________________________
    //________________________________________________________________________________________________________________________________________________________________________________

    collider.collisionHandler = (unitObj_AA, unitObj_BB) -> {
      System.out.println("Testing collide, not a true handler, assign true handler later");
    };
    windowSession_corr.collisionManager.register(collider);
    return collider;
  }

  public Widget createCollider_addToPane(double radius, double posX_Center, double posY_Center, Pane pane_parent, Paint paint) {
    Widget collider = createCollider(radius, paint);
    pane_parent.getChildren().add(collider.node_underlying);
    collider.node_underlying.setLayoutX(posX_Center);
    collider.node_underlying.setLayoutY(posY_Center);
    return collider;
  }

  //______________________________

  private Widget createBlock(double width, double height, Paint paint_Fill) {
    //____
    final Widget block = Widget.init();
    block.node_underlying.setBackground(Background.EMPTY);

    final Rectangle rectangle = new Rectangle();
    block.node_underlying.getChildren().add(rectangle);
    rectangle.setWidth(width);
    rectangle.setHeight(height);
    rectangle.setFill(paint_Fill);
    block.shape            = rectangle;

    //______________________________________________________________________________________________________________________
    block.collisionHandler = CollisionManager.NullCollisionHandler;
    windowSession_corr.collisionManager.register(block);
    //_____________________________________________________________________________________
    //_____________________________________________________________________

    return block;
  }

  public Widget createBlock_addToPane(double width, double height, double posX_Begin, double posY_Begin, Pane pane_parent, Paint paint_Fill) {
    Widget block = createBlock(width, height, paint_Fill);
    pane_parent.getChildren().add(block.node_underlying);
    block.node_underlying.setLayoutX(posX_Begin);
    block.node_underlying.setLayoutY(posY_Begin);
    return block;
  }

  //_____________

  //_________________________________________________________

  private static final double radius_ori = 13;
  private static final double opacity_ori = 0.5;

  public LinkedList<UnitObj> createBlock_demo_Simple() {
    LinkedList<UnitObj> arr_Collider = new LinkedList<>();

    Widget collider_M = createCollider_addToPane(radius_ori, 450, 450, windowSession_corr.panel_SemanticRoot, Color.rgb(191, 86, 255, opacity_ori));
    collider_M.node_underlying.setViewOrder(-1);
    windowSession_corr.selectStatus.add(collider_M);
    collider_M.name = "M";
    arr_Collider.add(collider_M);

    createBlock_addToPane(50, 300, 300, 200, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    createBlock_addToPane(150, 300, 500, 100, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);

    return arr_Collider;
  }

  public LinkedList<UnitObj> createBlock_demo_CrossBlock() {
    LinkedList<UnitObj> arr_Collider = new LinkedList<>();

    Widget collider_M = createCollider_addToPane(radius_ori, 450, 450, windowSession_corr.panel_SemanticRoot, Color.rgb(191, 86, 255, opacity_ori));
    collider_M.node_underlying.setViewOrder(-1);
    windowSession_corr.selectStatus.add(collider_M);
    collider_M.name = "M";
    arr_Collider.add(collider_M);

    createBlock_addToPane(500, 50, 150, 200, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    createBlock_addToPane(50, 500, 500, 100, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    createBlock_addToPane(150, 20, 250, 500, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);

    return arr_Collider;
  }

  public LinkedList<UnitObj> createBlock_demo_CrossBlock_Complex() {
    LinkedList<UnitObj> arr_Collider = new LinkedList<>();

    Widget collider_M = createCollider_addToPane(radius_ori, 450, 450, windowSession_corr.panel_SemanticRoot, Color.rgb(191, 86, 255, opacity_ori));
    collider_M.node_underlying.setViewOrder(-1);
    windowSession_corr.selectStatus.add(collider_M);
    collider_M.name = "M";
    arr_Collider.add(collider_M);

    createBlock_addToPane(700, 50, 150, 200, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    createBlock_addToPane(50, 500, 700, 100, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    createBlock_addToPane(50, 600, 200, 50, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    createBlock_addToPane(400, 50, 50, 500, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    createBlock_addToPane(180, 50, 550, 410, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);

    Widget block = createBlock_addToPane(150, 20, 350, 400, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    windowSession_corr.javafxUtil.make_Draggable(block.node_underlying);

    return arr_Collider;
  }

  //_____

  //______________________________________________________________
  //___________________________________________________________________________________
  //_____________________________________________________________________________________________
  //__________________________________________________________________________________________________________________________
  //___
  //____________________________________________________________________________________________________________________________________________________________
  //___
  //________________________________________________________________________________________________
  //______________________________________________________________________________________________________
  //__________________________________________________________________________________________________
  //____________________________________________________________________________________________________________________________________________________

  private HashSet<ExecutorService> gp_executor_SimpleShutdown = new HashSet<>();

  public Widget createBlock_RandomMoving(double width, double height, double speed) {
    Widget blocker_Moving = createBlock_addToPane(width, height, 0, 0, windowSession_corr.panel_SemanticRoot, Color.rgb(128, 0, 0, opacity_ori));

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("thd-RandomMoving-%d").build());
    executor.scheduleWithFixedDelay(() -> {
      Platform.runLater(() -> {
        windowSession_corr.pathingUtil.goTo_TargetLocations_NoRepathCollideHandler_debug(blocker_Moving, Math.random() * 500, Math.random() * 500, speed); //_
      });
    }, 0, 1000, TimeUnit.MILLISECONDS);
    gp_executor_SimpleShutdown.add(executor);
    return blocker_Moving;
  }

  public Widget createBlock_RegularMoving(double width, double height, double posX_Begin, double posY_Begin, double posX_End, double posY_End, double speed, long delay) {
    Widget blocker_Moving = createBlock_addToPane(width, height, posX_Begin, posY_Begin, windowSession_corr.panel_SemanticRoot, Color.rgb(128, 0, 0, opacity_ori));

    //_____________________________________________
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("thd-RandomMoving-%d").build());
    executor.scheduleWithFixedDelay(new Runnable()
      {
        boolean toggle = false;

        @Override
        public void run() {
          //____________________________________________________
          Platform.runLater(() -> {
            if (!toggle) {
              windowSession_corr.pathingUtil.goTo_TargetLocations_NoRepathCollideHandler_debug(blocker_Moving, posX_Begin, posY_Begin, speed);
            }
            else {
              windowSession_corr.pathingUtil.goTo_TargetLocations_NoRepathCollideHandler_debug(blocker_Moving, posX_End, posY_End, speed);
            }
            toggle = !toggle;
          });
        }

      }, 0, delay, TimeUnit.MILLISECONDS);
    gp_executor_SimpleShutdown.add(executor);
    return blocker_Moving;
  }

  //_____

  public void gotoRandomLocations(UnitObj unitObj, double speed) {
    Shape boundsShapeUnion = windowSession_corr.pathingUtil.get_BoundsShapeUnion_OfAllUnitObj_AdjLv2Scm_debug(windowSession_corr.pathingUtil.get_distance_FromSelfCenter_messy(unitObj));
    if (boundsShapeUnion.getParent() != windowSession_corr.panel_SemanticRoot) { throw new Error(); }
    int i = 0;
    int j = 0;
    while (true) {
      i++;
      if (i == 500) { break; }
      double posX = Math.random() * 800;
      double posY = Math.random() * 500;
      if (!boundsShapeUnion.contains(posX, posY)) { //___________________________________________________________
        //__________________________________________________________________________________
        j++;
        if (j == 10) { break; }
        //___________________________________________________________________________________________
        //_______________________________________________________________________________________________________________________________________________________________________________________________________
        //___
        //___________________________________________________________________
        //___________________________________________
        //____________________________________________________________________
        //____________________________
        //________________________________________________________
        //_________________________________________________________________________
        //_________________________________________________________________________

        Point2D pt = boundsShapeUnion.localToParent(posX, posY);
        windowSession_corr.pathingUtil.goTo_TargetLocation(unitObj, pt.getX(), pt.getY(), speed, GotoMode.Last_AfterAllPrevGotoTargets);

        //___________________________________________
        //_______________________________________________________________________
        //___________________________________
        //____________________________
        //_______________________________________________________
        //________________________________
        //________________________________
        //
        //__________________________________________________________________________________________________________________________
      }
    }

  }

  //_____

  private void createBlock_demo_AlignedCube(int amount_col, int amount_row, double width, double height, double hGap, double vGap, double hInitOffset, double vInitOffset) {
    int sn_row = 0;
    double posY = 0 - (height + vGap) + vInitOffset;
    while (sn_row != amount_row) {
      sn_row++;
      posY += (height + vGap);

      int sn_col = 0;
      double posX = 0 - (width + hGap) + hInitOffset;
      while (sn_col != amount_col) {
        sn_col++;
        posX += width + hGap;

        createBlock_addToPane(width, height, posX, posY, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
      }
    }
  }

  public LinkedList<UnitObj> createBlock_demo_AlignedCube_Simple(int amount_col, int amount_row, double width, double height, double hGap, double vGap, double hInitOffset, double vInitOffset) {
    LinkedList<UnitObj> arr_Collider = new LinkedList<>();

    Widget collider_M = createCollider_addToPane(radius_ori, 450, 450, windowSession_corr.panel_SemanticRoot, Color.rgb(191, 86, 255, opacity_ori));
    //___________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
    collider_M.node_underlying.setViewOrder(-1);
    //______________________________________________
    windowSession_corr.selectStatus.add(collider_M);
    collider_M.name = "M";
    arr_Collider.add(collider_M);

    createBlock_demo_AlignedCube(amount_col, amount_row, width, height, hGap, vGap, hInitOffset, vInitOffset);

    return arr_Collider;
  }

  public LinkedList<UnitObj> createBlock_demo_AlignedCube_ComplexMoving(int amount_col, int amount_row, double width, double height, double hGap, double vGap, double hInitOffset, double vInitOffset) {
    LinkedList<UnitObj> arr_Collider = new LinkedList<>();

    Widget collider_M = createCollider_addToPane(radius_ori, 450, 450, windowSession_corr.panel_SemanticRoot, Color.rgb(191, 86, 255, opacity_ori));
    //___________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
    collider_M.node_underlying.setViewOrder(-1);
    //______________________________________________
    windowSession_corr.selectStatus.add(collider_M);
    collider_M.name = "M";
    arr_Collider.add(collider_M);

    createBlock_demo_AlignedCube(amount_col, amount_row, width, height, hGap, vGap, hInitOffset, vInitOffset);

    Widget block = createBlock_addToPane(150, 20, 250, 500, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red_dim);
    windowSession_corr.javafxUtil.make_Draggable(block.node_underlying);
    block.node_underlying.setViewOrder(-1);
    Widget block_02 = createBlock_RandomMoving(150, 20, 1.0);
    Widget block_03 = createBlock_RandomMoving(20, 150, 1.0);
    Widget block_04 = createBlock_RegularMoving(150, 40, 600, 50, 600, 600, 0.5, 2000);
    block_02.node_underlying.setViewOrder(-1);
    block_03.node_underlying.setViewOrder(-1);
    block_04.node_underlying.setViewOrder(-1);

    Widget collider_A1 = createCollider_addToPane(10, 500, 450, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Yellow);
    Widget collider_A2 = createCollider_addToPane(20, 570, 450, windowSession_corr.panel_SemanticRoot, JavafxUtil.color_Red);
    collider_A1.node_underlying.setViewOrder(-1);
    collider_A2.node_underlying.setViewOrder(-1);
    collider_A1.name = "A1";
    collider_A2.name = "A2";
    arr_Collider.add(collider_A1);
    arr_Collider.add(collider_A2);

    return arr_Collider;

  }

  public TrafficLight createTrafficLight_demo_AlignedCube(int amount_col, int amount_row, double width, double height, double hGap, double vGap, double hInitOffset, double vInitOffset,
                                                          int sn_row_Target, int sn_col_Target,
                                                          long timeLength_On, long timeLength_Off, boolean mode_VerticalOrHorizontal) {
    TrafficLight trafficLight = null;
    //_______________________________________________________________

    boolean found = false;
    int sn_row_Loop = 0;
    double posY_Loop = 0 + vInitOffset;
    done: //
    while (sn_row_Loop != amount_row) {
      sn_row_Loop++;
      if (sn_row_Loop == 1) {
        if (mode_VerticalOrHorizontal) {
          posY_Loop += 0;
        }
        else {
          posY_Loop += height;
        }
      }
      else {
        posY_Loop += height + vGap;
      }

      if (sn_row_Loop == sn_row_Target) {
        int sn_col_currRow = 0;
        double posX_currRow = 0 + hInitOffset;
        while (sn_col_currRow != amount_col) {
          sn_col_currRow++;
          if (sn_col_currRow == 1) {
            if (mode_VerticalOrHorizontal) {
              posX_currRow += width;
            }
            else {
              posX_currRow += 0;
            }
          }
          else {
            posX_currRow += hGap + width;
          }
          if (sn_col_currRow == sn_col_Target) {
            if (mode_VerticalOrHorizontal) {
              trafficLight = windowSession_corr.trafficLightManager.create_TrafficLight_withRate(hGap, height, posX_currRow, posY_Loop, timeLength_On, timeLength_Off);
            }
            else {
              trafficLight = windowSession_corr.trafficLightManager.create_TrafficLight_withRate(width, vGap, posX_currRow, posY_Loop, timeLength_On, timeLength_Off);
            }
            found = true;
            break done;
          }
        }
      }
    }

    if (!found) { throw new Error("Not found -- index must be out of bound."); }
    if (trafficLight == null) { throw new Error(); }

    return trafficLight;
  }

  public LinkedList<UnitObj> createBlock_demo_AlignedCube_ComplexMoving_TrafficLight(int amount_col, int amount_row, double width, double height, double hGap, double vGap, double hInitOffset, double vInitOffset) {
    LinkedList<UnitObj> arr_Collide = createBlock_demo_AlignedCube_ComplexMoving(amount_col, amount_row, width, height, hGap, vGap, hInitOffset, vInitOffset);

    TrafficLight trafficLight_01 = createTrafficLight_demo_AlignedCube(amount_col, amount_row, width, height, hGap, vGap, hInitOffset, vInitOffset, 1, 3, 500, 1400, false);
    TrafficLight trafficLight_02 = createTrafficLight_demo_AlignedCube(amount_col, amount_row, width, height, hGap, vGap, hInitOffset, vInitOffset, 2, 2, 1000, 2000, true);
    TrafficLight trafficLight_03 = createTrafficLight_demo_AlignedCube(amount_col, amount_row, width, height, hGap, vGap, hInitOffset, vInitOffset, 2, 3, 500, 1400, true);
    trafficLight_01.node_underlying.setViewOrder(-1);
    trafficLight_02.node_underlying.setViewOrder(-1);
    trafficLight_03.node_underlying.setViewOrder(-1);

    return arr_Collide;
  }

  //_____________

  public void shutDown() {
    for (ExecutorService executor : gp_executor_SimpleShutdown) {
      executor.shutdown(); //
    }

  }

}
