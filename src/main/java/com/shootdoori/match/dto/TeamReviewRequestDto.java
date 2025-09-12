package com.shootdoori.match.dto;

public record TeamReviewRequestDto(Long matchId,
                                   Long reviewerTeamId,
                                   Long reviewedTeamId,
                                   Long rating,
                                   String comment,
                                   Long punctualityRating,
                                   Long sportsmanshipRating) {
}
