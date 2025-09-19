package com.shootdoori.match.exception;

public class JoinQueueNotFoundException extends BusinessException {
  public JoinQueueNotFoundException() {
    super(ErrorCode.JOIN_QUEUE_NOT_FOUND);
  }
}
