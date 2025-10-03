package com.shootdoori.match.exception.domain.team;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TeamCapacityExceededException extends BusinessException {

    public TeamCapacityExceededException() {
        super(ErrorCode.TEAM_CAPACITY_EXCEEDED);
    }
}
