package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchWaitingStatus;
import java.time.LocalDateTime;

public record MatchCreateResponseDto(
    Long waitingId,
    Long teamId,
    MatchWaitingStatus status,
    LocalDateTime expiresAt
) {
}
