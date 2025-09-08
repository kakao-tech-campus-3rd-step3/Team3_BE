package com.shootdoori.match.entity;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "TEAM_NAME", nullable = false, length = 100)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CAPTAIN_ID", nullable = false)
    private User captain;

    @Column(name = "UNIVERSITY", nullable = false, length = 100)
    private String university;

    @Enumerated(EnumType.STRING)
    @Column(name = "TEAM_TYPE", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '동아리'")
    private TeamType teamType = TeamType.OTHER;

    @Column(name = "MEMBER_COUNT", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer memberCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "SKILL_LEVEL", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '아마추어'")
    private SkillLevel skillLevel = SkillLevel.아마추어;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    protected Team() {
    }

    public Team(String teamName, User captain, String university, TeamType teamType,
        Integer memberCount, SkillLevel skillLevel, String description) {
        this.teamName = teamName;
        this.captain = captain;
        this.university = university;
        this.teamType = teamType != null ? teamType : TeamType.OTHER;
        this.memberCount = (memberCount == null || memberCount < 0) ? 0 : memberCount;
        this.skillLevel = skillLevel != null ? skillLevel : SkillLevel.아마추어;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Long getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public User getCaptain() {
        return captain;
    }

    public String getUniversity() {
        return university;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
