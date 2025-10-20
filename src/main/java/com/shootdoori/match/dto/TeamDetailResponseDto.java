package com.shootdoori.match.dto;

import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.team.TeamType;

public record TeamDetailResponseDto(
    Long id,
    String name,
    String description,
    String university,
    SkillLevel skillLevel,
    TeamType teamType,
    Integer memberCount,
    String createdAt
) {

}