package com.shootdoori.match.entity.lineup;

import com.shootdoori.match.entity.common.AuditInfo;
import com.shootdoori.match.entity.team.Team;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "lineup")
public class Lineup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lineup_id")
    private Long id;

    @Embedded
    private AuditInfo audit = new AuditInfo();

    public Lineup() {}

    public Long getId() {
        return id;
    }

    public AuditInfo getAudit() {
        return audit;
    }

    public LocalDateTime getCreatedAt() {
        return audit.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return audit.getUpdatedAt();
    }
}
