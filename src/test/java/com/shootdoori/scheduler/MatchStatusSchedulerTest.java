package com.shootdoori.scheduler;

import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.team.*;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = com.shootdoori.match.MatchApplication.class)
@Transactional
class MatchStatusSchedulerTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private VenueRepository venueRepository;

    private User team1Captain;
    private User team2Captain;
    private Team team1;
    private Team team2;
    private TeamMember team1Member;
    private TeamMember team2Member;
    private Venue savedVenue;

    @BeforeEach
    void setUp() {
        // 팀 캡틴 생성
        team1Captain = profileRepository.save(User.create(
            "팀1 리더", "프로", "team1@university.ac.kr",
            "12345678", "010-9999-9999", "공격수", "강원대학교", "컴퓨터공학과", "20",
            "확정된 매치에서 팀 1에 해당하는 리더입니다."
        ));

        team2Captain = profileRepository.save(User.create(
            "팀2 리더", "프로", "team2@university.ac.kr",
            "12345678", "010-9999-9990", "공격수", "강원대학교", "컴퓨터공학과", "20",
            "확정된 매치에서 팀 2에 해당하는 리더입니다."
        ));

        // 팀 생성
        team1 = teamRepository.save(new Team("Team 1", team1Captain, "강원대학교", TeamType.OTHER, TeamSkillLevel.AMATEUR, "Team 1"));
        team2 = teamRepository.save(new Team("Team 2", team2Captain, "강원대학교", TeamType.OTHER, TeamSkillLevel.AMATEUR, "Team 2"));

        // 팀 멤버 생성
        team1Member = teamMemberRepository.save(new TeamMember(team1, team1Captain, TeamMemberRole.LEADER));
        team2Member = teamMemberRepository.save(new TeamMember(team2, team2Captain, TeamMemberRole.LEADER));

        // 경기장 생성
        savedVenue = venueRepository.save(new Venue(
            "강원대 대운동장", "춘천", BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780),
            "033-123-4567", "강원대", 0L
        ));
    }

    @ParameterizedTest(name = "{index} -> matchDate={0}, matchTime={1}, shouldBeFinished={2}")
    @CsvSource({
        "2025-10-12, 23:00, true",
        "2025-10-13, 20:00, true",
        "2025-10-13, 23:00, true",
        "2025-10-14, 00:00, false",
        "2025-10-14, 10:00, false"
    })
    void testUpdateFinishedMatches(String dateStr, String timeStr, boolean shouldBeFinished) {
        LocalDate matchDate = LocalDate.parse(dateStr);
        LocalTime matchTime = LocalTime.parse(timeStr);

        Match match = matchRepository.save(new Match(team1, team2, matchDate, matchTime, savedVenue, MatchStatus.MATCHED));

        // 테스트 기준 시각: 2025-10-14 02:00 (새벽 2시)
        LocalDate fixedToday = LocalDate.of(2025, 10, 14);
        LocalTime fixedNow = LocalTime.of(2, 0);

        // cutoff 계산
        LocalTime cutoffTime = fixedNow.minusHours(3); // 02:00 - 3시간 = 23:00
        LocalDate cutoffDate;
        if (cutoffTime.isAfter(fixedNow)) {
            cutoffDate = fixedToday.minusDays(1);
        } else {
            cutoffDate = fixedToday;
        }

        // MATCHED인 경기 조회 및 상태 변경
        List<Match> matchesToFinish = matchRepository.findMatchesToFinish(cutoffDate, cutoffTime);
        matchesToFinish.forEach(m -> m.updateStatus(MatchStatus.FINISHED));
        matchRepository.saveAll(matchesToFinish);

        // 검증
        Match updatedMatch = matchRepository.findById(match.getMatchId()).orElseThrow();
        if (shouldBeFinished) {
            assertEquals(MatchStatus.FINISHED, updatedMatch.getStatus());
        } else {
            assertEquals(MatchStatus.MATCHED, updatedMatch.getStatus());
        }
    }
}