package com.shootdoori.match.review;

import com.shootdoori.match.entity.*;
import com.shootdoori.match.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TeamReviewIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private TeamReviewRepository teamReviewRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProfileRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    private User captain1, captain2;
    private Team team1, team2;
    private Venue venue;
    private Match finishedMatch;

    @BeforeEach
    void setUp() {
        captain1 = createUser("김한림", "test1@naver.com","test1@hallym.ac.kr", "010-1111-1111");
        captain2 = createUser("박강원", "test2@naver.com","test2@kangwon.ac.kr", "010-2222-2222");
        userRepository.save(captain1);
        userRepository.save(captain2);

        team1 = createTeam("한림 FC", captain1);
        team2 = createTeam("강원 KNU", captain2);
        teamRepository.save(team1);
        teamRepository.save(team2);

        venue = createVenue("공지천 인조구장");
        venueRepository.save(venue);

        finishedMatch = createMatch(team1, team2, venue, MatchStatus.FINISHED);
        matchRepository.save(finishedMatch);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("팀 리뷰 생성 성공 테스트")
    void createTeamReview_Success() {
        // Given: team1이 team2에 대한 리뷰를 작성
        Integer rating = 5;
        ReviewBinaryEvaluation punctuality = ReviewBinaryEvaluation.GOOD;
        ReviewBinaryEvaluation sportsmanship = ReviewBinaryEvaluation.GOOD;
        ReviewSkillLevel skillLevel = ReviewSkillLevel.SIMILAR;

        TeamReview newReview = TeamReview.from(
                finishedMatch,
                team1,
                team2,
                rating,
                punctuality,
                sportsmanship,
                skillLevel
        );

        // When: 리뷰를 저장
        TeamReview savedReview = teamReviewRepository.save(newReview);
        em.flush();
        em.clear();

        // Then: 저장된 리뷰를 검증
        TeamReview foundReview = teamReviewRepository.findById(savedReview.getId()).orElse(null);
        assertThat(foundReview).isNotNull();
        assertThat(foundReview.getMatch().getMatchId()).isEqualTo(finishedMatch.getMatchId());
        assertThat(foundReview.getReviewerTeam().getTeamId()).isEqualTo(team1.getTeamId());
        assertThat(foundReview.getReviewedTeam().getTeamId()).isEqualTo(team2.getTeamId());
        assertThat(foundReview.getRating()).isEqualTo(rating);
        assertThat(foundReview.getPunctualityReview()).isEqualTo(punctuality);
        assertThat(foundReview.getSportsmanshipReview()).isEqualTo(sportsmanship);
        assertThat(foundReview.getSkillLevelReview()).isEqualTo(skillLevel);
    }

    @Test
    @DisplayName("하나의 경기에 대해 동일한 팀이 중복 리뷰 작성 시 예외 발생")
    void createTeamReview_Fail_WithDuplicateReview() {
        // Given: team1이 team2에 대한 리뷰를 이미 작성한 상태
        TeamReview firstReview = TeamReview.from(
                finishedMatch, team1, team2, 4,
                ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR
        );
        teamReviewRepository.saveAndFlush(firstReview);

        // When: 동일한 팀(team1)이 다시 리뷰를 작성하려고 시도
        TeamReview duplicateReview = TeamReview.from(
                finishedMatch, team1, team2, 5,
                ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.HIGHER
        );

        // Then: Unique Constraint 위반으로 DataIntegrityViolationException 발생
        assertThrows(DataIntegrityViolationException.class, () -> {
            teamReviewRepository.saveAndFlush(duplicateReview);
        });
    }



    // 테스트 데이터 생성용 메서드들
    private User createUser(String name, String email, String universityEmail, String phoneNumber) {
        return User.create(name, "아마추어", email, universityEmail, "encodedPassword", phoneNumber,
                "미드필더", email.contains("hallym") ? "한림대학교" : "강원대학교", "컴퓨터공학과", "23", "테스트용 유저입니다.");
    }

    private Team createTeam(String name, User captain) {
        return new Team(name, captain, captain.getUniversity().name(), TeamType.OTHER, SkillLevel.AMATEUR, "테스트용 팀입니다.");
    }

    private Venue createVenue(String name) {
        return new Venue(name, "주소", BigDecimal.valueOf(37.8), BigDecimal.valueOf(127.7), "010-1234-5678",
                "테스트", 3000L);
    }

    private Match createMatch(Team team1, Team team2, Venue venue, MatchStatus status) {
        return new Match(team1, team2, LocalDate.now().minusDays(1), LocalTime.of(19, 0), venue, status);
    }
}
