package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDateTime;

public record MatchCreateResponseDto(
    Long waitingId,
    Long teamId,
    TeamName teamName,
    MatchWaitingStatus status,
    LocalDateTime expiresAt
) {
    public static MatchCreateResponseDto from(MatchWaiting matchWaiting) {
        return new MatchCreateResponseDto(
            matchWaiting.getWaitingId(),
            matchWaiting.getTeamId(),
            matchWaiting.getTeamName(),
            matchWaiting.getMatchWaitingStatus(),
            matchWaiting.getExpiresAt()
        );
    }
}
