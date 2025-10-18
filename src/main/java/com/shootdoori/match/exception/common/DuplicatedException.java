package com.shootdoori.match.exception.common;

public class DuplicatedException extends BusinessException {

    public DuplicatedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicatedException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
