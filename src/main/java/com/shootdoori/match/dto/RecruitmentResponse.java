package com.shootdoori.match.dto;

import com.shootdoori.match.entity.MercenaryRecruitment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RecruitmentResponse(
    Long recruitmentId,
    LocalDate matchDate,
    LocalTime matchTime,
    String message,
    String position,
    String skillLevel,
    String recruitmentStatus,
    LocalDateTime createdAt
) {
    public RecruitmentResponse(MercenaryRecruitment recruitment) {
        this(
            recruitment.getId(),
            recruitment.getMatchDate(),
            recruitment.getMatchTime(),
            recruitment.getMessage(),
            recruitment.getPosition().getDisplayName(),
            recruitment.getSkillLevel().getDisplayName(),
            recruitment.getRecruitmentStatus().getDisplayName(),
            recruitment.getCreatedAt()
        );
    }
}
