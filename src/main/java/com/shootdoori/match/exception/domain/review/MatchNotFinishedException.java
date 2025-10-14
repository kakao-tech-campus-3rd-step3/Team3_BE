package com.shootdoori.match.exception.domain.review;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MatchNotFinishedException extends BusinessException {
    public MatchNotFinishedException() {
        super(ErrorCode.MATCH_NOT_FINISHED_YET);
    }
}
