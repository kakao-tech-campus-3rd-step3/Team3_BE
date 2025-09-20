package com.shootdoori.match.entity;

import com.shootdoori.match.exception.JoinQueueNotPendingException;
import com.shootdoori.match.exception.NoPermissionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "join_queue",
    indexes = {
        @Index(name = "idx_join_queue_team_status", columnList = "team_id,status")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class JoinQueue {

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
    private JoinQueueStatus status = JoinQueueStatus.PENDING;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    public JoinQueueStatus getStatus() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    protected JoinQueue() {

    }

    public JoinQueue(Team team, User applicant, String message) {
        this.team = team;
        this.applicant = applicant;
        this.message = message;
    }

    public static JoinQueue create(Team team, User applicant, String message) {
        return new JoinQueue(team, applicant, message);
    }

    public void approve(TeamMember approver, TeamMemberRole role, String decisionReason) {
        verifyPending();
        approver.canMakeJoinDecisionFor(this.team);

        this.team.recruitMember(this.applicant, role);
        this.status = JoinQueueStatus.APPROVED;
        this.decisionReason = decisionReason;
        this.decidedBy = approver.getUser();
        this.decidedAt = LocalDateTime.now();
    }

    public void reject(TeamMember approver, String decisionReason) {
        verifyPending();
        approver.canMakeJoinDecisionFor(this.team);

        this.status = JoinQueueStatus.REJECTED;
        this.decisionReason = decisionReason;
        this.decidedBy = approver.getUser();
        this.decidedAt = LocalDateTime.now();
    }

    public void cancel(User requester, String decisionReason) {
        verifyPending();

        if (!requester.equals(this.applicant)) {
            throw new NoPermissionException();
        }

        this.status = JoinQueueStatus.CANCELED;
        this.decisionReason = decisionReason;
        this.decidedBy = requester;
        this.decidedAt = LocalDateTime.now();
    }

    private void verifyPending() {
        if (!this.status.isPending()) {
            throw new JoinQueueNotPendingException();
        }
    }
}
