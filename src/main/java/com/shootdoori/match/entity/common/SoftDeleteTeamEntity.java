package com.shootdoori.match.entity.common;

import com.shootdoori.match.entity.team.TeamStatus;
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
        status = TeamStatus.DELETED;
    }
}
