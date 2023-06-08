package com.redfrog.traffic.controller;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.redfrog.traffic.service.TrafficItemControlService;
import com.redfrog.traffic.trafficLight.TrafficLight;

import javafx.application.Platform;

@Controller
@RequestMapping("/v0.2.1.0/developer")
public class TrafficItemController {

  private TrafficItemControlService trafficItemControlService;

  @Autowired
  public TrafficItemController(TrafficItemControlService trafficItemControlService) {
    super();
    this.trafficItemControlService = trafficItemControlService;

  }

  //_________
  //____________________________________________________________

  //___
  //____________________________________________________________________
  //_________________________________________________________________________________________________
  //______________________________________
  //___
  //___________________________________________________________________________________________________
  //_
  //________________________
  //_

  //___
  //_______________________________________________________________________________________________________________________________
  //_
  //________________________________________________________________________________________________________________________________________________________________________________________
  //___
  //_____________________________________________________________
  //_
  //_____________________________________________________________________________________________________________________________
  //_
  //____________________________________________________________
  //______________________________
  //_______________________
  //____________________________
  //
  //_____________________________________________

  //__________________________________________________________________________________________________
  //____________________________________________________________________________________________
  
  
  //_______________________________________________________________________________

  @GetMapping("/test")
  public void test() {
    System.out.println("Test");
  }

  @PostMapping("/testPost")
  public void testPost() {
    System.out.println("testPost");
  }
  
  @PostMapping("/createTrafficLight_demo_AlignedCube")
  public ResponseEntity<TrafficLight> createTrafficLight_demo_AlignedCube(
                                                                          @RequestParam(name = "sn_row_Target", required = false, defaultValue = "1") int sn_row_Target,
                                                                          @RequestParam(name = "sn_col_Target", required = false, defaultValue = "1") int sn_col_Target,
                                                                          @RequestParam(name = "timeLength_AllowMove", required = false, defaultValue = "500") long timeLength_AllowMove,
                                                                          @RequestParam(name = "timeLength_StopMove", required = false, defaultValue = "1400") long timeLength_StopMove,
                                                                          @RequestParam(name = "mode_VerticalOrHorizontal", required = false, defaultValue = "true") boolean mode_VerticalOrHorizontal

  ) {
    int amount_col = 4;
    int amount_row = 4;
    double width = 150;
    double height = 150;
    double hGap = 80;
    double vGap = 80;
    double hInitOffset = 30;
    double vInitOffset = 30;
    
    System.out.println(sn_row_Target);
    System.out.println(sn_col_Target);

    FutureTask<TrafficLight> future = new FutureTask<TrafficLight>(() -> {
      TrafficLight trafficLight = trafficItemControlService.createTrafficLight_demo_AlignedCube(amount_col, amount_row, width, height, hGap, vGap, hInitOffset, vInitOffset, sn_row_Target, sn_col_Target, timeLength_AllowMove, timeLength_StopMove, mode_VerticalOrHorizontal);
      trafficLight.node_underlying.setViewOrder(-1);
      
      System.out.println("Developer");
      System.out.println(trafficLight);
      
      return trafficLight;
    });
    
    Platform.runLater(future);
    
    TrafficLight trafficLight_future;
    
    try {
      trafficLight_future = future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      throw new Error(e);
    }

    return ResponseEntity.ok().body(trafficLight_future);
  }

}
