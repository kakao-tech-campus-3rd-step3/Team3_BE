package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeamMemberNotFoundException extends BusinessException {

    public TeamMemberNotFoundException() {
        super(ErrorCode.TEAM_MEMBER_NOT_FOUND);
    }
}
