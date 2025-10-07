package com.shootdoori.match.entity.match;

import com.shootdoori.match.entity.common.DateEntity;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.venue.Venue;
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

    public Match(Team team1, Team team2, LocalDate matchDate, LocalTime matchTime, Venue venue,
                 MatchStatus status) {
        this.team1 = team1;
        this.team2 = team2;
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

    public Team findEnemyTeam(Team myTeam) {
        if (myTeam.equals(this.getTeam1())) {
            return this.getTeam2();
        }
        return this.getTeam1();
    }
}
