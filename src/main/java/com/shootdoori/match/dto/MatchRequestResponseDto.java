package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchRequestStatus;

public record MatchRequestResponseDto(
    Long requestId,
    Long requestTeamId,
    Long targetTeamId,
    String requestMessage,
    MatchRequestStatus status
) {}
