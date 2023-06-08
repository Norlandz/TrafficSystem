package com.redfrog.traffic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import com.redfrog.traffic.session.WindowSession;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

//________________________________________________________________________________________________________
public class TrafficPathingAppJavafxBoot extends Application {

  private ConfigurableApplicationContext applicationContext;

  @Override
  public void init() { applicationContext = new SpringApplicationBuilder(TrafficPathingAppSpringBoot.class).run(); }

  @Override
  public void start(Stage primaryStage) { applicationContext.publishEvent(new StageReadyEvent(primaryStage)); }

  @Override
  public void stop() {
    //_________________
    applicationContext.close();
    Platform.exit();
  }

  static class StageReadyEvent extends ApplicationEvent {
    public StageReadyEvent(Stage stage) { super(stage); }

    public Stage getStage() { return ((Stage) getSource()); }
  }
}
