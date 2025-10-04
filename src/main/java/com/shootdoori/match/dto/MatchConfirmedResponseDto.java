package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchConfirmedResponseDto(
    Long matchId,
    Long team1Id,
    TeamName team1Name,
    Long team2Id,
    TeamName team2Name,
    LocalDate matchDate,
    LocalTime matchTime,
    Long venueId,
    MatchStatus status
) {
}
