package com.shootdoori.match.dto;

import com.shootdoori.match.entity.SkillLevel;
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
                              SkillLevel skillLevel,
                              String description,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {

    public TeamResponseDto(Team team){
        this(team.getTeamId(),
                team.getTeamName(),
                team.getCaptain(),
                team.getUniversity(),
                team.getTeamType(),
                team.getMemberCount(),
                team.getSkillLevel(),
                team.getDescription(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }
}
