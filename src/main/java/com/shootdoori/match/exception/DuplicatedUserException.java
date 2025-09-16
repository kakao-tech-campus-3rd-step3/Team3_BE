package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatedUserException extends BusinessException {

    public DuplicatedUserException() {
        super(ErrorCode.DUPLICATED_USER);
    }
}
