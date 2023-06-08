package com.redfrog.traffic.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

  public static String time2strnano(Instant instant) { return DateTimeFormatter.ofPattern("yyyyMMdd/HHmm/ss.nnnnnnnnn/Z").withZone(ZoneId.systemDefault()).format(instant); }
  
  public static String time2str(Instant instant) { return DateTimeFormatter.ofPattern("yyyyMMdd_HHmm_ssSSS").withZone(ZoneId.systemDefault()).format(instant); }

}