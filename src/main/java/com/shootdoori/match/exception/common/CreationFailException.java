package com.shootdoori.match.exception.common;

public class CreationFailException extends BusinessException {
    public CreationFailException(ErrorCode errorCode) {
      super(errorCode);
    }
}
