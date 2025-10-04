package com.shootdoori.match.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchTest {

    private Team team1;
    private Team team2;
    private Venue venue;

    @BeforeEach
    void setUp() {
        team1 = new Team();
        team2 = new Team();
        venue = new Venue();
    }

    @Test
    @DisplayName("Match 객체 생성시 status가 null로 제공되면 MatchStatus.RECRUITING 인지")
    void createMatchWithDefaultStatus() {
        Match match = new Match(team1, team2, LocalDate.now(), LocalTime.now(), venue, null);

        assertEquals(MatchStatus.RECRUITING, match.getStatus());
    }
}