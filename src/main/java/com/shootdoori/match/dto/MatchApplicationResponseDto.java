package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchApplicationStatus;
import java.time.LocalDateTime;

public record MatchApplicationResponseDto(
    Long applicationId,
    Long applicantTeamId,
    Long targetTeamId,
    MatchApplicationStatus status
) {}
