package com.shootdoori.match.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_application")
@EntityListeners(AuditingEntityListener.class)
public class MatchApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "APPLICATION_ID")
  private Long applicationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "QUEUE_ID", nullable = false)
  private MatchQueue matchQueue;

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

  @CreatedDate
  @Column(name = "APPLIED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime appliedAt;

  @Column(name = "RESPONDED_AT")
  private LocalDateTime respondedAt;

  @LastModifiedDate
  @Column(name = "UPDATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  protected MatchApplication() {
  }

  public MatchApplication(MatchQueue matchQueue, Team applicantTeam, Team targetTeam, String applicationMessage) {
      this.matchQueue = matchQueue;
      this.applicantTeam = applicantTeam;
      this.targetTeam = targetTeam;
      this.applicationMessage = applicationMessage;
      this.status = MatchApplicationStatus.PENDING;
      this.appliedAt = LocalDateTime.now();
      this.respondedAt = null;
      this.updatedAt = LocalDateTime.now();
  }

  public Long getApplicationId() {
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

  public LocalDateTime getRespondedAt() {return respondedAt;}

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public MatchQueue getMatchQueue(){ return matchQueue; }

  public void updateApplicationStatus(MatchApplicationStatus status, LocalDateTime respondedAt){
    this.status = status;
    this.respondedAt = respondedAt;
  }

  public void cancelApplication(){
    this.status = MatchApplicationStatus.CANCELED;
  }
}
