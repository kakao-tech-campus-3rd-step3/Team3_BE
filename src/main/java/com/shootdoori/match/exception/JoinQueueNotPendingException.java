package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class JoinQueueNotPendingException extends BusinessException {
    public JoinQueueNotPendingException() {
        super(ErrorCode.JOIN_QUEUE_NOT_PENDING);
    }
}

