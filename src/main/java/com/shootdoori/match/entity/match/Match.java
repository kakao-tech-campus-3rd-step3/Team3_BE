package com.shootdoori.match.entity.match;

import com.shootdoori.match.entity.common.DateEntity;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.exception.domain.review.MatchNotFinishedException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "match_table")
public class Match extends DateEntity {

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

    public Team findEnemyTeam(Team myTeam) {
        if (myTeam.equals(this.getMatchCreateTeam())) {
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
}
