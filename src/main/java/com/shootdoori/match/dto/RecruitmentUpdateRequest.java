package com.shootdoori.match.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RecruitmentUpdateRequest(
    LocalDate matchDate,
    LocalTime matchTime,
    String message,
    String position,
    String skillLevel
) {
}
