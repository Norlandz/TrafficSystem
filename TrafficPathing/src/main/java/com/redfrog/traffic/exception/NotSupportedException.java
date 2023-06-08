package com.redfrog.traffic.exception;

public class NotSupportedException extends RuntimeException {

  public NotSupportedException() {
    super();
    //_____________________________________
  }

  public NotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    //_____________________________________
  }

  public NotSupportedException(String message, Throwable cause) {
    super(message, cause);
    //_____________________________________
  }

  public NotSupportedException(String message) {
    super(message);
    //_____________________________________
  }

  public NotSupportedException(Throwable cause) {
    super(cause);
    //_____________________________________
  }

}
