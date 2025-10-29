package com.shootdoori.match.entity.team;

import com.shootdoori.match.entity.common.AuditInfo;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DifferentException;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import java.util.Objects;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "team_member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "user_id"})
    }
)
@AttributeOverride(name = "audit.createdAt", column = @Column(name = "joined_at", nullable = false, updatable = false))
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

    @Embedded
    private AuditInfo audit = new AuditInfo();

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

    public LocalDateTime getJoinedAt() { return audit.getCreatedAt(); }

    public LocalDateTime getUpdatedAt() { return audit.getUpdatedAt(); }

    public void setTeam(Team team) {
        this.team = team;
    }

    public TeamMember delegateLeadership(TeamMember newLeader) {

        if (!this.isCaptain()) {
            throw new NoPermissionException(ErrorCode.LEADERSHIP_DELEGATION_FORBIDDEN);
        }

        validateDelegate(newLeader);

        this.role = TeamMemberRole.MEMBER;
        newLeader.role = TeamMemberRole.LEADER;

        return this;
    }

    public TeamMember delegateViceLeadership(TeamMember newViceLeader) {

        if (!this.isViceCaptain()) {
            throw new NoPermissionException(ErrorCode.VICE_LEADERSHIP_DELEGATION_FORBIDDEN);
        }

       validateDelegate(newViceLeader);

        this.role = TeamMemberRole.MEMBER;
        newViceLeader.role = TeamMemberRole.VICE_LEADER;

        return this;
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

    private void validateDelegate(TeamMember targetMember) {

        if (this.equals(targetMember)) {
            throw new DuplicatedException(ErrorCode.SELF_DELEGATION_NOT_ALLOWED);
        }

        if (!this.team.equals(targetMember.getTeam())) {
            throw new DifferentException(ErrorCode.DIFFERENT_TEAM_DELEGATION_NOT_ALLOWED);
        }
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
            throw new NoPermissionException(ErrorCode.INSUFFICIENT_ROLE_FOR_JOIN_DECISION);
        }

        return true;
    }

    public boolean isCaptain() {
        return this.role == TeamMemberRole.LEADER;
    }

    public boolean isViceCaptain() {
        return this.role == TeamMemberRole.VICE_LEADER;
    }

    public void checkCaptainPermission(Long userId) {
        if(!Objects.equals(this.team.getCaptain().getId(), userId)) {
            throw new NoPermissionException(ErrorCode.CAPTAIN_ONLY_OPERATION);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TeamMember)) {
            return false;
        }
        TeamMember teamMember = (TeamMember) o;
        return Objects.equals(id, teamMember.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getTeamId() {
        return this.team.getTeamId();
    }
}
