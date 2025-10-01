package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchWaitingStatus;
import com.shootdoori.match.entity.SkillLevel;
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
  SkillLevel skillLevelMin,
  SkillLevel skillLevelMax,
  Boolean universityOnly,
  String message,
  MatchWaitingStatus status,
  LocalDateTime expiresAt
) {}
