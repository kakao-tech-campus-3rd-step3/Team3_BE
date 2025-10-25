package com.shootdoori.match.dto;
import com.shootdoori.match.entity.common.Position;

public record LineupRequestDto(Long teamMemberId,
                               Position position,
                               Boolean isStarter) {
}
