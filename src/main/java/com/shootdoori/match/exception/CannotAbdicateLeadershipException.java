package com.shootdoori.match.exception;

public class CannotAbdicateLeadershipException extends BusinessException {

    public CannotAbdicateLeadershipException() {
        super(ErrorCode.CANNOT_ABDICATE_LEADERSHIP);
    }
}
