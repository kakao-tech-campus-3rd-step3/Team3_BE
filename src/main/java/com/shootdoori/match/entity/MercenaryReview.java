package com.shootdoori.match.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;

@Entity
@Table(
    name = "mercenary_review",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"match_id", "reviewer_team_id", "mercenary_user_id"})
    }
)
@Check(
    name = "ck_mercenary_review_ratings",
    constraints =
        "overall_rating BETWEEN 1 AND 5 " +
            "AND skill_rating BETWEEN 1 AND 5 " +
            "AND manner_rating BETWEEN 1 AND 5 " +
            "AND communication_rating BETWEEN 1 AND 5"
)
public class MercenaryReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mercenary_review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_team_id", nullable = false)
    private Team reviewerTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mercenary_user_id", nullable = false)
    private User mercenaryUser;

    @Column(name = "mercenary_name", nullable = false, length = 100)
    private String mercenaryName;

    @Min(1) @Max(5)
    @Column(name = "overall_rating", nullable = false)
    private int overallRating;

    @Min(1) @Max(5)
    @Column(name = "skill_rating", nullable = false)
    private int skillRating;

    @Min(1) @Max(5)
    @Column(name = "manner_rating", nullable = false)
    private int mannerRating;

    @Min(1) @Max(5)
    @Column(name = "communication_rating", nullable = false)
    private int communicationRating;

    @Column(name = "comment", length = 300)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected MercenaryReview() {
    }

    public MercenaryReview(Match match,
        Team reviewerTeam,
        User mercenaryUser,
        String mercenaryName,
        int overallRating,
        int skillRating,
        int mannerRating,
        int communicationRating,
        String comment) {
        this.match = match;
        this.reviewerTeam = reviewerTeam;
        this.mercenaryUser = mercenaryUser;
        this.mercenaryName = mercenaryName;
        this.overallRating = overallRating;
        this.skillRating = skillRating;
        this.mannerRating = mannerRating;
        this.communicationRating = communicationRating;
        this.comment = comment;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public Team getReviewerTeam() {
        return reviewerTeam;
    }

    public User getMercenaryUser() {
        return mercenaryUser;
    }

    public String getMercenaryName() {
        return mercenaryName;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public int getSkillRating() {
        return skillRating;
    }

    public int getMannerRating() {
        return mannerRating;
    }

    public int getCommunicationRating() {
        return communicationRating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}