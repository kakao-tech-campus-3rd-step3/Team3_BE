package com.shootdoori.match.dto;

import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;
import com.shootdoori.match.entity.review.TeamReview;

public record TeamReviewResponseDto(Long teamReviewId,
                                    Long matchId,
                                    Long reviewerTeamId,
                                    Long reviewedTeamId,
                                    Integer rating,
                                    ReviewBinaryEvaluation punctualityReview,
                                    ReviewBinaryEvaluation sportsmanshipReview,
                                    ReviewSkillLevel skillLevelReview) {

    public static TeamReviewResponseDto from(TeamReview review) {
        return new TeamReviewResponseDto(
                review.getId(),
                review.getMatch().getMatchId(),
                review.getReviewerTeam().getTeamId(),
                review.getReviewedTeam().getTeamId(),
                review.getRating(),
                review.getPunctualityReview(),
                review.getSportsmanshipReview(),
                review.getSkillLevelReview()
        );
    }
}
