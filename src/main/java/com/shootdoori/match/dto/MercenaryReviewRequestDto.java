package com.shootdoori.match.dto;

import com.shootdoori.match.entity.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.ReviewSkillLevel;

public record MercenaryReviewRequestDto(Long matchId,
                                        Long reviewerTeamId,
                                        Long userId,
                                        Integer rating,
                                        ReviewBinaryEvaluation punctualityReview,
                                        ReviewBinaryEvaluation sportsmanshipReview,
                                        ReviewSkillLevel skillLevelReview) {
}
