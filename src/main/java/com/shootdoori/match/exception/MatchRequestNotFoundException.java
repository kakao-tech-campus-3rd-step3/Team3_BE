package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MatchRequestNotFoundException extends BusinessException {
  public MatchRequestNotFoundException(Long requestId) {
    super(ErrorCode.MATCH_REQUEST_NOT_FOUND, requestId.toString());
  }
}
