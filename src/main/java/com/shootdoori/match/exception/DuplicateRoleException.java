package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRoleException extends BusinessException {

    public DuplicateRoleException(String roleType) {
        super(ErrorCode.DUPLICATE_ROLE, roleType);
    }
}
