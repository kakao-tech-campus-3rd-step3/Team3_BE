package com.shootdoori.match.entity.lineup;

import com.shootdoori.match.entity.common.AuditInfo;
import com.shootdoori.match.entity.common.Position;
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
@Table(name = "lineup_member")
public class LineupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lineup_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Position position;

    @Column(name = "is_starter")
    private Boolean isStarter = true;

    @Embedded
    private AuditInfo audit = new AuditInfo();


    public LineupMember(TeamMember teamMember,
                        Position position,
                        Boolean isStarter) {
        this.teamMember = teamMember;
        this.position = position;
        this.isStarter = isStarter;
    }

    protected LineupMember() {
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return audit.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return audit.getUpdatedAt();
    }

    public AuditInfo getAudit() {
        return audit;
    }

    public void update(Position position,
                       Boolean isStarter) {
        this.position = position;
        this.isStarter = isStarter;
    }
}
