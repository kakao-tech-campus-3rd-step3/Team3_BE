package com.shootdoori.match.dto;

import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;

public record TeamReviewRequestDto(Long matchId,
                                   Long reviewerTeamId,
                                   Long reviewedTeamId,
                                   Integer rating,
                                   ReviewBinaryEvaluation punctualityReview,
                                   ReviewBinaryEvaluation sportsmanshipReview,
                                   ReviewSkillLevel skillLevelReview) {
}
