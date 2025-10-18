package com.shootdoori.match.exception.domain.joinwaiting;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class JoinWaitingNotPendingException extends BusinessException {
    public JoinWaitingNotPendingException() {
        super(ErrorCode.JOIN_WAITING_NOT_PENDING);
    }
}

