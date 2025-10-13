package com.shootdoori.match.entity.team.join;

import com.shootdoori.match.entity.common.DateEntity;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.domain.joinwaiting.JoinWaitingNotPendingException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "join_waiting",
    indexes = {
        @Index(name = "idx_join_waiting_team_status", columnList = "team_id,status")
    }
)
public class JoinWaiting extends DateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JoinWaitingStatus status = JoinWaitingStatus.PENDING;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public User getApplicant() {
        return applicant;
    }

    public JoinWaitingStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public User getDecidedBy() {
        return decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public Long getVersion() {
        return version;
    }

    protected JoinWaiting() {

    }

    public JoinWaiting(Team team, User applicant, String message) {
        this.team = team;
        this.applicant = applicant;
        this.message = message;
    }

    public static JoinWaiting create(Team team, User applicant, String message) {
        return new JoinWaiting(team, applicant, message);
    }

    public void approve(TeamMember approver, TeamMemberRole role, String decisionReason) {
        verifyPending();
        approver.canMakeJoinDecisionFor(this.team);

        this.team.recruitMember(this.applicant, role);
        this.status = JoinWaitingStatus.APPROVED;
        this.decisionReason = decisionReason;
        this.decidedBy = approver.getUser();
        this.decidedAt = LocalDateTime.now();
    }

    public void reject(TeamMember approver, String decisionReason) {
        verifyPending();
        approver.canMakeJoinDecisionFor(this.team);

        this.status = JoinWaitingStatus.REJECTED;
        this.decisionReason = decisionReason;
        this.decidedBy = approver.getUser();
        this.decidedAt = LocalDateTime.now();
    }

    public void cancel(User requester, String decisionReason) {
        verifyPending();

        if (!requester.equals(this.applicant)) {
            throw new NoPermissionException(ErrorCode.JOIN_REQUEST_OWNERSHIP_VIOLATION);
        }

        this.status = JoinWaitingStatus.CANCELED;
        this.decisionReason = decisionReason;
        this.decidedBy = requester;
        this.decidedAt = LocalDateTime.now();
    }

    private void verifyPending() {
        if (!this.status.isPending()) {
            throw new JoinWaitingNotPendingException();
        }
    }
}
