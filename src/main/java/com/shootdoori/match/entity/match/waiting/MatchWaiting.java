package com.shootdoori.match.entity.match.waiting;

import com.shootdoori.match.entity.common.DateEntity;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.value.TeamName;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "match_waiting")
public class MatchWaiting extends DateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WAITING_ID")
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
    private MatchWaitingStatus status = MatchWaitingStatus.WAITING;

    @Column(name = "EXPIRES_AT", nullable = false, columnDefinition = "TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '24' HOUR)")
    private LocalDateTime expiresAt;


    protected MatchWaiting() {
    }


  public MatchWaiting(Team team,
                      LocalDate preferredDate,
                      LocalTime preferredTimeStart,
                      LocalTime preferredTimeEnd,
                      Venue preferredVenue,
                      SkillLevel skillLevelMin,
                      SkillLevel skillLevelMax,
                      Boolean universityOnly,
                      String message,
                      MatchWaitingStatus status,
                      LocalDateTime expiresAt
                      ) {
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

    public MatchWaitingStatus getMatchWaitingStatus() {
        return status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void updateWaitingStatus(MatchWaitingStatus status) {
        this.status = status;
    }

    public void cancelMatchWaiting() {
        this.status = MatchWaitingStatus.CANCELED;
    }

    public boolean belongTo(TeamMember teamMember){
        return this.team.equals(teamMember.getTeam());
    }

    public Long getTeamId(){
        return this.team.getTeamId();
    }

    public TeamName getTeamName(){
        return this.team.getTeamName();
    }

    public Long getVenueId(){
        return this.preferredVenue.getVenueId();
    }

}
