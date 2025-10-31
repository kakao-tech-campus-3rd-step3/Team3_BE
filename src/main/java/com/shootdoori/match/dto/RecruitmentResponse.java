package com.shootdoori.match.dto;

import com.shootdoori.match.entity.mercenary.MercenaryRecruitment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RecruitmentResponse(
    Long recruitmentId,
    Long teamId,
    String teamName,
    String universityName,
    LocalDate matchDate,
    LocalTime matchTime,
    String message,
    String position,
    String skillLevel,
    String recruitmentStatus,
    LocalDateTime createdAt
) {
    public static RecruitmentResponse from(MercenaryRecruitment recruitment) {
        return new RecruitmentResponse(
            recruitment.getId(),
            recruitment.getTeam().getTeamId(),
            recruitment.getTeam().getTeamName().name(),
            recruitment.getTeam().getUniversity().name(),
            recruitment.getMatchDate(),
            recruitment.getMatchTime(),
            recruitment.getMessage(),
            recruitment.getPosition().name(),
            recruitment.getSkillLevel().getDisplayName(),
            recruitment.getRecruitmentStatus().getDisplayName(),
            recruitment.getCreatedAt()
        );
    }
}
