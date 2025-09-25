package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CaptainNotFoundException extends BusinessException {

    public CaptainNotFoundException() {
        super(ErrorCode.CAPTAIN_NOT_FOUND);
    }
}
