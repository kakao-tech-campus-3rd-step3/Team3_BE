package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchQueueStatus;
import java.time.LocalDateTime;

public record MatchCreateResponseDto(
    Long waitingId,
    Long teamId,
    MatchQueueStatus status,
    LocalDateTime expiresAt
) {
}
