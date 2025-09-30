package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OneselfMatchException extends BusinessException {
  public OneselfMatchException() {
    super(ErrorCode.ONESELF_MATCH);
  }
}
