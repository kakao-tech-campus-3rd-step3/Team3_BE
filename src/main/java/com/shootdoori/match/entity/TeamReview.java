package com.shootdoori.match.entity;

import com.shootdoori.match.dto.TeamReviewRequestDto;
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
    name = "team_review",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"match_id", "reviewer_team_id"})
    }
)
@Check(
    name = "ck_team_review_ratings",
    constraints = "rating BETWEEN 1 AND 5 "
)
public class TeamReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_team_id", nullable = false)
    private Team reviewerTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewed_team_id", nullable = false)
    private Team reviewedTeam;

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

    protected TeamReview() {
    }

    public TeamReview(Match match,
        Team reviewerTeam,
        Team reviewedTeam,
        Integer rating,
        ReviewBinaryEvaluation punctualityReview,
        ReviewBinaryEvaluation sportsmanshipReview,
        ReviewSkillLevel skillLevelReview) {
        this.match = match;
        this.reviewerTeam = reviewerTeam;
        this.reviewedTeam = reviewedTeam;
        this.rating = rating;
        this.punctualityReview = punctualityReview;
        this.sportsmanshipReview = sportsmanshipReview;
        this.skillLevelReview = skillLevelReview;
    }

    public static TeamReview from(Match match,
                                  Team reviewerTeam,
                                  Team reviewedTeam,
                                  Integer rating,
                                  ReviewBinaryEvaluation punctualityReview,
                                  ReviewBinaryEvaluation sportsmanshipReview,
                                  ReviewSkillLevel skillLevelReview) {
        return new TeamReview(
                match,
                reviewerTeam,
                reviewedTeam,
                rating,
                punctualityReview,
                sportsmanshipReview,
                skillLevelReview
        );
    }

    public void update(TeamReview teamReview) {
        this.match = teamReview.match;
        this.reviewerTeam = teamReview.reviewerTeam;
        this.reviewedTeam = teamReview.reviewedTeam;
        this.rating = teamReview.rating;
        this.punctualityReview = teamReview.punctualityReview;
        this.sportsmanshipReview = teamReview.sportsmanshipReview;
        this.skillLevelReview = teamReview.skillLevelReview;
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

    public Team getReviewedTeam() {
        return reviewedTeam;
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