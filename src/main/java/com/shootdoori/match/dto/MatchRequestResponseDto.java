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
            matchRequest.getRequestTeamId(),
            matchRequest.getRequestTeamName(),
            matchRequest.getTargetTeamId(),
            matchRequest.getTargetTeamName(),
            matchRequest.getRequestMessage(),
            matchRequest.getStatus()
        );
    }
}
