package com.redfrog.traffic;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.redfrog.traffic.TrafficPathingAppJavafxBoot.StageReadyEvent;
import com.redfrog.traffic.model.AnchorPaneWrap;
import com.redfrog.traffic.model.UnitObj;
import com.redfrog.traffic.pathing.PathingUtil.GotoMode;
import com.redfrog.traffic.pathing.PathingUtil.GotoStatus;
import com.redfrog.traffic.service.TrafficItemControlService;
import com.redfrog.traffic.session.WindowSession;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//___________________________________________________________________________

@Component
public class TrafficPathingAppSessionInit implements ApplicationListener<StageReadyEvent> {

  //________________________________________________________
  //____________________________________________________________

  //_________

  //___________________________________________________________________
  @Autowired
  private WindowSession windowSession_corr;

  @Autowired
  private TrafficItemControlService trafficItemControlService;

  //_________

  public static final double speed_Move = 1.0;

  @Override
  public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
    final Stage primaryStage = stageReadyEvent.getStage();

    windowSession_corr.javafxStage = primaryStage;

    double w_PanelSemanticRoot = 900 + 50;
    double h_PanelSemanticRoot = 600 + 50;
    double w_Scene = w_PanelSemanticRoot + 50;
    double h_Scene = h_PanelSemanticRoot + 50;

    //____
    AnchorPane pane_JavafxRoot = new AnchorPane();
    windowSession_corr.pane_JavafxRoot = pane_JavafxRoot;
    Scene scene = new Scene(pane_JavafxRoot, w_Scene, h_Scene);
    primaryStage.setScene(scene);
    pane_JavafxRoot.setBackground(Background.EMPTY); //_____________________________________
    primaryStage.setTitle("Init Page");

    AnchorPane panel_SemanticRoot = new AnchorPane();
    windowSession_corr.panel_SemanticRoot = panel_SemanticRoot;
    pane_JavafxRoot.getChildren().add(panel_SemanticRoot);
    panel_SemanticRoot.setLayoutX(10);
    panel_SemanticRoot.setLayoutY(10);
    panel_SemanticRoot.setPrefSize(w_PanelSemanticRoot, h_PanelSemanticRoot);
    //_______________________________________________________

    //____

    LinkedList<UnitObj> arr_Collider;

    //_______________________________________________________________________
    //_________________________________________________________________________________________________________________
    //___________________________________________________________________________
    //___________________________________________________________________________________
    //________________________________________________________________________________________________________________________
    arr_Collider = trafficItemControlService.createBlock_demo_AlignedCube_ComplexMoving_TrafficLight(4, 4, 150, 150, 80, 80, 30, 30);

    arr_Collider.forEach(e -> {
      listen_Select(e);
    });

    UnitObj collider_M = arr_Collider.getFirst();

    //__________________

    primaryStage.show();

    //_____________________________________

