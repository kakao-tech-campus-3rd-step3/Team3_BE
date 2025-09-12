package com.shootdoori.match.dto;

import com.shootdoori.match.entity.TeamReview;

public record TeamReviewResponseDto(Long teamReviewId,
                                    Long matchId,
                                    Long reviewerTeamId,
                                    Long reviewedTeamId,
                                    Long rating,
                                    String comment,
                                    Long punctualityRating,
                                    Long sportsmanshipRating) {

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
