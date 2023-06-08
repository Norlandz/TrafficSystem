package com.redfrog.trafficcontroller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.redfrog.traffic.annotation.Config;
import com.redfrog.traffic.trafficLight.TrafficLight;

@Controller
@RequestMapping("/v0.2.1.0/user")
public class TrafficItemController_UserFacing {

  private RestTemplate restTemplate;

  @Autowired
  public TrafficItemController_UserFacing(RestTemplate restTemplate) {
    super();
    this.restTemplate = restTemplate;
  }

  @Config
  public final static String url = "http://localhost:18092";

  // http://localhost:18091/v0.2.1.0/user/createTrafficLight_demo_AlignedCube?sn_row_Target=2&sn_col_Target=1&timeLength_AllowMove=100&timeLength_StopMove=100&mode_VerticalOrHorizontal=false

  @PostMapping("/createTrafficLight_demo_AlignedCube")
  public ResponseEntity<TrafficLight> createTrafficLight_demo_AlignedCube(
                                                                          @RequestParam(name = "sn_row_Target", required = false, defaultValue = "1") int sn_row_Target,
                                                                          @RequestParam(name = "sn_col_Target", required = false, defaultValue = "1") int sn_col_Target,
                                                                          @RequestParam(name = "timeLength_AllowMove", required = false, defaultValue = "500") long timeLength_AllowMove,
                                                                          @RequestParam(name = "timeLength_StopMove", required = false, defaultValue = "1400") long timeLength_StopMove,
                                                                          @RequestParam(name = "mode_VerticalOrHorizontal", required = false, defaultValue = "true") boolean mode_VerticalOrHorizontal

  ) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // idk why have to have header ... 

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("sn_row_Target", "" + sn_row_Target);
    map.add("sn_col_Target", "" + sn_col_Target);
    map.add("timeLength_AllowMove", "" + timeLength_AllowMove);
    map.add("timeLength_StopMove", "" + timeLength_StopMove);
    map.add("mode_VerticalOrHorizontal", "" + mode_VerticalOrHorizontal);

    System.out.println(map);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

    ResponseEntity<TrafficLight> responseEntity = restTemplate.postForEntity(url + "/v0.2.1.0/developer/createTrafficLight_demo_AlignedCube", request, TrafficLight.class);

    System.out.println("User");
    System.out.println(responseEntity.getBody());

    return ResponseEntity.ok().body(responseEntity.getBody());
  }

}
