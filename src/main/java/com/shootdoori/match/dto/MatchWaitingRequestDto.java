package com.shootdoori.match.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchWaitingRequestDto(
  Long teamId,
  LocalDate selectDate,
  LocalTime startTime
) {}
