package com.shootdoori.match.dto;

import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.value.TeamName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MatchWaitingResponseDto(
    Long waitingId,
    Long teamId,
    TeamName teamName,
    LocalDate preferredDate,
    LocalTime preferredTimeStart,
    LocalTime preferredTimeEnd,
    Long preferredVenueId,
    SkillLevel skillLevelMin,
    SkillLevel skillLevelMax,
    Boolean universityOnly,
    String message,
    MatchWaitingStatus status,
    LocalDateTime expiresAt,
    Long lineup1Id
) {
    public static MatchWaitingResponseDto from(MatchWaiting mw) {
        return new MatchWaitingResponseDto(
            mw.getWaitingId(),
            mw.getTeamId(),
            mw.getTeamName(),
            mw.getPreferredDate(),
            mw.getPreferredTimeStart(),
            mw.getPreferredTimeEnd(),
            mw.getVenueId(),
            mw.getSkillLevelMin(),
            mw.getSkillLevelMax(),
            mw.getUniversityOnly(),
            mw.getMessage(),
            mw.getMatchWaitingStatus(),
            mw.getExpiresAt(),
            mw.getCreateTeamLineupId()
        );
    }
}
