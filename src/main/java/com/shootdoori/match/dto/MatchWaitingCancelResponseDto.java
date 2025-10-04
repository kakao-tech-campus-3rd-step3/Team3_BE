package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchWaitingStatus;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDateTime;

public record MatchWaitingCancelResponseDto(
    Long waitingId,
    Long teamId,
    TeamName teamName,
    MatchWaitingStatus status,
    LocalDateTime expiresAt
) {
}
