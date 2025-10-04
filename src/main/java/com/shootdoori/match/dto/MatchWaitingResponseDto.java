package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchWaiting;
import com.shootdoori.match.entity.MatchWaitingStatus;
import com.shootdoori.match.entity.MatchWaitingSkillLevel;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MatchWaitingResponseDto(
  Long waitingId,
  Long teamId,
  TeamName teamName,
  LocalDate preferredDate,
  LocalTime preferredTimeStart,
  LocalTime preferredTimeEnd,
  Long preferredVenueId,
  MatchWaitingSkillLevel skillLevelMin,
  MatchWaitingSkillLevel skillLevelMax,
  Boolean universityOnly,
  String message,
  MatchWaitingStatus status,
  LocalDateTime expiresAt
) {
  public static MatchWaitingResponseDto from(MatchWaiting mw) {
    return new MatchWaitingResponseDto(
      mw.getWaitingId(),
      mw.getTeam().getTeamId(),
      mw.getTeam().getTeamName(),
      mw.getPreferredDate(),
      mw.getPreferredTimeStart(),
      mw.getPreferredTimeEnd(),
      mw.getPreferredVenue().getVenueId(),
      mw.getSkillLevelMin(),
      mw.getSkillLevelMax(),
      mw.getUniversityOnly(),
      mw.getMessage(),
      mw.getMatchWaitingStatus(),
      mw.getExpiresAt()
    );
  }
}
