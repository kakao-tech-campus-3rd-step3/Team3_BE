package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDateTime;

public record MatchWaitingCancelResponseDto(
    Long waitingId,
    Long teamId,
    TeamName teamName,
    MatchWaitingStatus status,
    LocalDateTime expiresAt
) {
    public static MatchWaitingCancelResponseDto from(MatchWaiting matchWaiting) {
        return new MatchWaitingCancelResponseDto(
            matchWaiting.getWaitingId(),
            matchWaiting.getTeamId(),
            matchWaiting.getTeamName(),
            matchWaiting.getMatchWaitingStatus(),
            matchWaiting.getExpiresAt()
        );
    }
}
