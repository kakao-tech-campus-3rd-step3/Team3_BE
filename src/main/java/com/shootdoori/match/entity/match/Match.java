package com.shootdoori.match.entity.match;

import com.shootdoori.match.entity.common.AuditInfo;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.exception.domain.review.MatchNotFinishedException;
import com.shootdoori.match.value.TeamName;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "match_table")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MATCH_ID")
    private Long matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM1_ID", nullable = false)
    private Team matchCreateTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM2_ID", nullable = false)
    private Team matchRequestTeam;

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

    @Embedded
    private AuditInfo audit = new AuditInfo();

    public Match(Team matchCreateTeam, Team matchRequestTeam, LocalDate matchDate, LocalTime matchTime, Venue venue,
                 MatchStatus status) {
        this.matchCreateTeam = matchCreateTeam;
        this.matchRequestTeam = matchRequestTeam;
        this.matchDate = matchDate;
        this.matchTime = matchTime;
        this.venue = venue;
        this.status = status != null ? status : MatchStatus.RECRUITING;
    }

    protected Match() {
    }

    public Long getMatchId() {
        return matchId;
    }

    public Team getMatchCreateTeam() {
        return matchCreateTeam;
    }

    public Team getMatchRequestTeam() {
        return matchRequestTeam;
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
        return audit.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return audit.getUpdatedAt();
    }

    public Team findEnemyTeam(TeamMember teamMember) {
        if (teamMember.getTeam().equals(this.getMatchCreateTeam())) {
            return this.getMatchRequestTeam();
        }
        return this.getMatchCreateTeam();
    }

    public void updateStatus(MatchStatus matchStatus) {
        this.status = matchStatus;
    }

    public void validateMatchFinished() {
        if (this.status != MatchStatus.FINISHED) {
            throw new MatchNotFinishedException();
        }
    }

    public Long getCreateTeamId(){
        return this.matchCreateTeam.getTeamId();
    }

    public TeamName getCreateTeamName(){
        return this.matchCreateTeam.getTeamName();
    }

    public Long getRequestTeamId(){
        return this.matchRequestTeam.getTeamId();
    }

    public TeamName getRequestTeamName(){
        return this.matchRequestTeam.getTeamName();
    }
}