    //____
    panel_SemanticRoot.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
      if (!event.isConsumed()) {
        if (!event.isControlDown() && !event.isAltDown() && event.isSecondaryButtonDown()) {
          event.consume();
          double posX = event.getX();
          double posY = event.getY();

          Set<UnitObj> gp_unitObj_Selected = windowSession_corr.selectStatus.gp_unitObj_Selected;
          if (gp_unitObj_Selected.size() == 0) { System.out.println("arr_node_Selected has 0 item"); }

          int i = 0;
          for (UnitObj unitObj_Selected_curr : gp_unitObj_Selected) {
            i++;
            //_________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
            System.out.printf("Point Start: %f, %f %n", unitObj_Selected_curr.node_underlying.getLayoutX(), unitObj_Selected_curr.node_underlying.getLayoutY());
            System.out.printf("Point End: %f, %f %n", posX, posY);

            if (!event.isShiftDown()) {
              windowSession_corr.pathingUtil.goTo_TargetLocation(unitObj_Selected_curr, posX, posY, speed_Move, GotoMode.Immediate_AbandonAllPrevGotoTargets);
            }
            else {
              windowSession_corr.pathingUtil.goTo_TargetLocation(unitObj_Selected_curr, posX, posY, speed_Move, GotoMode.Last_AfterAllPrevGotoTargets);
            }
          }
        }
      }
    });

    //____
    panel_SemanticRoot.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
      if (!event.isConsumed()) {
        KeyCode keyCode = event.getCode();
        if (!event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && (keyCode == KeyCode.C || keyCode == KeyCode.S || keyCode == KeyCode.E)) {
          event.consume();

          Set<UnitObj> gp_unitObj_Selected = windowSession_corr.selectStatus.gp_unitObj_Selected;
          if (gp_unitObj_Selected.size() == 0) { System.out.println("arr_node_Selected has 0 item"); }

          int i = 0;
          for (UnitObj unitObj_Selected_curr : gp_unitObj_Selected) {
            i++;
            GotoStatus gotoStatus = null;
            if (event.getCode() == KeyCode.C) {
              gotoStatus = GotoStatus.Cancelled;
              windowSession_corr.pathingUtil.cancel_goTo_TargetLocations(unitObj_Selected_curr);
            }
            else if (event.getCode() == KeyCode.S) {
              gotoStatus = GotoStatus.Paused;
              windowSession_corr.pathingUtil.pause_goTo_TargetLocations(unitObj_Selected_curr); //__
            }
            else if (event.getCode() == KeyCode.E) {
              gotoStatus = GotoStatus.Resumed;
              windowSession_corr.pathingUtil.resume_goTo_TargetLocations(unitObj_Selected_curr); //__
            }
            System.out.printf("Point %s: %f, %f %n", gotoStatus, unitObj_Selected_curr.node_underlying.getLayoutX(), unitObj_Selected_curr.node_underlying.getLayoutY());
          }
        }
      }
    });

    //____
    Button button__clear_PathingDebugShape = new Button();
    panel_SemanticRoot.getChildren().add(button__clear_PathingDebugShape);
    button__clear_PathingDebugShape.setText("clear_PathingDebugShape()");
    button__clear_PathingDebugShape.setMnemonicParsing(false);
    button__clear_PathingDebugShape.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
      if (!event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && event.isPrimaryButtonDown()) {
        System.out.println(">> clicked :: " + Instant.now());
        windowSession_corr.pathingUtil.clear_PathingDebugShape();
      }
    });
    button__clear_PathingDebugShape.setViewOrder(-1);
    //__________________________
    primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
      if (!event.isControlDown() && event.isAltDown() && !event.isShiftDown() && event.getCode() == KeyCode.A) {
        System.out.println(">> clicked :: " + Instant.now());
        windowSession_corr.pathingUtil.clear_PathingDebugShape();
      }
      else if (!event.isControlDown() && event.isAltDown() && !event.isShiftDown() && event.getCode() == KeyCode.S) {
        windowSession_corr.selectStatus.clear();
        windowSession_corr.selectStatus.add(collider_M);
        //______________________________________________________
        System.out.println(collider_M.node_underlying.getWidth());
        System.out.println(collider_M.node_underlying.getHeight());
        Bounds bounds_AA = collider_M.node_underlying.getBoundsInParent();
        double ww = bounds_AA.getWidth();
        double hh = bounds_AA.getHeight();
        System.out.println(ww + " :: " + hh);
      }
      else if (event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && event.getCode() == KeyCode.A) { //
        arr_Collider.forEach(e -> {
          windowSession_corr.selectStatus.add(e);
        });
      }
    });
    primaryStage.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
      if (!event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && event.isMiddleButtonDown()) {
        System.out.println(">> clicked :: " + Instant.now());
        windowSession_corr.selectStatus.clear();
      }
    });

    //____
    Button button_gotoRandomLocations = new Button();
    panel_SemanticRoot.getChildren().add(button_gotoRandomLocations);
    button_gotoRandomLocations.setLayoutY(30);
    button_gotoRandomLocations.setText("gotoRandomLocations()");
    button_gotoRandomLocations.setMnemonicParsing(false);
    button_gotoRandomLocations.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
      if (!event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && event.isPrimaryButtonDown()) {
        System.out.println(">> clicked :: " + Instant.now());
        windowSession_corr.selectStatus.gp_unitObj_Selected.forEach(e -> {
          trafficItemControlService.gotoRandomLocations(e, speed_Move);
        });
      }
    });
    button_gotoRandomLocations.setViewOrder(-1);

    //_________________

    run_test();

    //_________________

    primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (event) -> {
      trafficItemControlService.shutDown();
      windowSession_corr.trafficLightManager.shutDown();
    });
  }

  //_________________

  public void listen_Select(UnitObj unitObj) {
    AnchorPaneWrap node = unitObj.node_underlying;
    //_________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
      //_____________________________________________________________________________________________________________________
      if (!event.isConsumed()) {
        if (!event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && event.isPrimaryButtonDown()) {
          event.consume();
          System.out.println("Selected :: " + node);
          windowSession_corr.selectStatus.clear();
          windowSession_corr.selectStatus.add(unitObj);
        }
        else if (event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && event.isPrimaryButtonDown()) {
          event.consume();
          System.out.println("Selected :: " + node);
          windowSession_corr.selectStatus.add(unitObj);
        }

      }
    });
  }

  //_________________

  //________
  private void run_test() {
    //________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
  }

}