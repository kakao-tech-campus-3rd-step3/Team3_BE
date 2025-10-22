package com.shootdoori.match.exception.common;

public class UnauthorizedException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public UnauthorizedException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public UnauthorizedException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + (detail != null ? " (" + detail + ")" : ""));
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }
}