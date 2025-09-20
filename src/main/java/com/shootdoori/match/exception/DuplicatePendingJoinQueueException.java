package com.shootdoori.match.exception;

public class DuplicatePendingJoinQueueException extends BusinessException {

    public DuplicatePendingJoinQueueException() {
        super(ErrorCode.JOIN_QUEUE_ALREADY_PENDING);
    }
}
