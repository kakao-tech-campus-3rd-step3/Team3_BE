package com.shootdoori.match.entity.review;

import com.shootdoori.match.entity.common.DateEntity;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.team.Team;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class TeamReview extends DateEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "punctuality_review")
    private ReviewBinaryEvaluation punctualityReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "sportsmanship_review")
    private ReviewBinaryEvaluation sportsmanshipReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level_review")
    private ReviewSkillLevel skillLevelReview;

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
}