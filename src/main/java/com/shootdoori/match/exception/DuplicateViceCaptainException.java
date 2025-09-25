package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateViceCaptainException extends BusinessException {

    public DuplicateViceCaptainException() {
        super(ErrorCode.DUPLICATE_VICE_CAPTAIN);
    }
}
