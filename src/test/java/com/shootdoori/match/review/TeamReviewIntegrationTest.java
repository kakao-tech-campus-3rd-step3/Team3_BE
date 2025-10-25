package com.shootdoori.match.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shootdoori.match.dto.TeamReviewRequestDto;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;
import com.shootdoori.match.entity.review.TeamReview;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Transactional
class TeamReviewIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

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

    private User user1, user2;
    private Team team1, team2;
    private Venue venue;
    private Match match;

    @BeforeEach
    void setUp() {
        user1 = createUser("김한림", "test1@hallym.ac.kr", "010-1111-1111");
        user2 = createUser("박강원", "test2@kangwon.ac.kr", "010-2222-2222");
        userRepository.save(user1);
        userRepository.save(user2);

        team1 = createTeam("한림 FC", user1);
        team2 = createTeam("강원 KNU", user1);
        teamRepository.save(team1);
        teamRepository.save(team2);

        venue = createVenue("공지천 인조구장");
        venueRepository.save(venue);

        match = createMatch(team1, team2, venue, MatchStatus.FINISHED);
        matchRepository.save(match);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("POST /api/team-reviews - 팀 리뷰 생성 성공")
    void postReview_Success() throws Exception {
        // given
        TeamReviewRequestDto requestDto = new TeamReviewRequestDto(
                match.getMatchId(),
                team1.getTeamId(),
                team2.getTeamId(),
                5,
                ReviewBinaryEvaluation.GOOD,
                ReviewBinaryEvaluation.GOOD,
                ReviewSkillLevel.SIMILAR
        );

        // when & then
        mockMvc.perform(post("/api/team-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andDo(print());

        // DB 검증
        assertThat(teamReviewRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/team-reviews - 특정 팀이 받은 모든 리뷰 조회 성공")
    void getAllReviews_Success() throws Exception {
        // given: team2가 받은 리뷰 1개 저장
        TeamReview review = TeamReview.from(match, team1, team2, 4, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.HIGHER);
        teamReviewRepository.save(review);

        // when & then
        mockMvc.perform(get("/api/team-reviews")
                        .param("teamId", team2.getTeamId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reviewedTeamId").value(team2.getTeamId()))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andDo(print());
    }

    @Test
    @DisplayName("GET /api/team-reviews/{reviewId} - 특정 리뷰 단건 조회 성공")
    void getSingleReview_Success() throws Exception {
        // given
        TeamReview review = TeamReview.from(match, team1, team2, 5, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);
        TeamReview savedReview = teamReviewRepository.save(review);

        // when & then
        mockMvc.perform(get("/api/team-reviews/{reviewId}", savedReview.getId())
                        .param("teamId", team2.getTeamId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamReviewId").value(savedReview.getId()))
                .andExpect(jsonPath("$.rating").value(5))
                .andDo(print());
    }

    @Test
    @DisplayName("PUT /api/team-reviews/{reviewId} - 리뷰 수정 성공")
    void updateReview_Success() throws Exception {
        // given
        TeamReview review = TeamReview.from(match, team1, team2, 5, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);
        TeamReview savedReview = teamReviewRepository.save(review);

        TeamReviewRequestDto updateRequest = new TeamReviewRequestDto(
                match.getMatchId(),
                team1.getTeamId(),
                team2.getTeamId(),
                1, // 평점 1점으로 수정
                ReviewBinaryEvaluation.BAD,
                ReviewBinaryEvaluation.BAD,
                ReviewSkillLevel.LOWER
        );

        // when & then
        mockMvc.perform(put("/api/team-reviews/{reviewId}", savedReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent())
                .andDo(print());

        // DB 검증
        TeamReview updatedReview = teamReviewRepository.findById(savedReview.getId()).get();
        assertThat(updatedReview.getRating()).isEqualTo(1);
        assertThat(updatedReview.getPunctualityReview()).isEqualTo(ReviewBinaryEvaluation.BAD);
    }

    @Test
    @DisplayName("DELETE /api/team-reviews/{reviewId} - 리뷰 삭제 성공")
    void deleteReview_Success() throws Exception {
        // given
        TeamReview review = TeamReview.from(match, team1, team2, 5, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);
        TeamReview savedReview = teamReviewRepository.save(review);

        // when & then
        mockMvc.perform(delete("/api/team-reviews/{reviewId}", savedReview.getId()))
                .andExpect(status().isNoContent())
                .andDo(print());

        // DB 검증
        assertThat(teamReviewRepository.findById(savedReview.getId())).isEmpty();
    }



    // 테스트 데이터 생성용 메서드들
    private User createUser(String name, String email, String phoneNumber) {
        return User.create(name, "아마추어", email, "encodedPassword", phoneNumber,
                "MF", email.contains("hallym") ? "한림대학교" : "강원대학교", "컴퓨터공학과", "23", "테스트용 유저입니다.");
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
