package com.redfrog.traffic.status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.redfrog.traffic.model.AnchorPaneWrap;
import com.redfrog.traffic.model.UnitObj;
import com.redfrog.traffic.session.WindowSession;
import com.redfrog.traffic.util.JavafxUtil;

public class SelectStatus {

  private final WindowSession windowSession_corr;

  public SelectStatus(WindowSession windowSession_corr) {
    this.windowSession_corr = windowSession_corr; //
  }

  public Set<UnitObj> gp_unitObj_Selected = new HashSet<>();
  private HashMap<UnitObj, String> mpp__unitObj_vs_cssStyle = new HashMap<>();

  private final static String cssStyleStr_Focus = ""
                                                  + "-fx-border-style: solid;"
                                                  + "-fx-border-width: 2;"
                                                  + "-fx-border-color: " + JavafxUtil.rgba_Cyan + ";";

  public void add(UnitObj unitObj) {
    boolean det_NotAlreadyExist = gp_unitObj_Selected.add(unitObj);
    if (det_NotAlreadyExist) { //______________________________________________________
      AnchorPaneWrap node = unitObj.node_underlying;
      mpp__unitObj_vs_cssStyle.put(unitObj, node.getBorderInnerCssStyle());
      node.setBorderInnerCssStyle(cssStyleStr_Focus);
    }
  }

  public void remove(UnitObj unitObj) {
    gp_unitObj_Selected.remove(unitObj);
    AnchorPaneWrap node = unitObj.node_underlying;
    node.setBorderInnerCssStyle(mpp__unitObj_vs_cssStyle.get(unitObj));
  }

  public void clear() {
    for (UnitObj unitObj : gp_unitObj_Selected) {
      AnchorPaneWrap node = unitObj.node_underlying;
      node.setBorderInnerCssStyle(mpp__unitObj_vs_cssStyle.get(unitObj));
    }
    gp_unitObj_Selected.clear();
  }

}
