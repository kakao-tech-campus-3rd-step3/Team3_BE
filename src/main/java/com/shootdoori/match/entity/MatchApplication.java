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
public class MatchApplication extends DateEntity {

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

  @Column(name = "RESPONDED_AT")
  private LocalDateTime respondedAt;

  protected MatchApplication() {
  }

  public MatchApplication(MatchQueue matchQueue, Team applicantTeam, Team targetTeam, String applicationMessage) {
      this.matchQueue = matchQueue;
      this.applicantTeam = applicantTeam;
      this.targetTeam = targetTeam;
      this.applicationMessage = applicationMessage;
      this.status = MatchApplicationStatus.PENDING;
      this.respondedAt = null;
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

  public LocalDateTime getRespondedAt() {return respondedAt;}

  public MatchQueue getMatchQueue(){ return matchQueue; }

  public void updateApplicationStatus(MatchApplicationStatus status, LocalDateTime respondedAt){
    this.status = status;
    this.respondedAt = respondedAt;
  }

  public void cancelApplication(){
    this.status = MatchApplicationStatus.CANCELED;
  }
}
