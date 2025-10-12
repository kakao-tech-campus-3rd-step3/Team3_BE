package com.shootdoori.match.exception.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NoPermissionException extends BusinessException {

    public NoPermissionException() {
        super(ErrorCode.NO_PERMISSION);
    }

    public NoPermissionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NoPermissionException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
