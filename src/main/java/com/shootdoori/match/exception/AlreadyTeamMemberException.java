package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyTeamMemberException extends BusinessException {

    public AlreadyTeamMemberException() {
        super(ErrorCode.ALREADY_TEAM_MEMBER);
    }
}
