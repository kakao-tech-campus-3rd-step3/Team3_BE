package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchRequestResponseDto(
    Long requestId,
    Long requestTeamId,
    TeamName requestTeamName,
    Long targetTeamId,
    TeamName targetTeamName,
    LocalDate preferredDate,
    LocalTime preferredTimeStart,
    LocalTime preferredTimeEnd,
    String venueName,
    String requestMessage,
    MatchRequestStatus status,
    Long requestTeamLineupId
) {
    public static MatchRequestResponseDto from(MatchRequest matchRequest) {
        return new MatchRequestResponseDto(
            matchRequest.getRequestId(),
            matchRequest.getRequestTeamId(),
            matchRequest.getRequestTeamName(),
            matchRequest.getTargetTeamId(),
            matchRequest.getTargetTeamName(),
            matchRequest.getMatchWaiting().getPreferredDate(),
            matchRequest.getMatchWaiting().getPreferredTimeStart(),
            matchRequest.getMatchWaiting().getPreferredTimeEnd(),
            matchRequest.getMatchWaiting().getPreferredVenue().getVenueName(),
            matchRequest.getRequestMessage(),
            matchRequest.getStatus(),
            matchRequest.getRequestTeamLineupId()
        );
    }
}
