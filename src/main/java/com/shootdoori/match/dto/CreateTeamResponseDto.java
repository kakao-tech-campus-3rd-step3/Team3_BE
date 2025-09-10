package com.shootdoori.match.dto;

public record CreateTeamResponseDto(
    Long teamId,
    String message,
    String teamUrl  // 생성된 팀의 접근 URL (예: "/api/teams/{id}")
) {

}
