package com.shootdoori.match.entity.match.request;

import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.team.Team;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_request")
@EntityListeners(AuditingEntityListener.class)
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REQUEST_ID")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WAITING_ID", nullable = false)
    private MatchWaiting matchWaiting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_TEAM_ID", nullable = false)
    private Team requestTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_TEAM_ID", nullable = false)
    private Team targetTeam;

    @Column(name = "REQUEST_MESSAGE", columnDefinition = "TEXT")
    private String requestMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '대기중'")
    private MatchRequestStatus status = MatchRequestStatus.PENDING;

    @CreatedDate
    @Column(name = "REQUEST_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime requestAt;

    @Column(name = "RESPONDED_AT")
    private LocalDateTime respondedAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    protected MatchRequest() {
    }

    public MatchRequest(MatchWaiting matchWaiting, Team requestTeam, Team targetTeam, String requestMessage) {
        this.matchWaiting = matchWaiting;
        this.requestTeam = requestTeam;
        this.targetTeam = targetTeam;
        this.requestMessage = requestMessage;
        this.status = MatchRequestStatus.PENDING;
        this.requestAt = LocalDateTime.now();
        this.respondedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getRequestId() {
        return requestId;
    }

    public Team getRequestTeam() {
        return requestTeam;
    }

    public Team getTargetTeam() {
        return targetTeam;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public MatchRequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestAt() {
        return requestAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public MatchWaiting getMatchWaiting() {
        return matchWaiting;
    }

    public void updateRequestStatus(MatchRequestStatus status, LocalDateTime respondedAt) {
        this.status = status;
        this.respondedAt = respondedAt;
    }

    public void cancelRequest() {
        this.status = MatchRequestStatus.CANCELED;
    }
}
