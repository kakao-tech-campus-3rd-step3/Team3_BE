package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeamNotFoundException extends BusinessException {

    public TeamNotFoundException(Long teamId) {
        super(ErrorCode.TEAM_NOT_FOUND, "ID: " + teamId);
    }
}
