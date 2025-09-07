package com.shootdoori.match.dto;

public record CreateTeamRequestDto(
    String name,        // 팀 이름
    String description, // 팀 소개 (최대 1000자)
    String university,  // 소속 대학교
    String skillLevel,  // 실력 수준: "아마추어" | "세미프로" | "프로"
    String teamType,    // 팀 유형: "중앙동아리" | "과동아리" | "기타"
    Integer memberCount // 현재 팀원 수 (nullable → 기본 0)
) {

}