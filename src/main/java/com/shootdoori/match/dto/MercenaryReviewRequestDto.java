package com.shootdoori.match.dto;

import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;

public record MercenaryReviewRequestDto(Long matchId,
                                        Long reviewerTeamId,
                                        Long userId,
                                        Integer rating,
                                        ReviewBinaryEvaluation punctualityReview,
                                        ReviewBinaryEvaluation sportsmanshipReview,
                                        ReviewSkillLevel skillLevelReview) {
}
