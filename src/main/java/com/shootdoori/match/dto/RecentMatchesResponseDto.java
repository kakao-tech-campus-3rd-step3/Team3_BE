package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.MatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RecentMatchesResponseDto(
    Long matchId,
    String team1Name,
    String team2Name,
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
      matchSummaryProjection.team1Name(),
      matchSummaryProjection.team2Name(),
      matchSummaryProjection.matchDate(),
      matchSummaryProjection.matchTime(),
      matchSummaryProjection.venueName(),
      matchSummaryProjection.status(),
      matchSummaryProjection.createdAt(),
      matchSummaryProjection.updatedAt()
    );
  }
}
