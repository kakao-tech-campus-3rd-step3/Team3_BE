package com.shootdoori.match.dto;
import com.shootdoori.match.entity.common.Position;

public record LineupMemberRequestDto(Long teamMemberId,
                                     Position position,
                                     Boolean isStarter) {
}
