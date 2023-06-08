package com.redfrog.traffic.collision;

import com.redfrog.traffic.model.UnitObj;

@FunctionalInterface
public interface CollisionHandler {
  void handle(UnitObj unitObj_AA, UnitObj unitObj_BB);

}
