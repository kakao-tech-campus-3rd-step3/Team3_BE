package com.shootdoori.match.dto;

import com.shootdoori.match.entity.lineup.Lineup;

import java.time.LocalDateTime;

public record LineupResponseDto(Long id,
                                Long teamId,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {

    public static LineupResponseDto from(Lineup lineup) {
        return new LineupResponseDto(lineup.getId(),
                lineup.getTeam().getTeamId(),
                lineup.getCreatedAt(),
                lineup.getUpdatedAt()
        );
    }
}
