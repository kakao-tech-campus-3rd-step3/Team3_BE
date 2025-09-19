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
    constraints = "rating BETWEEN 1 AND 5 "
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

    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "punctuality_review")
    private ReviewBinaryEvaluation punctualityReview;

    @Column(name = "sportsmanship_review")
    private ReviewBinaryEvaluation sportsmanshipReview;

    @Column(name = "skill_level_review")
    private ReviewSkillLevel skillLevelReview;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected MercenaryReview() {
    }

    public MercenaryReview(Match match,
                           Team reviewerTeam,
                           User mercenaryUser,
                           Integer rating,
                           ReviewBinaryEvaluation punctualityReview,
                           ReviewBinaryEvaluation sportsmanshipReview,
                           ReviewSkillLevel skillLevelReview) {
        this.match = match;
        this.reviewerTeam = reviewerTeam;
        this.mercenaryUser = mercenaryUser;
        this.rating = rating;
        this.punctualityReview = punctualityReview;
        this.sportsmanshipReview = sportsmanshipReview;
        this.skillLevelReview = skillLevelReview;
    }

    public void update(MercenaryReview mercenaryReview) {
        this.match = mercenaryReview.match;
        this.reviewerTeam = mercenaryReview.reviewerTeam;
        this.mercenaryUser = mercenaryReview.mercenaryUser;
        this.rating = mercenaryReview.rating;
        this.punctualityReview = mercenaryReview.punctualityReview;
        this.sportsmanshipReview = mercenaryReview.sportsmanshipReview;
        this.skillLevelReview = mercenaryReview.skillLevelReview;
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

    public Integer getRating() {
        return rating;
    }

    public ReviewBinaryEvaluation getPunctualityReview() {
        return punctualityReview;
    }

    public ReviewBinaryEvaluation getSportsmanshipReview() {
        return sportsmanshipReview;
    }

    public ReviewSkillLevel getSkillLevelReview() {
        return skillLevelReview;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}