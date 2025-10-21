package com.shootdoori.match.entity;

import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchTest {

    private static final LocalDate MATCH_DATE = LocalDate.now();
    private static final LocalTime MATCH_TIME = LocalTime.now();

    private Team team1;
    private Team team2;
    private Venue venue;

    @BeforeEach
    void setUp() {
        User captain = User.create(
            "캡틴", "아마추어", "c@kangwon.ac.kr",
            "Abcd1234!", "imkakao", "GK", "강원대학교", "컴퓨터공학과", "25", "캡틴"
        );

        team1 = new Team("팀1", captain, "강원대학교", TeamType.OTHER, SkillLevel.AMATEUR, "설명");
        team2 = new Team("팀2", captain, "강원대학교", TeamType.OTHER, SkillLevel.AMATEUR, "설명");

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
    @DisplayName("Match 객체 생성시 status가 null로 제공되면 MatchStatus.RECRUITING 인지")
    void createMatchWithDefaultStatus() {
        Match match = new Match(team1, team2, MATCH_DATE, MATCH_TIME, venue, null);

        assertEquals(MatchStatus.RECRUITING, match.getStatus());
    }
}