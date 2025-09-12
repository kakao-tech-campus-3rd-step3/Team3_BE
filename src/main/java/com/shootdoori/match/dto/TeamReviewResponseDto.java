package com.shootdoori.match.dto;

public record TeamReviewResponseDto(Long teamReviewId,
                                    Integer matchId,
                                    Long reviewerTeamId,
                                    Long reviewedTeamId,
                                    Integer rating,
                                    String comment,
                                    Integer punctualityRating,
                                    Integer sportsmanshipRating) {
}
