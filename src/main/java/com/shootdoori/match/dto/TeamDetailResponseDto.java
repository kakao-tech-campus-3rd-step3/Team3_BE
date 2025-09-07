package com.shootdoori.match.dto;

import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.TeamType;

public record TeamDetailResponseDto(
    Long id,            // 팀 ID
    String name,        // 팀 이름
    String description, // 팀 소개
    String university,  // 대학교
    SkillLevel skillLevel,  // 실력 수준
    TeamType teamType,    // 팀 유형
    Integer memberCount,// 팀원 수
    String createdAt    // 생성 일자
) {

}