package com.shootdoori.match.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "match_queue")
public class MatchQueue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "QUEUE_ID")
  private Long waitingId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM_ID", nullable = false)
  private Team team;

  @Column(name = "PREFERRED_DATE", nullable = false)
  private LocalDate preferredDate;

  @Column(name = "PREFERRED_TIME_START", nullable = false)
  private LocalTime preferredTimeStart;

  @Column(name = "PREFERRED_TIME_END", nullable = false)
  private LocalTime preferredTimeEnd;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PREFERRED_VENUE_ID", nullable = false)
  private Venue preferredVenue;

  @Enumerated(EnumType.STRING)
  @Column(name = "SKILL_LEVEL_MIN", nullable = false)
  private SkillLevel skillLevelMin;

  @Enumerated(EnumType.STRING)
  @Column(name = "SKILL_LEVEL_MAX", nullable = false)
  private SkillLevel skillLevelMax;

  @Column(name = "UNIVERSITY_ONLY", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
  private Boolean universityOnly = false;

  @Column(name = "MESSAGE", columnDefinition = "TEXT")
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '대기중'")
  private MatchQueueStatus status = MatchQueueStatus.WAITING;

  @Column(name = "EXPIRES_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '24' HOUR)")
  private LocalDateTime expiresAt;

  @Column(name = "CREATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  protected MatchQueue() {}


  public MatchQueue(Team team,
      LocalDate preferredDate,
      LocalTime preferredTimeStart,
      LocalTime preferredTimeEnd,
      Venue preferredVenue,
      SkillLevel skillLevelMin,
      SkillLevel skillLevelMax,
      Boolean universityOnly,
      String message,
      MatchQueueStatus status,
      LocalDateTime expiresAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.team = team;
    this.preferredDate = preferredDate;
    this.preferredTimeStart = preferredTimeStart;
    this.preferredTimeEnd = preferredTimeEnd;
    this.preferredVenue = preferredVenue;
    this.skillLevelMin = skillLevelMin;
    this.skillLevelMax = skillLevelMax;
    this.universityOnly = universityOnly;
    this.message = message;
    this.status = status;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getWaitingId() {
    return waitingId;
  }

  public Team getTeam() {
    return team;
  }

  public LocalDate getPreferredDate() {
    return preferredDate;
  }

  public LocalTime getPreferredTimeStart() {
    return preferredTimeStart;
  }

  public LocalTime getPreferredTimeEnd() {
    return preferredTimeEnd;
  }

  public Venue getPreferredVenue() {
    return preferredVenue;
  }

  public SkillLevel getSkillLevelMin() {
    return skillLevelMin;
  }

  public SkillLevel getSkillLevelMax() {
    return skillLevelMax;
  }

  public Boolean getUniversityOnly() {
    return universityOnly;
  }

  public String getMessage() {
    return message;
  }

  public MatchQueueStatus getMatchRequestStatus() {
    return status;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void updateQueueStatus(MatchQueueStatus status, LocalDateTime updatedAt){
    this.status = status;
    this.updatedAt = updatedAt;
  }
}
