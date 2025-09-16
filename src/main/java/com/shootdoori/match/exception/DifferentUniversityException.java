package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class DifferentUniversityException extends BusinessException {

    public DifferentUniversityException() {
        super(ErrorCode.DIFFERENT_UNIVERSITY);
    }
}
