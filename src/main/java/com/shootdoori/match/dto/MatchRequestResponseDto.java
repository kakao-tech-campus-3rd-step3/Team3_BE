package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import com.shootdoori.match.value.TeamName;

public record MatchRequestResponseDto(
    Long requestId,
    Long requestTeamId,
    TeamName requestTeamName,
    Long targetTeamId,
    TeamName targetTeamName,
    String requestMessage,
    MatchRequestStatus status
) {
    public static MatchRequestResponseDto from(MatchRequest matchRequest) {
        return new MatchRequestResponseDto(
            matchRequest.getRequestId(),
            matchRequest.getRequestTeam().getTeamId(),
            matchRequest.getRequestTeam().getTeamName(),
            matchRequest.getTargetTeam().getTeamId(),
            matchRequest.getTargetTeam().getTeamName(),
            matchRequest.getRequestMessage(),
            matchRequest.getStatus()
        );
    }
}
