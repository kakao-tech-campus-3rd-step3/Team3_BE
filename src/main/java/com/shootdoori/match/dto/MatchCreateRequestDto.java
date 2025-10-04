package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MatchWaitingSkillLevel;
import java.time.LocalDate;
import java.time.LocalTime;

public record MatchCreateRequestDto(
    LocalDate preferredDate,
    LocalTime preferredTimeStart,
    LocalTime preferredTimeEnd,
    Long preferredVenueId,
    MatchWaitingSkillLevel skillLevelMin,
    MatchWaitingSkillLevel skillLevelMax,
    Boolean universityOnly,
    String message
) {}
