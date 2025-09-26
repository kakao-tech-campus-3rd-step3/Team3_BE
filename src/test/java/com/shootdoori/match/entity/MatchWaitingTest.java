package com.shootdoori.match.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchWaitingTest {

  private Team team;
  private Venue venue;
  private LocalDate preferredDate;
  private LocalTime preferredTimeStart;
  private LocalTime preferredTimeEnd;
  private SkillLevel skillLevelMin;
  private SkillLevel skillLevelMax;
  private Boolean universityOnly;
  private String message;
  private MatchWaitingStatus status;
  private LocalDateTime expiresAt;

  @BeforeEach
  void setUp() {
    team = new Team();
    venue = new Venue();
    preferredDate = LocalDate.of(2025, 9, 26);
    preferredTimeStart = LocalTime.of(10, 0);
    preferredTimeEnd = LocalTime.of(12, 0);
    skillLevelMin = SkillLevel.AMATEUR;
    skillLevelMax = SkillLevel.PRO;
    universityOnly = true;
    message = "매치 신청합니다.";
    status = MatchWaitingStatus.WAITING;
    expiresAt = LocalDateTime.now().plusDays(1);
  }

  @Test
  @DisplayName("MatchWaiting 의 updateWaitingStatus에 의해 status가 변경되는지 확인")
  void updateWaitingStatus_ShouldChangeStatus() {
    MatchWaiting matchWaiting = new MatchWaiting(
      team,
      preferredDate,
      preferredTimeStart,
      preferredTimeEnd,
      venue,
      skillLevelMin,
      skillLevelMax,
      universityOnly,
      message,
      MatchWaitingStatus.WAITING,
      expiresAt
    );

    matchWaiting.updateWaitingStatus(MatchWaitingStatus.MATCHED);

    assertEquals(MatchWaitingStatus.MATCHED, matchWaiting.getMatchRequestStatus());
  }
}