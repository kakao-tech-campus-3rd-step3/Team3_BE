package com.shootdoori.match.exception.common;

public class TooManyRequestsException extends BusinessException {

  public TooManyRequestsException(ErrorCode errorCode) {
    super(errorCode);
  }

  public TooManyRequestsException(ErrorCode errorCode, String detail) {
    super(errorCode, detail);
  }
}