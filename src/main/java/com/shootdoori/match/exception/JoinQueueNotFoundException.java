package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class JoinQueueNotFoundException extends BusinessException {
  public JoinQueueNotFoundException() {
    super(ErrorCode.JOIN_QUEUE_NOT_FOUND);
  }
}
