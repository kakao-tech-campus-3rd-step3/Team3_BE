package com.shootdoori.match.exception.domain.team;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;

public class TeamHasRemainingMembersException extends BusinessException {
    public TeamHasRemainingMembersException() {
        super(ErrorCode.TEAM_HAS_REMAINING_MEMBERS);
    }
}
