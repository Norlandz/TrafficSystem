package com.redfrog.traffic.model;

import com.redfrog.traffic.collision.CollisionHandler;

import javafx.scene.shape.Shape;

public abstract class UnitObj extends EntityGeneral implements Collidable {

  //___________
  public final transient AnchorPaneWrap node_underlying = new AnchorPaneWrap();

  public transient Shape shape;
  
  //__________________________________________
  //
  //____________________________________________________________________
  //
  //____________________________________________________________________________________________________________
  //__
  //_______________________________________________________________________________

  public transient CollisionHandler collisionHandler;

  //_____________________________________

  //________________________________

  public String name;
  
  //_______________________________________________________________________________________________________
  @Deprecated
  public boolean det_ShouldHandleCollide = true;

}
