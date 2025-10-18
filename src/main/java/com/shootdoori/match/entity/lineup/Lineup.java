package com.shootdoori.match.entity.lineup;

import com.shootdoori.match.entity.common.DateEntity;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.user.UserPosition;
import jakarta.persistence.*;

@Entity
@Table(name = "lineup")
public class Lineup extends DateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lineup_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiting_id")
    private MatchWaiting waiting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private MatchRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private UserPosition position;

    @Column(name = "is_starter")
    private Boolean isStarter = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "lineup_status", nullable = false)
    private LineupStatus lineupStatus = LineupStatus.CREATED;

    public Lineup(Match match,
                  MatchWaiting waiting,
                  MatchRequest request,
                  TeamMember teamMember,
                  UserPosition position,
                  Boolean isStarter) {
        this.match = match;
        this.waiting = waiting;
        this.request = request;
        this.teamMember = teamMember;
        this.position = position;
        this.isStarter = isStarter;
    }

    protected Lineup() {
    }

    public Long getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public MatchWaiting getWaiting() {
        return waiting;
    }

    public MatchRequest getRequest() {
        return request;
    }

    public TeamMember getTeamMember() {
        return teamMember;
    }

    public UserPosition getPosition() {
        return position;
    }

    public Boolean getIsStarter() {
        return isStarter;
    }

    public LineupStatus getLineupStatus() {
        return lineupStatus;
    }

    public void toCreated() {
        this.lineupStatus = LineupStatus.CREATED;
    }

    public void toRequested() {
        this.lineupStatus = LineupStatus.REQUESTED;
    }

    public void toMatched() {
        this.lineupStatus = LineupStatus.MATCHED;
    }

    public void update(Match match,
                       MatchWaiting waiting,
                       MatchRequest request,
                       UserPosition position,
                       Boolean isStarter) {
        this.match = match;
        this.waiting = waiting;
        this.request = request;
        this.position = position;
        this.isStarter = isStarter;
    }
}
