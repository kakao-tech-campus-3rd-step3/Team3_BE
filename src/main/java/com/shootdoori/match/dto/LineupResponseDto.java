package com.shootdoori.match.dto;

import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.entity.lineup.LineupStatus;
import com.shootdoori.match.entity.user.UserPosition;

import java.time.LocalDateTime;

public record LineupResponseDto(Long id,
                                Long matchId,
                                Long waitingId,
                                Long requestId,
                                Long teamMemberId,
                                UserPosition position,
                                Boolean isStarter,
                                LineupStatus lineupStatus,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {

    public static LineupResponseDto from(Lineup lineup) {
        return new LineupResponseDto(
                lineup.getId(),
                lineup.getMatch() != null ? lineup.getMatch().getMatchId() : null,
                lineup.getWaiting() != null ? lineup.getWaiting().getWaitingId() : null,
                lineup.getRequest() != null ? lineup.getRequest().getRequestId() : null,
                lineup.getTeamMember().getId(),
                lineup.getPosition(),
                lineup.getIsStarter(),
                lineup.getLineupStatus(),
                lineup.getCreatedAt(),
                lineup.getUpdatedAt()
        );
    }
}
