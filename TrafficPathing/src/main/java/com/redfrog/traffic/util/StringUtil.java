package com.redfrog.traffic.util;

public class StringUtil {

  public static String omitString(String str, int length_after, int length_leftPart) {
    int length_ori = str.length();
    if (length_after > length_ori) { throw new IndexOutOfBoundsException(); }
    if (length_leftPart > length_after) { throw new IndexOutOfBoundsException(); }
    //________________
    return str.substring(0, length_leftPart) + "..." + str.substring(length_ori - (length_after - length_leftPart - 3), length_ori);
  }

  //______________________________________________________________________________________________________
  public static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
