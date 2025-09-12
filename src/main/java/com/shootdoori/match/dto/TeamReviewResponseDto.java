package com.shootdoori.match.dto;

import com.shootdoori.match.entity.TeamReview;

public record TeamReviewResponseDto(Long teamReviewId,
                                    Integer matchId,
                                    Long reviewerTeamId,
                                    Long reviewedTeamId,
                                    Integer rating,
                                    String comment,
                                    Integer punctualityRating,
                                    Integer sportsmanshipRating) {

    public static TeamReviewResponseDto from(TeamReview review) {
        return new TeamReviewResponseDto(
                review.getId(),
                review.getMatch().getMatchId(),
                review.getReviewerTeam().getTeamId(),
                review.getReviewedTeam().getTeamId(),
                review.getRating(),
                review.getComment(),
                review.getPunctualityRating(),
                review.getSportsmanshipRating()
        );
    }
}
