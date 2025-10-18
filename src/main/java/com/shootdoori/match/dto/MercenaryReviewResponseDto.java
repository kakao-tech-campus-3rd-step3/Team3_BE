package com.shootdoori.match.dto;

import com.shootdoori.match.entity.review.MercenaryReview;
import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;

public record MercenaryReviewResponseDto(Long mercenaryReviewId,
                                         Long matchId,
                                         Long reviewerTeamId,
                                         Long userId,
                                         Integer rating,
                                         ReviewBinaryEvaluation punctualityReview,
                                         ReviewBinaryEvaluation sportsmanshipReview,
                                         ReviewSkillLevel skillLevelReview) {

    public static MercenaryReviewResponseDto from(MercenaryReview review) {
        return new MercenaryReviewResponseDto(
                review.getId(),
                review.getMatch().getMatchId(),
                review.getReviewerTeam().getTeamId(),
                review.getMercenaryUser().getId(),
                review.getRating(),
                review.getPunctualityReview(),
                review.getSportsmanshipReview(),
                review.getSkillLevelReview()
        );
    }
}
