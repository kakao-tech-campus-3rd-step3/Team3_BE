package com.shootdoori.match.dto;

import com.shootdoori.match.entity.Match;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.Venue;
import com.shootdoori.match.entity.MatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RecentMatchesResponseDto(
    Integer matchId,
    Team team1,
    Team team2,
    LocalDate matchDate,
    LocalTime matchTime,
    Venue venue,
    MatchStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
  public static RecentMatchesResponseDto from(Match match) {
    return new RecentMatchesResponseDto(
        match.getMatchId(),
        match.getTeam1(),
        match.getTeam2(),
        match.getMatchDate(),
        match.getMatchTime(),
        match.getVenue(),
        match.getStatus(),
        match.getCreatedAt(),
        match.getUpdatedAt()
    );
  }
}
