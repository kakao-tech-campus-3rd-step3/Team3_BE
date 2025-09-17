package com.shootdoori.match.dto;

import java.time.LocalDateTime;

public record TeamMemberResponseDto(
    Long id,
    Long userId,
    String name,
    String email,
    String position,
    String role,
    LocalDateTime joinedAt
) {

}
