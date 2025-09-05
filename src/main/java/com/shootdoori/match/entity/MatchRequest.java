package com.shootdoori.match.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_requests")
public class MatchRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "REQUEST_ID")
  private Integer requestId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REQUESTING_TEAM_ID", nullable = false)
  private Team requestingTeam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TARGET_TEAM_ID", nullable = false)
  private Team targetTeam;

  @Column(name = "REQUEST_MESSAGE", columnDefinition = "TEXT")
  private String requestMessage;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '대기중'")
  private MatchRequestStatus status = MatchRequestStatus.대기중;

  @Column(name = "REQUESTED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime requestedAt;

  @Column(name = "RESPONDED_AT")
  private LocalDateTime respondedAt;

  @Column(name = "UPDATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  protected MatchRequest() {}

  public Integer getRequestId() {
    return requestId;
  }

  public Team getRequestingTeam() {
    return requestingTeam;
  }

  public Team getTargetTeam() {
    return targetTeam;
  }

  public String getRequestMessage() {
    return requestMessage;
  }

  public MatchRequestStatus getMatchRequestStatus() {
    return status;
  }

  public LocalDateTime getRequestedAt() {
    return requestedAt;
  }

  public LocalDateTime getRespondedAt() {
    return respondedAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
