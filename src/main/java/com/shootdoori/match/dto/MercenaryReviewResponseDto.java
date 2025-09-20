package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MercenaryReview;
import com.shootdoori.match.entity.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.ReviewSkillLevel;

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
