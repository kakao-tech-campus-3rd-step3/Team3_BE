package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchRequestStatus;

public record MatchApplicationResponseDto(
    Long applicationId,
    Long applicantTeamId,
    Long targetTeamId,
    String applicationMessage,
    MatchRequestStatus status
) {}
