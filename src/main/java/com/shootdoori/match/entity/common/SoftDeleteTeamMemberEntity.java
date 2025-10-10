package com.shootdoori.match.entity.common;

import com.shootdoori.match.entity.team.TeamMemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SoftDeleteTeamMemberEntity extends DateEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamMemberStatus status = TeamMemberStatus.ACTIVE;

    public TeamMemberStatus getStatus() {
        return status;
    }

    public void changeStatusDeleted() {
        status = TeamMemberStatus.DELETED;
    }

    public boolean isDeleted() {
        return status == TeamMemberStatus.DELETED;
    }

    public void changeStatusActive() {
        status = TeamMemberStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == TeamMemberStatus.ACTIVE;
    }
}
