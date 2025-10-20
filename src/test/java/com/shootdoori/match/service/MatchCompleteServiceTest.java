package com.shootdoori.match.service;

import com.shootdoori.match.dto.EnemyTeamResponseDto;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.team.*;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Transactional
class MatchCompleteServiceTest {

    @Autowired
    private MatchCompleteService matchCompleteService;

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
    private Match savedMatch;
    private Venue savedVenue;

    private final Long NON_EXIST_MATCH_ID = 1000000007L;

    @BeforeEach
    void setUp() {
        team1Captain = profileRepository.save(User.create(
            "팀1 리더",
            "프로",
            "team1@kangwon.ac.kr",
            "12345678",
            "010-9999-9999",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "확정된 매치에서 팀 1에 해당하는 리더입니다."
        ));

        team2Captain = profileRepository.save(User.create(
            "팀2 리더",
            "프로",
            "team2@kangwon.ac.kr",
            "12345678",
            "010-9999-9990",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "확정된 매치에서 팀 2에 해당하는 리더입니다."
        ));

        team1 = teamRepository.save(new Team(
            "Team 1",
            team1Captain,
            "강원대학교",
            TeamType.OTHER,
            SkillLevel.AMATEUR,
            "Team 1"
        ));

        team2 = teamRepository.save(new Team(
            "Team 2",
            team2Captain,
            "강원대학교",
            TeamType.OTHER,
            SkillLevel.AMATEUR,
            "Team 2"
        ));

        team1Member = teamMemberRepository.save(new TeamMember(team1, team1Captain, TeamMemberRole.LEADER));
        team2Member = teamMemberRepository.save(new TeamMember(team2, team2Captain, TeamMemberRole.LEADER));

        Venue venue = new Venue(
            "강원대 대운동장",
            "춘천",
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780),
            "033-123-4567",
            "강원대",
            0L
        );
        savedVenue = venueRepository.save(venue);

        savedMatch = matchRepository.save(new Match(
            team1,
            team2,
            LocalDate.now(),
            LocalTime.of(10, 0),
            savedVenue,
            MatchStatus.FINISHED
        ));
    }

    @Test
    @DisplayName("내 팀이 team1일 때 상대팀(team2) 정보가 올바르게 조회된다")
    void findEnemyTeam_whenMyTeamIsTeam1_returnsTeam2() {
        // when
        EnemyTeamResponseDto response = matchCompleteService.findEnemyTeam(team1Captain.getId(), savedMatch.getMatchId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.teamId()).isEqualTo(team2.getTeamId());
        assertThat(response.teamName()).isEqualTo(team2.getTeamName().name());
        assertThat(response.captainId()).isEqualTo(team2Captain.getId());
        assertThat(response.captainName()).isEqualTo(team2Captain.getName());
        assertThat(response.universityName()).isEqualTo(team2.getUniversity().name());
        assertThat(response.skillLevel()).isEqualTo(team2.getSkillLevel());
    }

    @Test
    @DisplayName("내 팀이 team2일 때 상대팀(team1) 정보가 올바르게 조회된다")
    void findEnemyTeam_whenMyTeamIsTeam2_returnsTeam1() {
        // when
        EnemyTeamResponseDto response = matchCompleteService.findEnemyTeam(team2Captain.getId(), savedMatch.getMatchId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.teamId()).isEqualTo(team1.getTeamId());
        assertThat(response.teamName()).isEqualTo(team1.getTeamName().name());
        assertThat(response.captainId()).isEqualTo(team1Captain.getId());
        assertThat(response.captainName()).isEqualTo(team1Captain.getName());
        assertThat(response.universityName()).isEqualTo(team1.getUniversity().name());
        assertThat(response.skillLevel()).isEqualTo(team1.getSkillLevel());
    }

    @Test
    @DisplayName("존재하지 않는 매치 ID로 조회 시 NotFoundException 발생")
    void findEnemyTeam_matchNotFound_throwsException() {
        // when
        Throwable thrown = catchThrowable(() ->
            matchCompleteService.findEnemyTeam(team1Captain.getId(), NON_EXIST_MATCH_ID)
        );

        // then
        assertThat(thrown)
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(String.valueOf(NON_EXIST_MATCH_ID));
    }
}
