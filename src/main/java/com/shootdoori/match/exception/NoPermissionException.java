package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NoPermissionException extends BusinessException {

    public NoPermissionException() {
        super(ErrorCode.NO_PERMISSION);
    }
}
