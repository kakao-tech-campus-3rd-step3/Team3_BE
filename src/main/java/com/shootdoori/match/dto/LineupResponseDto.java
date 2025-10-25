package com.shootdoori.match.dto;

import com.shootdoori.match.entity.lineup.LineupMember;
import com.shootdoori.match.entity.common.Position;

import java.time.LocalDateTime;

public record LineupResponseDto(Long id,
                                Long teamMemberId,
                                Position position,
                                Boolean isStarter,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {

    public static LineupResponseDto from(LineupMember lineupMember) {
        return new LineupResponseDto(
                lineupMember.getId(),
                lineupMember.getTeamMember().getId(),
                lineupMember.getPosition(),
                lineupMember.getIsStarter(),
                lineupMember.getCreatedAt(),
                lineupMember.getUpdatedAt()
        );
    }
}
