package com.shootdoori.match.dto;

import com.shootdoori.match.entity.TeamSkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;

import java.time.LocalDateTime;

public record TeamResponseDto(Long teamId,
                              String teamName,
                              User captain,
                              String university,
                              TeamType teamType,
                              Integer memberCount,
                              TeamSkillLevel skillLevel,
                              String description,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {

    public TeamResponseDto(Team team){
        this(team.getTeamId(),
                team.getTeamName().name(),
                team.getCaptain(),
                team.getUniversity().name(),
                team.getTeamType(),
                team.getMemberCount().count(),
                team.getSkillLevel(),
                team.getDescription().description(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }
}
