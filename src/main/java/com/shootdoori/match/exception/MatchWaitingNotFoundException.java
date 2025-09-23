package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MatchWaitingNotFoundException extends BusinessException {
  public MatchWaitingNotFoundException(Long waitingId) {
    super(ErrorCode.MATCH_WAITING_NOT_FOUND, waitingId.toString());
  }
}
