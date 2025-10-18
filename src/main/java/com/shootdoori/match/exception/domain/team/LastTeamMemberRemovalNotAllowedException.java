package com.shootdoori.match.exception.domain.team;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LastTeamMemberRemovalNotAllowedException extends BusinessException {

    public LastTeamMemberRemovalNotAllowedException() {
        super(ErrorCode.LAST_TEAM_MEMBER_REMOVAL_NOT_ALLOWED);
    }
}
