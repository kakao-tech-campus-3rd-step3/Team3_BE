package com.shootdoori.match.entity.lineup;

import com.shootdoori.match.entity.common.AuditInfo;
import com.shootdoori.match.entity.common.Position;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.team.TeamMember;
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
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "lineup")
public class Lineup {

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
    private Position position;

    @Column(name = "is_starter")
    private Boolean isStarter = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "lineup_status", nullable = false)
    private LineupStatus lineupStatus = LineupStatus.CREATED;

    @Embedded
    private AuditInfo audit = new AuditInfo();


    public Lineup(Match match,
                  MatchWaiting waiting,
                  MatchRequest request,
                  TeamMember teamMember,
                  Position position,
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

    public Position getPosition() {
        return position;
    }

    public Boolean getIsStarter() {
        return isStarter;
    }

    public LineupStatus getLineupStatus() {
        return lineupStatus;
    }

    public LocalDateTime getCreatedAt() {
        return audit.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return audit.getUpdatedAt();
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
                       Position position,
                       Boolean isStarter) {
        this.match = match;
        this.waiting = waiting;
        this.request = request;
        this.position = position;
        this.isStarter = isStarter;
    }
}
