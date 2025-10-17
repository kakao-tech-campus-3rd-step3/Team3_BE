package com.shootdoori.match.dto;
import com.shootdoori.match.entity.user.UserPosition;

public record LineupRequestDto(Long matchId,
                               Long waitingId,
                               Long requestId,
                               Long teamMemberId,
                               UserPosition position,
                               Boolean isStarter) {
}
