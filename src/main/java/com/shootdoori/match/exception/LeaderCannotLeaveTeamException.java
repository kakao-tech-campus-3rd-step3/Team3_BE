package com.shootdoori.match.exception;

public class LeaderCannotLeaveTeamException extends BusinessException {
    public LeaderCannotLeaveTeamException(ErrorCode errorCode) {
        super(errorCode);
    }
}
