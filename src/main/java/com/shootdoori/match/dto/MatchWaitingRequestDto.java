package com.shootdoori.match.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchWaitingRequestDto(
    LocalDate selectDate,
    LocalTime startTime
) {
}
