package com.redfrog.traffic.model;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;

public class AnchorPaneWrap extends AnchorPane {

  private AnchorPane paneBorderInner;

  private void initPaneBorderInner() {
    paneBorderInner = new AnchorPane();
    this.getChildren().add(paneBorderInner);
    paneBorderInner.setBackground(Background.EMPTY);
    paneBorderInner.prefWidthProperty().bind(this.widthProperty());
    paneBorderInner.prefHeightProperty().bind(this.heightProperty());
  }

  public AnchorPane getPaneBorderInner() {
    if (paneBorderInner == null) { initPaneBorderInner(); }
    return paneBorderInner;
  }

  public String getBorderInnerCssStyle() {
    if (paneBorderInner == null) { initPaneBorderInner(); }
    return paneBorderInner.getStyle();
  }
  
  public void setBorderInnerCssStyle(String cssStyle) {
    if (paneBorderInner == null) { initPaneBorderInner(); }
    paneBorderInner.setStyle(cssStyle);
  }

}
