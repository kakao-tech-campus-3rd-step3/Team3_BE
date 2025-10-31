package com.shootdoori.match.entity;

import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchRequestTest {

    private static final LocalDate PREFERRED_DATE = LocalDate.of(2025, 9, 26);
    private static final LocalTime PREFERRED_TIME_START = LocalTime.of(10, 0);
    private static final LocalTime PREFERRED_TIME_END = LocalTime.of(12, 0);
    private static final SkillLevel SKILL_LEVEL_MIN = SkillLevel.AMATEUR;
    private static final SkillLevel SKILL_LEVEL_MAX = SkillLevel.PRO;
    private static final boolean UNIVERSITY_ONLY = true;
    private static final String WAITING_MESSAGE = "매치 신청합니다.";
    private static final MatchWaitingStatus WAITING_STATUS = MatchWaitingStatus.WAITING;

    private User captain;
    private User anotherCaptain;

    private Team requestTeam;
    private Team targetTeam;
    private MatchWaiting matchWaiting;

    @BeforeEach
    void setUp() {
        Venue venue = new Venue(
            "강원대 대운동장",
            "춘천",
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780),
            "033-123-4567",
            "강원대",
            0L
        );

        captain = User.create(
            "김학생",
            "아마추어",
            "student@kangwon.ac.kr",
            "Abcd1234!",
            "imkim251",
            "GK",
            "강원대학교",
            "컴퓨터공학과",
            "25",
            "축구를 좋아하는 대학생입니다. GK 포지션을 주로 맡고 있으며, 즐겁게 운동하고 싶습니다!"
        );

        anotherCaptain = User.create(
            "손응민",
            "세미프로",
            "student35@kangwon.ac.kr",
            "Abcd1234!",
            "imkim252",
            "RB",
            "강원대학교",
            "컴퓨터공학과",
            "35",
            "축구 좋아하는 아빠입니다."
        );

        requestTeam = new Team(
            "강원대 FC",
            captain,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );

        targetTeam = new Team(
            "감자빵 FC",
            captain,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("아마추어"),
            "주 1회 연습합니다."
        );

        matchWaiting = new MatchWaiting(
            requestTeam,
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
    }

    @Test
    @DisplayName("MatchRequest 객체 생성시 status가 기본 PENDING으로 설정되는지 확인")
    void createMatchRequest_DefaultValuesShouldBeSet() {
        String message = "친선 경기 신청합니다.";

        MatchRequest matchRequest = new MatchRequest(matchWaiting, requestTeam, targetTeam,
            message);

        assertEquals(MatchRequestStatus.PENDING, matchRequest.getStatus());
    }

    @Test
    @DisplayName("MatchRequest 의 updateRequestStatus에 의해 status가 변경되는지 확인")
    void updateRequestStatus_ShouldUpdateStatusAndRespondedAt() {
        MatchRequest matchRequest = new MatchRequest(matchWaiting, requestTeam, targetTeam,
            "테스트 메시지");
        LocalDateTime respondedAt = LocalDateTime.now();

        matchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, respondedAt);

        assertEquals(MatchRequestStatus.ACCEPTED, matchRequest.getStatus());
        assertEquals(respondedAt, matchRequest.getRespondedAt());
    }

    @Test
    @DisplayName("MatchRequest 의 cancelRequest 에 의해 status가 CANCELED로 변경되는지 확인")
    void cancelRequest_ShouldSetStatusToCanceled() {
        MatchRequest matchRequest = new MatchRequest(matchWaiting, requestTeam, targetTeam,
            "테스트 메시지");

        matchRequest.cancelRequest();

        assertEquals(MatchRequestStatus.CANCELED, matchRequest.getStatus());
    }
}