package com.shootdoori.match.dto;

public record CreateTeamResponseDto(
    Long teamId,     // 생성된 팀 ID
    String message,  // 응답 메시지 (예: "팀이 성공적으로 생성되었습니다.")
    String teamUrl   // 생성된 팀의 접근 URL (예: "/api/teams/{id}")
) {}
