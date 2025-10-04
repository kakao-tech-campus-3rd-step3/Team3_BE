package com.shootdoori.match.exception;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;

public class LeaderCannotLeaveTeamException extends BusinessException {
    public LeaderCannotLeaveTeamException(ErrorCode errorCode) {
        super(errorCode);
    }
}
