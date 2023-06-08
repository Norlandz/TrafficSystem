package com.redfrog.traffic.exception;

public class TypeError extends RuntimeException {

  public TypeError() {
    super();
    //_____________________________________
  }

  public TypeError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    //_____________________________________
  }

  public TypeError(String message, Throwable cause) {
    super(message, cause);
    //_____________________________________
  }

  public TypeError(String message) {
    super(message);
    //_____________________________________
  }

  public TypeError(Throwable cause) {
    super(cause);
    //_____________________________________
  }

}
