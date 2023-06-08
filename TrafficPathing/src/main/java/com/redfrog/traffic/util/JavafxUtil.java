package com.redfrog.traffic.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.util.concurrent.AtomicDouble;
import com.redfrog.traffic.exception.TypeError;
import com.redfrog.traffic.session.WindowSession;
import com.redfrog.traffic.shape.Point;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class JavafxUtil {

  private final WindowSession windowSession_corr;

  public JavafxUtil(WindowSession windowSession_corr) {
    super();
    this.windowSession_corr = windowSession_corr;
  }

  //_________

  //
  //__________________________________________________________________________________________________________________
  public static double get_Pos_relToGivenPane(Node node, Pane pane_UpperHost, String axis) {
    double x = 0;
    Node pane_curr = node;

    do {

      if (axis.equals("X")) {
        x += pane_curr.getLayoutX();
      }
      else if (axis.equals("Y")) {
        x += pane_curr.getLayoutY();
      }
      else {
        throw new TypeError();
      }

      pane_curr = (Pane) pane_curr.getParent(); //________
      if (pane_curr == null) {
        System.out.println("This node (seems) is removed from the Scene -- thus its Parent is null -- Listeners still working cuz may need later when Undo.");
        return x;
        //______________________________________________________________________________________________________________________________________________________
        //_____________________________________________________
      }
    } while (pane_curr != pane_UpperHost);

    return x;
  }

  public enum MoveLayerMode {
    PosIncrementRel,
    TopBottom,
    PosAbs,
  }

  public static Pair<Integer, Integer> move_NodeLayer(Node node, int ind_Move, MoveLayerMode moveLayerMode) {
    Pane node_parent = (Pane) node.getParent();

    Integer ind_new = null;
    ObservableList<Node> children = node_parent.getChildren();
    int ind_ori = children.indexOf(node);
    if (ind_ori == -1) { throw new Error("No such element"); }
    int size_BeforeRemove = children.size(); //______________________________________________________________________________________________

    if (moveLayerMode == MoveLayerMode.PosIncrementRel) {
      ind_new = ind_ori + ind_Move;
      if (ind_new > size_BeforeRemove - 1) { ind_new = size_BeforeRemove - 1; }
      if (ind_new < 0) { ind_new = 0; }
      if (ind_new != ind_ori) {
        children.remove(node);
        children.add(ind_new, node);
      }
    }
    else if (moveLayerMode == MoveLayerMode.TopBottom) {
      if (ind_Move == -1 && ind_ori != 0) {
        ind_new = 0;
        node.toBack(); //______________________________________________
      }
      else if (ind_Move == 1 && ind_ori != size_BeforeRemove - 1) { //_____________________________________________________________________________________________________
        ind_new = size_BeforeRemove - 1;
        node.toFront(); //______________________________________________
      }
      else {
        throw new TypeError();
      }
    }
    else if (moveLayerMode == MoveLayerMode.PosAbs) {
      if (ind_Move != ind_ori) {
        ind_new = ind_Move;
        children.remove(node);
        children.add(ind_new, node);
        if (ind_Move >= size_BeforeRemove) { throw new IndexOutOfBoundsException("Index out of bound -- cuz this is not just Add, this is *Remove* then Add. To add to end, Index must 1 less than Size, not equal to Size."); }
      }
    }
    else {
      throw new TypeError();
    }

    return new ImmutablePair<>(ind_ori, ind_new);
  }

  //_____________

  public static void scale_NodeShapeScale(Node node, double scaleX, double scaleY) {
    node.setScaleX(scaleX);
    node.setScaleY(scaleY);
  }

  //_____________

  public final static String rgba_Blue = "rgba(0,0,255,0.5)";
  public final static String rgba_Red_dim = "rgba(128,0,0,0.5)";
  public final static String rgba_Red = "rgba(255,40,40,0.5)";
  public final static String rgba_Red_light = "rgba(255,128,128,0.5)";
  public final static String rgba_Green_dim = "rgba(0,128,0,0.5)";
  public final static String rgba_Green = "rgba(0,255,0,0.5)";
  public final static String rgba_Green_light = "rgba(40,160,40,0.5)";
  public final static String rgba_Blue_dim = "rgba(0,0,128,0.5)";
  public final static String rgba_Yellow = "rgba(210,210,0,0.5)";
  public final static String rgba_Yellow_dim = "rgba(128,128,0,0.5)";
  public final static String rgba_Orange = "rgba(255,165,0,0.5)";
  public final static String rgba_Purple = "rgba(128,0,128,0.5)";
  public final static String rgba_Cyan = "rgba(0,255,255,0.5)";
  public final static String rgba_Teal = "rgba(0,128,128,0.5)";
  public final static String rgba_Grey = "rgba(128,128,128,0.5)";

  public final static String rgba_Transparent = "rgba(255,255,255,0)";
  public final static double rgba_opacity = 0.5;

  public final static Color color_Blue = Color.web(JavafxUtil.rgba_Blue);
  public final static Color color_Red_dim = Color.web(JavafxUtil.rgba_Red_dim);
  public final static Color color_Red = Color.web(JavafxUtil.rgba_Red);
  public final static Color color_Red_light = Color.web(JavafxUtil.rgba_Red_light);
  public final static Color color_Green_dim = Color.web(JavafxUtil.rgba_Green_dim);
  public final static Color color_Green = Color.web(JavafxUtil.rgba_Green);
  public final static Color color_Green_light = Color.web(JavafxUtil.rgba_Green_light);
  public final static Color color_Blue_dim = Color.web(JavafxUtil.rgba_Blue_dim);
  public final static Color color_Yellow = Color.web(JavafxUtil.rgba_Yellow);
  public final static Color color_Yellow_dim = Color.web(JavafxUtil.rgba_Yellow_dim);
  public final static Color color_Orange = Color.web(JavafxUtil.rgba_Orange);
  public final static Color color_Purple = Color.web(JavafxUtil.rgba_Purple);
  public final static Color color_Cyan = Color.web(JavafxUtil.rgba_Cyan);
  public final static Color color_Teal = Color.web(JavafxUtil.rgba_Teal);
  public final static Color color_Grey = Color.web(JavafxUtil.rgba_Grey);

  public final static Color color_Green_opaLight = Color.web(JavafxUtil.rgba_Green, 0.02);
  public final static Color color_Green_dim_opaLight = Color.web(JavafxUtil.rgba_Green_dim, 0.02);
  public final static Color color_Orange_opaLight = Color.web(JavafxUtil.rgba_Orange, 0.02);
  public final static Color color_Grey_opaLight = Color.web(JavafxUtil.rgba_Grey, 0.02);

  public final static LinearGradient lg_Green_dim = LinearGradient.valueOf("linear-gradient(to bottom, " + rgba_Transparent + " 10%," + rgba_Green_dim + " 50%, " + rgba_Transparent + " 10%)");
  //______________________________________________________
  //______________________________________________________________________________________
  //_________________________________________________________________________________________________________________
  //__________________________________________________________________________________________________________________________________________________________________________________

  //_____________

  /**
_______________________________
__*/
  public static LinkedList<Point> get_VerticesFromBoundingBox(Bounds bounds_BB) {
    double posXMin_BB = bounds_BB.getMinX();
    double posYMin_BB = bounds_BB.getMinY();
    double posXMax_BB = bounds_BB.getMaxX();
    double posYMax_BB = bounds_BB.getMaxY();

    LinkedList<Point> arr_vertex = new LinkedList<>();
    arr_vertex.add(new Point(posXMin_BB, posYMin_BB)); //______
    arr_vertex.add(new Point(posXMin_BB, posYMax_BB)); //______
    arr_vertex.add(new Point(posXMax_BB, posYMin_BB)); //______
    arr_vertex.add(new Point(posXMax_BB, posYMax_BB)); //______
    return arr_vertex;
  }

  public static Rectangle convert_Bounds2Rectangle(Bounds bounds) {
    double xx = bounds.getMinX();
    double yy = bounds.getMinY();
    double ww = bounds.getWidth();
    double hh = bounds.getHeight();
    return new Rectangle(xx, yy, ww, hh);
  }

  public static LinkedList<Point> pick_PointClosest(List<Point> arr_point, Point point_Base) { return pick_PointClosest(arr_point, point_Base, false); }

  public static LinkedList<Point> pick_PointClosest(List<Point> arr_point, Point point_Base, boolean det_IgnoreSelf) {
    LinkedList<Point> arr_point_min = new LinkedList<>();
    Double length_min = null;
    for (Point point_curr : arr_point) {
      if (det_IgnoreSelf && point_curr.equals(point_Base)) { continue; }

      double length_curr = point_curr.distance(point_Base);
      if (length_min == null) {
        length_min = length_curr;
        arr_point_min.add(point_curr);
      }
      else {
        if (length_curr < length_min) {
          length_min = length_curr;
          arr_point_min.clear();
          arr_point_min.add(point_curr);
        }
        else if (length_curr == length_min) {
          arr_point_min.add(point_curr); //_
        }
      }
    }
    return arr_point_min;
  }

  //___________________________________________________________________________________________________________________________
  //_______________________________________
  //__________________________________________
  //____________________________________________________
  //_________________________________________________________________________________________________________
  //__________________
  //____________________________________________
  //___________________________________________
  //___________________
  //________________________
  //____________________________________________________
  //_________________________________________________
  //_____________________
  //____________________________________________________________________
  //________________________________________________________________________
  //_______________________________
  //___________________
  //__________________
  //____________________________________________
  //_________________
  //_________________________________
  //_______________

  //_____________

  public static boolean contains_InBounds_CompletelyInside(Bounds bounds, double x, double y) {
    if (bounds.isEmpty()) return false;
    return x > bounds.getMinX() && x < bounds.getMaxX() && y > bounds.getMinY() && y < bounds.getMaxY();
  }

  //_____________

  public void make_Draggable(final Node node) { make_Draggable(node, true); }

  public void make_Draggable(final Node node, boolean det_PreventMovePassLeft) {
    final AtomicDouble posX_offset = new AtomicDouble();
    final AtomicDouble posY_offset = new AtomicDouble();

    //___________________________________________________________________

    node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
      if (!event.isConsumed()) {
        if (event.isControlDown() && event.isAltDown() && !event.isShiftDown() && event.isPrimaryButtonDown()) {
          event.consume();
          //_____________________________________________________

          double x = event.getSceneX();
          double y = event.getSceneY();
          posX_offset.set(node.getLayoutX() - x);
          posY_offset.set(node.getLayoutY() - y);
        }
      }
    });
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
      if (!event.isConsumed()) {
        if (event.isControlDown() && event.isAltDown() && !event.isShiftDown() && event.isPrimaryButtonDown()) {
          event.consume();
          //_______________________________________________________

          double x = event.getSceneX() + posX_offset.get();
          double y = event.getSceneY() + posY_offset.get();

          if (det_PreventMovePassLeft) {
            if (x < 0) { x = 0; }
            if (y < 0) { y = 0; }
          }

          node.setLayoutX(x);
          node.setLayoutY(y);
        }
      }
    });
    //______________________________________________________________
    //________________________________
    //____________________________________________________________
    //______________________________________
    //___________________________________________________
    //
    //___________________________________________________________
    //___________________________________________________________
    //
    //________________________________________
    //_________________________________
    //_________________________________
    //___________
    //
    //_____________________________
    //_____________________________
    //_________
    //_______
    //_______

    node.hoverProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
      if (newValue) { node.setCursor(Cursor.MOVE); }
    });

  }

  public static Double calc_LengthOfArrPath(LinkedList<Point> arrPath) {
    if (arrPath.isEmpty() || arrPath == null) {
      return null;
    }
    else if (arrPath.size() == 1) {
      return 0.0;
    }
    else {
      double length = 0;
      Point point_prev = null;
      int i = -1;
      for (Point point_curr : arrPath) {
        i++;
        if (i == 0) {
          //_____________
        }
        else {
          length += point_prev.distance(point_curr);
        }
        point_prev = point_curr;
      }
      return length;
    }
  }

  /**
____________________________________________________________________
____________________________________________________________________________________________________________________________________________________________
__*/
  public static Point2D get_RelativeOffsetInNode(Node node_Self, Node node_RelativeTo) {
    Point2D pointRel_Self_relScene00 = node_Self.localToScene(0, 0);
    Point2D pointRel_RelativeTo_relScene00 = node_RelativeTo.localToScene(0, 0);
    Point2D pointRel = pointRel_Self_relScene00.subtract(pointRel_RelativeTo_relScene00);
    return pointRel;
  }

  public static Point2D set_RelativeOffsetInNode(Node node_Self, Node node_RelativeTo) {
    Point2D pointRel = get_RelativeOffsetInNode(node_Self, node_RelativeTo);
    node_Self.setLayoutX(pointRel.getX());
    node_Self.setLayoutY(pointRel.getY());
    return pointRel;
  }

  //_____________

}
