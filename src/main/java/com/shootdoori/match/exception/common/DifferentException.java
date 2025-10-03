package com.shootdoori.match.exception.common;

public class DifferentException extends BusinessException {

    public DifferentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DifferentException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
