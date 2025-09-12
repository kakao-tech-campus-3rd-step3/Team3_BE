package com.shootdoori.match.entity;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "match_application")
public class MatchApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "APPLICATION_ID")
  private Integer applicationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "APPLICANT_TEAM_ID", nullable = false)
  private Team applicantTeam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TARGET_TEAM_ID", nullable = false)
  private Team targetTeam;

  @Column(name = "APPLICATION_MESSAGE", columnDefinition = "TEXT")
  private String applicationMessage;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '대기중'")
  private MatchApplicationStatus status = MatchApplicationStatus.PENDING;

  @Column(name = "APPLIED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime appliedAt;

  @Column(name = "RESPONDED_AT")
  private LocalDateTime respondedAt;

  @Column(name = "UPDATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  protected MatchApplication() {
  }

  public MatchApplication(Team applicantTeam,
      Team targetTeam,
      String applicationMessage,
      MatchApplicationStatus status,
      LocalDateTime appliedAt,
      LocalDateTime respondedAt,
      LocalDateTime updatedAt) {
    this.applicantTeam = applicantTeam;
    this.targetTeam = targetTeam;
    this.applicationMessage = applicationMessage;
    this.status = status;
    this.appliedAt = appliedAt;
    this.respondedAt = respondedAt;
    this.updatedAt = updatedAt;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public Team getApplicantTeam() {
    return applicantTeam;
  }

  public Team getTargetTeam() {
    return targetTeam;
  }

  public String getApplicationMessage() {
    return applicationMessage;
  }

  public MatchApplicationStatus getStatus() {
    return status;
  }

  public LocalDateTime getAppliedAt() {
    return appliedAt;
  }

  public LocalDateTime getRespondedAt() {
    return respondedAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
