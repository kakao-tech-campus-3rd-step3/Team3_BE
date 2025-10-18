package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDateTime;

public record MatchRequestHistoryResponseDto(
    Long requestId,
    Long requestTeamId,
    TeamName requestTeamName,
    Long targetTeamId,
    TeamName targetTeamName,
    String requestMessage,
    MatchRequestStatus status,
    LocalDateTime requestAt
) {
    public static MatchRequestHistoryResponseDto from(MatchRequest matchRequest) {
        return new MatchRequestHistoryResponseDto(
            matchRequest.getRequestId(),
            matchRequest.getRequestTeam().getTeamId(),
            matchRequest.getRequestTeam().getTeamName(),
            matchRequest.getTargetTeam().getTeamId(),
            matchRequest.getTargetTeam().getTeamName(),
            matchRequest.getRequestMessage(),
            matchRequest.getStatus(),
            matchRequest.getRequestAt()
        );
    }
}
