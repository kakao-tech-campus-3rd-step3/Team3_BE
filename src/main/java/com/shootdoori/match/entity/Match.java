package com.shootdoori.match.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "match_table")
public class Match {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "MATCH_ID")
  private Long matchId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM1_ID", nullable = false)
  private Team team1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM2_ID", nullable = false)
  private Team team2;

  @Column(name = "MATCH_DATE", nullable = false)
  private LocalDate matchDate;

  @Column(name = "MATCH_TIME", nullable = false)
  private LocalTime matchTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VENUE_ID", nullable = false)
  private Venue venue;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '예정'")
  private MatchStatus status = MatchStatus.RECRUITING;

  @Column(name = "CREATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "UPDATED_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  public Match(Team team1, Team team2, LocalDate matchDate, LocalTime matchTime, Venue venue, MatchStatus status) {
    this.team1 = team1;
    this.team2 = team2;
    this.matchDate = matchDate;
    this.matchTime = matchTime;
    this.venue = venue;
    this.status = status != null ? status : MatchStatus.RECRUITING;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  protected Match() {}

  public Long getMatchId() {
    return matchId;
  }

  public Team getTeam1() {
    return team1;
  }

  public Team getTeam2() {
    return team2;
  }

  public LocalDate getMatchDate() {
    return matchDate;
  }

  public LocalTime getMatchTime() {
    return matchTime;
  }

  public Venue getVenue() {
    return venue;
  }

  public MatchStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public Team findEnemyTeam(Long teamId) {
      if(Objects.equals(teamId, team1.getTeamId())) {
          return team2;
      }
      else {
          return team1;
      }
  }
}
