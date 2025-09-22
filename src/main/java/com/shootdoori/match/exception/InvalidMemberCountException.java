package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMemberCountException extends BusinessException {

    public InvalidMemberCountException(int count) {
        super(ErrorCode.INVALID_MEMBER_COUNT, String.valueOf(count));
    }
}
