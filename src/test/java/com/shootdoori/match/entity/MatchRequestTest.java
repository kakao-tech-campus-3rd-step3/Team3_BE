package com.shootdoori.match.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchRequestTest {

    private Team requestTeam;
    private Team targetTeam;
    private MatchWaiting matchWaiting;

    @BeforeEach
    void setUp() {
        requestTeam = new Team();

        targetTeam = new Team();

        matchWaiting = new MatchWaiting();
    }

    @Test
    @DisplayName("MatchRequest 객체 생성시 status가 기본 PENDING으로 설정되는지 확인")
    void createMatchRequest_DefaultValuesShouldBeSet() {
        String message = "친선 경기 신청합니다.";

        MatchRequest matchRequest = new MatchRequest(matchWaiting, requestTeam, targetTeam, message);

        assertEquals(MatchRequestStatus.PENDING, matchRequest.getStatus());
    }

    @Test
    @DisplayName("MatchRequest 의 updateRequestStatus에 의해 status가 변경되는지 확인")
    void updateRequestStatus_ShouldUpdateStatusAndRespondedAt() {
        MatchRequest matchRequest = new MatchRequest(matchWaiting, requestTeam, targetTeam, "테스트 메시지");
        LocalDateTime respondedAt = LocalDateTime.now();

        matchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, respondedAt);

        assertEquals(MatchRequestStatus.ACCEPTED, matchRequest.getStatus());
        assertEquals(respondedAt, matchRequest.getRespondedAt());
    }

    @Test
    @DisplayName("MatchRequest 의 cancelRequest 에 의해 status가 CANCELED로 변경되는지 확인")
    void cancelRequest_ShouldSetStatusToCanceled() {
        MatchRequest matchRequest = new MatchRequest(matchWaiting, requestTeam, targetTeam, "테스트 메시지");

        matchRequest.cancelRequest();

        assertEquals(MatchRequestStatus.CANCELED, matchRequest.getStatus());
    }
}