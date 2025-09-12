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
    name = "team_review",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"match_id", "reviewer_team_id"})
    }
)
@Check(
    name = "ck_team_review_ratings",
    constraints =
        "rating BETWEEN 1 AND 5 " +
            "AND (punctuality_rating IS NULL OR (punctuality_rating BETWEEN 1 AND 5)) " +
            "AND (sportsmanship_rating IS NULL OR (sportsmanship_rating BETWEEN 1 AND 5))"
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

    @Column(name = "comment", length = 500)
    private String comment;

    @Min(1)
    @Max(5)
    @Column(name = "punctuality_rating")
    private Integer punctualityRating;

    @Min(1)
    @Max(5)
    @Column(name = "sportsmanship_rating")
    private Integer sportsmanshipRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected TeamReview() {
    }

    public TeamReview(Match match,
        Team reviewerTeam,
        Team reviewedTeam,
        int rating,
        String comment,
        Integer punctualityRating,
        Integer sportsmanshipRating) {
        this.match = match;
        this.reviewerTeam = reviewerTeam;
        this.reviewedTeam = reviewedTeam;
        this.rating = rating;
        this.comment = comment;
        this.punctualityRating = punctualityRating;
        this.sportsmanshipRating = sportsmanshipRating;
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

    public String getComment() {
        return comment;
    }

    public Integer getPunctualityRating() {
        return punctualityRating;
    }

    public Integer getSportsmanshipRating() {
        return sportsmanshipRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}