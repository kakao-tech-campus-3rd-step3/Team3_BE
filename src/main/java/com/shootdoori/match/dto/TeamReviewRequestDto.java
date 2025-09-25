package com.shootdoori.match.dto;

import com.shootdoori.match.entity.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.ReviewSkillLevel;

public record TeamReviewRequestDto(Long matchId,
                                   Long reviewerTeamId,
                                   Long reviewedTeamId,
                                   Integer rating,
                                   ReviewBinaryEvaluation punctualityReview,
                                   ReviewBinaryEvaluation sportsmanshipReview,
                                   ReviewSkillLevel skillLevelReview) {
}
