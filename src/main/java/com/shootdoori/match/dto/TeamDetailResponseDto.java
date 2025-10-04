package com.shootdoori.match.dto;

import com.shootdoori.match.entity.team.TeamSkillLevel;
import com.shootdoori.match.entity.team.TeamType;

public record TeamDetailResponseDto(
    Long id,
    String name,
    String description,
    String university,
    TeamSkillLevel skillLevel,
    TeamType teamType,
    Integer memberCount,
    String createdAt
) {

}