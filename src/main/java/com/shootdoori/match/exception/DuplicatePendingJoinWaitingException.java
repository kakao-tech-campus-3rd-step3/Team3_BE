package com.shootdoori.match.exception;

public class DuplicatePendingJoinWaitingException extends BusinessException {

    public DuplicatePendingJoinWaitingException() {
        super(ErrorCode.JOIN_WAITING_ALREADY_PENDING);
    }
}
