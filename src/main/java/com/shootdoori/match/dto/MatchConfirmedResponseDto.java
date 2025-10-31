package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.Match;
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
    MatchStatus status,
    Long lineup1Id,
    Long lineup2Id
) {
    public static MatchConfirmedResponseDto from(Match match) {
        return new MatchConfirmedResponseDto(
            match.getMatchId(),
            match.getCreateTeamId(),
            match.getCreateTeamName(),
            match.getRequestTeamId(),
            match.getRequestTeamName(),
            match.getMatchDate(),
            match.getMatchTime(),
            match.getVenue().getVenueId(),
            match.getStatus(),
            match.getCreateTeamLineupId(),
            match.getRequestTeamLineupId()
        );
    }
}
