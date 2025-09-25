package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCaptainException extends BusinessException {

    public DuplicateCaptainException() {
        super(ErrorCode.DUPLICATE_CAPTAIN);
    }
}
