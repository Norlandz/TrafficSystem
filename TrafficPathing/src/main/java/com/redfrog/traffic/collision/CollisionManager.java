package com.redfrog.traffic.collision;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.redfrog.traffic.annotation.Main;
import com.redfrog.traffic.model.UnitObj;
import com.redfrog.traffic.session.WindowSession;
import com.redfrog.traffic.shape.Point;
import com.redfrog.traffic.util.JavafxUtil;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

//______________________________________________________________________________________________________
//_________________________________________________________________________________________________________________________
//_
//__________________________________________________________
//______________________________________________________________________________________
public class CollisionManager {

  private final WindowSession windowSession_corr;

  public CollisionManager(WindowSession windowSession_corr) {
    this.windowSession_corr = windowSession_corr;

  }

  //_________
  public HashSet<UnitObj> gp_unitObj = new HashSet<>();
  public HashMap<Node, UnitObj> mpp__node_vs_unitObj = new HashMap<>();

  public static double collusionDistanceThreshold = 2.0;
  public static double collusionDistanceThresholdLv2 = collusionDistanceThreshold + 3.0;

  public final static CollisionHandler NullCollisionHandler = (unitObj_AA, unitObj_BB) -> {};

  //_________

  //_______________________

  @Main
  public void register(UnitObj unitObj_MayCollider_Reg) {
    //____
    gp_unitObj.add(unitObj_MayCollider_Reg);
    mpp__node_vs_unitObj.put(unitObj_MayCollider_Reg.node_underlying, unitObj_MayCollider_Reg);

    //____
    final Rectangle rectangle_VisualBounds = new Rectangle();
    windowSession_corr.panel_SemanticRoot.getChildren().add(rectangle_VisualBounds);
    //________________________________________
    //__________________________________________________________________
    //______________________________________________________________
    //_________________________________________________________________________________________
    //________________________________________________________________________________________________
    //_______________________________________
    //______________________________________________________________
    rectangle_VisualBounds.setStrokeWidth(0.5);
    rectangle_VisualBounds.setStroke(Color.TEAL);
    rectangle_VisualBounds.setFill(Color.TRANSPARENT);

    //____

    //______________________________________________________
    //__________________________________________________________________________________________
    //__________________________________________________________
    //_______________________________________________________________________________________________________
    //______________________________________________________________________________________
    unitObj_MayCollider_Reg.node_underlying.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> {
      //______________________________
      //___________________________________
      //___________________________________
      //___________________________________________
      //___________________________________________
      //_________________________________________________________________________
      //_____________________________________________________________________________________________________________________
      //__________________________________________________
      for (UnitObj unitObj_MayCollidedOn_curr : gp_unitObj) {
        if (unitObj_MayCollidedOn_curr != unitObj_MayCollider_Reg) {

          //_________________
          //______________________________________
          if (det_UnderCollisionThreshold(unitObj_MayCollider_Reg, unitObj_MayCollidedOn_curr)) {           //________________________________________________________________________________________________________________________________________________________________________
            if (unitObj_MayCollidedOn_curr.det_ShouldHandleCollide) {
              CollisionHandler collisionHandler = unitObj_MayCollider_Reg.collisionHandler;
              if (collisionHandler != NullCollisionHandler) {
                collisionHandler.handle(unitObj_MayCollider_Reg, unitObj_MayCollidedOn_curr);
                //__________________________________________________________________________
              }
            }
          }
        }
      }

      //____
      Bounds bounds = unitObj_MayCollider_Reg.node_underlying.getBoundsInParent();
      rectangle_VisualBounds.setX(bounds.getMinX());
      rectangle_VisualBounds.setY(bounds.getMinY());
      rectangle_VisualBounds.setWidth(bounds.getWidth());
      rectangle_VisualBounds.setHeight(bounds.getHeight());

    });

  }

  public static BoundingBox get_BoundingBox_WithThreshold(Bounds bounds_BB, double collusionDistanceThreshold_in) {
    double poxXMin_BB = bounds_BB.getMinX();
    double poxYMin_BB = bounds_BB.getMinY();
    double posXMax_BB = bounds_BB.getMaxX();
    double posYMax_BB = bounds_BB.getMaxY();

    double poxXMin_BB_Adj = poxXMin_BB - collusionDistanceThreshold_in;
    double poxYMin_BB_Adj = poxYMin_BB - collusionDistanceThreshold_in;
    double posXMax_BB_Adj = posXMax_BB + collusionDistanceThreshold_in;
    double posYMax_BB_Adj = posYMax_BB + collusionDistanceThreshold_in;

    double lengthX_BB_Adj = posXMax_BB_Adj - poxXMin_BB_Adj;
    double lengthY_BB_Adj = posYMax_BB_Adj - poxYMin_BB_Adj;

    //________________________________________________________________________________________________________________
    BoundingBox bounds_BB_Adj = new BoundingBox(poxXMin_BB_Adj, poxYMin_BB_Adj, lengthX_BB_Adj, lengthY_BB_Adj);
    return bounds_BB_Adj;
  }

  private static boolean det_UnderCollisionThreshold(UnitObj unitObj_AA, UnitObj unitObj_BB) {
    //_______________________________________________________________________________________

    Bounds bounds_AA = unitObj_AA.node_underlying.getBoundsInParent();
    Bounds bounds_BB = unitObj_BB.node_underlying.getBoundsInParent();

    BoundingBox bounds_BB_Adj = CollisionManager.get_BoundingBox_WithThreshold(bounds_BB, collusionDistanceThreshold);
    //__________________________
    if (bounds_BB_Adj.intersects(bounds_AA)) {
      //____________________________________
      return true;
    }
    else {
      //_______________________________________
      return false;
    }

    //_____________________________________________________________________________________
    //_________________________________________________
    //______________
    //__________________________________________________________________________________________________________________________
    //______________________________________________________________________________________________________________________________
    //______________
    //____________________________________________________________
    //__________________________________________________________________
    //________________________________________________________
    //_________________
    //_______________
    //______________
    //______________
    //______________
    //_________________________________________________

  }

  public static Point get_VertexWithCollisionThresholdLv2(Bounds bounds, Point vertex) {
    List<Point> arr_vertex = JavafxUtil.get_VerticesFromBoundingBox(bounds);
    boolean found = false;
    int i = 0;
    for (Point vertex_curr : arr_vertex) {
      i++;
      //________________________
      if (vertex_curr.equals(vertex)) {
        found = true;
        break;
      }
    }

    if (found) {
      if (i == 1) {
        return new Point(vertex.getX() - collusionDistanceThresholdLv2, vertex.getY() - collusionDistanceThresholdLv2);
      }
      else if (i == 2) {
        return new Point(vertex.getX() - collusionDistanceThresholdLv2, vertex.getY() + collusionDistanceThresholdLv2);
      }
      else if (i == 3) {
        return new Point(vertex.getX() + collusionDistanceThresholdLv2, vertex.getY() - collusionDistanceThresholdLv2);
      }
      else if (i == 4) {
        return new Point(vertex.getX() + collusionDistanceThresholdLv2, vertex.getY() + collusionDistanceThresholdLv2);
      }
      else {
        throw new Error();
      }
    }
    else {
      throw new Error("vertex not found");
    }

  }

}
