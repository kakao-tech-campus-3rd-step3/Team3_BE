package com.shootdoori.match.entity.common;

import com.shootdoori.match.entity.team.TeamStatus;
import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SoftDeleteTeamEntity extends DateEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamStatus status = TeamStatus.ACTIVE;

    public TeamStatus getStatus() {
        return status;
    }

    public void changeStatusDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.TEAM_ALREADY_DELETED);
        }

        status = TeamStatus.DELETED;
    }

    public boolean isDeleted() {
        return status == TeamStatus.DELETED;
    }

    public void changeStatusActive() {
        if (isActive()) {
            throw new BusinessException(ErrorCode.TEAM_ALREADY_ACTIVE);
        }

        status = TeamStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == TeamStatus.ACTIVE;
    }
}
