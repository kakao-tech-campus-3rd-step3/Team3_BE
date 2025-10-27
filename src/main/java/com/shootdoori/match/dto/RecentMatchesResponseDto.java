package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.MatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RecentMatchesResponseDto(
    Long matchId,
    Long createTeamId,
    Long requestTeamId,
    String createTeamName,
    String requestTeamName,
    LocalDate matchDate,
    LocalTime matchTime,
    String venueName,
    MatchStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static RecentMatchesResponseDto from(MatchSummaryProjection matchSummaryProjection) {
        return new RecentMatchesResponseDto(
            matchSummaryProjection.matchId(),
            matchSummaryProjection.createTeamId(),
            matchSummaryProjection.requestTeamId(),
            matchSummaryProjection.createTeamName(),
            matchSummaryProjection.requestTeamName(),
            matchSummaryProjection.matchDate(),
            matchSummaryProjection.matchTime(),
            matchSummaryProjection.venueName(),
            matchSummaryProjection.status(),
            matchSummaryProjection.createdAt(),
            matchSummaryProjection.updatedAt()
        );
    }
}
