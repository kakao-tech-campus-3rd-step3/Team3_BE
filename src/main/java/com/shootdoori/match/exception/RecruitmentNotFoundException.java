package com.shootdoori.match.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecruitmentNotFoundException extends BusinessException {
    public RecruitmentNotFoundException() {
        super(ErrorCode.RECRUITMENT_NOT_FOUND);
    }
}
