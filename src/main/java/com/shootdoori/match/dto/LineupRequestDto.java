package com.shootdoori.match.dto;
import com.shootdoori.match.entity.Position;

public record LineupRequestDto(Long matchId,
                               Long waitingId,
                               Long requestId,
                               Long teamMemberId,
                               Position position,
                               Boolean isStarter) {
}
