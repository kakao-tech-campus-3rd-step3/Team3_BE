package com.shootdoori.match.dto;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.team.TeamType;

public record EnemyTeamResponseDto(Long teamId,
                                   String teamName,
                                   Long captainId,
                                   String captainName,
                                   String universityName,
                                   TeamType teamType,
                                   Integer memberCount,
                                   SkillLevel skillLevel,
                                   String description
) {

    public static EnemyTeamResponseDto from(Team team) {
        return new EnemyTeamResponseDto(
            team.getTeamId(),
            team.getTeamName().name(),
            team.getCaptain().getId(),
            team.getCaptain().getName(),
            team.getUniversity().name(),
            team.getTeamType(),
            team.getMemberCount().count(),
            team.getSkillLevel(),
            team.getDescription().description()
        );
    }
}
