package com.shootdoori.match.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.match.waiting.MatchWaitingSkillLevel;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamSkillLevel;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchWaitingTest {

    private static final LocalDate PREFERRED_DATE = LocalDate.of(2025, 9, 26);
    private static final LocalTime PREFERRED_TIME_START = LocalTime.of(10, 0);
    private static final LocalTime PREFERRED_TIME_END = LocalTime.of(12, 0);
    private static final MatchWaitingSkillLevel SKILL_LEVEL_MIN = MatchWaitingSkillLevel.AMATEUR;
    private static final MatchWaitingSkillLevel SKILL_LEVEL_MAX = MatchWaitingSkillLevel.PRO;
    private static final boolean UNIVERSITY_ONLY = true;
    private static final String WAITING_MESSAGE = "매치 신청합니다.";
    private static final MatchWaitingStatus WAITING_STATUS = MatchWaitingStatus.WAITING;

    private Team team;
    private Venue venue;

    @BeforeEach
    void setUp() {
        User captain = User.create(
            "캡틴", "아마추어", "c@example.com", "c@kangwon.ac.kr",
            "Abcd1234!", "imkakao", "골키퍼", "강원대학교", "컴퓨터공학과", "25", "캡틴"
        );

        team = new Team("팀", captain, "강원대학교", TeamType.OTHER, TeamSkillLevel.AMATEUR, "설명");
        venue = new Venue(
            "강원대 대운동장",
            "춘천",
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780),
            "033-123-4567",
            "강원대",
            0L
        );
    }

    @Test
    @DisplayName("MatchWaiting 의 updateWaitingStatus에 의해 status가 변경되는지 확인")
    void updateWaitingStatus_ShouldChangeStatus() {
        MatchWaiting matchWaiting = new MatchWaiting(
            team,
            PREFERRED_DATE,
            PREFERRED_TIME_START,
            PREFERRED_TIME_END,
            venue,
            SKILL_LEVEL_MIN,
            SKILL_LEVEL_MAX,
            UNIVERSITY_ONLY,
            WAITING_MESSAGE,
            WAITING_STATUS,
            LocalDateTime.now().plusDays(1)
        );

        matchWaiting.updateWaitingStatus(MatchWaitingStatus.MATCHED);

        assertEquals(MatchWaitingStatus.MATCHED, matchWaiting.getMatchWaitingStatus());
    }
}