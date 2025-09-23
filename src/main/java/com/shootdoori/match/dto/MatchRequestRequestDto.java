package com.shootdoori.match.dto;

public record MatchApplicationRequestDto(
    Long applicantTeamId,
    String applicationMessage
) {}
