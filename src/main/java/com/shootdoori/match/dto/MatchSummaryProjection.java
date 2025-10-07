package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MatchSummaryProjection(
  Long matchId,
  LocalDate matchDate,
  LocalTime matchTime,
  MatchStatus status,
  String team1Name,
  String team2Name,
  String venueName,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
