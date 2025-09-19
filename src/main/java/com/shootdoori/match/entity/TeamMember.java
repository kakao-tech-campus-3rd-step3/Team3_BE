package com.shootdoori.match.entity;

import com.shootdoori.match.dto.UpdateTeamMemberRequestDto;
import com.shootdoori.match.exception.DuplicateCaptainException;
import com.shootdoori.match.exception.DuplicateViceCaptainException;
import com.shootdoori.match.exception.NoPermissionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "team_member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "user_id"})
    }
)
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private TeamMemberRole role = TeamMemberRole.MEMBER;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected TeamMember() {
    }

    public TeamMember(Team team, User user, TeamMemberRole role) {
        this.team = team;
        this.user = user;
        this.role = role != null ? role : TeamMemberRole.MEMBER;
    }

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public User getUser() {
        return user;
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void changeRole(Team team, UpdateTeamMemberRequestDto requestDto) {
        TeamMemberRole newRole = TeamMemberRole.fromDisplayName(requestDto.role());

        if (newRole == TeamMemberRole.LEADER && team.hasCaptain()
            && this.role != TeamMemberRole.LEADER) {
            throw new DuplicateCaptainException();
        }

        if (newRole == TeamMemberRole.VICE_LEADER && team.hasViceCaptain()
            && this.role != TeamMemberRole.VICE_LEADER) {
            throw new DuplicateViceCaptainException();
        }

        this.role = newRole;
    }

    public boolean canMakeJoinDecisionFor(Team team) {
        if (!this.team.equals(team) || !this.role.canMakeJoinDecision()) {
            throw new NoPermissionException();
        }

        return true;
    }
}
