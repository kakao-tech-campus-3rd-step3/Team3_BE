package com.shootdoori.match.entity.team;

import com.shootdoori.match.entity.common.DateEntity;
import com.shootdoori.match.entity.common.SoftDeleteTeamMemberEntity;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DifferentException;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import jakarta.persistence.AttributeOverride;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "team_member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "user_id"})
    }
)
@SQLRestriction("status = 'ACTIVE'")
@AttributeOverride(name = "createdAt", column = @Column(name = "joined_at", nullable = false, updatable = false))
public class TeamMember extends SoftDeleteTeamMemberEntity {

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


    protected TeamMember() {
    }

    public TeamMember(Team team, User user, TeamMemberRole role) {
        this.team = team;
        this.user = user;
        this.role = role != null ? role : TeamMemberRole.MEMBER;
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
        return getCreatedAt();
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void delegateLeadership(TeamMember newLeader) {

        if (!this.isCaptain()) {
            // TODO: NoPermissionException 경우구별 안되는 문제 해결해야 함.
            throw new NoPermissionException();
        }

        if (this.equals(newLeader)) {
            throw new DuplicatedException(ErrorCode.SELF_DELEGATION_NOT_ALLOWED);
        }

        if (!this.team.equals(newLeader.getTeam())) {
            throw new DifferentException(ErrorCode.DIFFERENT_TEAM_DELEGATION_NOT_ALLOWED);
        }

        // TODO: 이전 회장을 일반 멤버 or 역할 교환 어떤 것이 나은지 고민해야 함.
        this.role = TeamMemberRole.MEMBER;
        newLeader.role = TeamMemberRole.LEADER;
    }

    public void changeRole(Team team, TeamMemberRole newRole) {

        if (isPromotionToLeader(newRole)) {
            validateLeaderPromotion(team);
        }

        if (isPromotionToViceLeader(newRole)) {
            validateViceLeaderPromotion(team);
        }

        this.role = newRole;
    }

    private boolean isPromotionToLeader(TeamMemberRole newRole) {
        return newRole == TeamMemberRole.LEADER && this.role != TeamMemberRole.LEADER;
    }

    private boolean isPromotionToViceLeader(TeamMemberRole newRole) {
        return newRole == TeamMemberRole.VICE_LEADER && this.role != TeamMemberRole.VICE_LEADER;
    }

    private void validateLeaderPromotion(Team team) {
        if (team.hasCaptain()) {
            throw new DuplicatedException(ErrorCode.DUPLICATE_ROLE, "회장");
        }
    }

    private void validateViceLeaderPromotion(Team team) {
        if (team.hasViceCaptain()) {
            throw new DuplicatedException(ErrorCode.DUPLICATE_ROLE, "부회장");
        }
    }

    public boolean canMakeJoinDecisionFor(Team team) {
        if (!this.team.equals(team) || !this.role.canMakeJoinDecision()) {
            throw new NoPermissionException();
        }

        return true;
    }

    public boolean isCaptain() {
        return this.role == TeamMemberRole.LEADER;
    }

    public boolean isViceCaptain() {
        return this.role == TeamMemberRole.VICE_LEADER;
    }

    public void delete() {
        changeStatusDeleted();
    }

    public void restore() {
        changeStatusActive();
    }
}
