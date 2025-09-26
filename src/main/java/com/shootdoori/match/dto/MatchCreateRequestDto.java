package com.shootdoori.match.dto;

import com.shootdoori.match.entity.SkillLevel;
import java.time.LocalDate;
import java.time.LocalTime;

public record MatchCreateRequestDto(
    Long teamId,
    LocalDate preferredDate,
    LocalTime preferredTimeStart,
    LocalTime preferredTimeEnd,
    Long preferredVenueId,
    SkillLevel skillLevelMin,
    SkillLevel skillLevelMax,
    Boolean universityOnly,
    String message
) {}
