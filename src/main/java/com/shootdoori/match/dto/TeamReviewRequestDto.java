package com.shootdoori.match.dto;

public record TeamReviewRequestDto(Integer matchId,
                                   Long reviewerTeamId,
                                   Long reviewedTeamId,
                                   Integer rating,
                                   String comment,
                                   Integer punctualityRating,
                                   Integer sportsmanshipRating) {
}
