package com.shootdoori.match.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shootdoori.match.dto.MercenaryReviewRequestDto;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.review.MercenaryReview;
import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamSkillLevel;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Transactional
class MercenaryReviewIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MercenaryReviewRepository mercenaryReviewRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProfileRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private MatchRepository matchRepository;

    private User user1, user2;
    private Team team1, team2;
    private Venue venue;
    private Match match;

    @BeforeEach
    void setUp() {
        user1 = createUser("이용병","test1@naver.com" ,"mercenary_captain@hallym.ac.kr", "010-1111-1111");
        user2 = createUser("김용병","test2@naver.com" ,"mercenary_captain@kangwon.ac.kr", "010-2222-2222");
        userRepository.save(user1);
        userRepository.save(user2);

        team1 = createTeam("슛돌이 FC", user1);
        team2 = createTeam("감자 FC", user2);
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
    @DisplayName("GET /api/mercenary-reviews - 특정 용병이 받은 모든 리뷰 조회 성공")
    void getAllReviews_Success() throws Exception {
        // given
        MercenaryReview review1 = new MercenaryReview(match, team1, user1, 5, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);
        MercenaryReview review2 = new MercenaryReview(match, team2, user1, 1, ReviewBinaryEvaluation.BAD, ReviewBinaryEvaluation.BAD, ReviewSkillLevel.LOWER);
        mercenaryReviewRepository.save(review1);
        mercenaryReviewRepository.save(review2);

        // when & then
        mockMvc.perform(get("/api/mercenary-reviews")
                        .param("profileId", user1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].reviewerTeamId").value(team1.getTeamId()))
                .andExpect(jsonPath("$[1].reviewerTeamId").value(team2.getTeamId()))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[1].rating").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("GET /api/mercenary-reviews/{reviewId} - 특정 용병 리뷰 단건 조회 성공")
    void getSingleReview_Success() throws Exception {
        // given
        MercenaryReview review = new MercenaryReview(match, team1, user1, 5, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);
        MercenaryReview savedReview = mercenaryReviewRepository.save(review);

        // when & then
        mockMvc.perform(get("/api/mercenary-reviews/{reviewId}", savedReview.getId())
                        .param("profileId", user1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mercenaryReviewId").value(savedReview.getId()))
                .andExpect(jsonPath("$.rating").value(savedReview.getRating()))
                .andDo(print());
    }

    @Test
    @DisplayName("POST /api/mercenary-reviews - 용병 리뷰 생성 성공")
    void postReview_Success() throws Exception {
        // given
        MercenaryReviewRequestDto requestDto = new MercenaryReviewRequestDto(
                match.getMatchId(),
                team1.getTeamId(),
                user1.getId(),
                5,
                ReviewBinaryEvaluation.GOOD,
                ReviewBinaryEvaluation.GOOD,
                ReviewSkillLevel.SIMILAR
        );

        // when & then
        mockMvc.perform(post("/api/mercenary-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("PUT /api/mercenary-reviews/{reviewId} - 리뷰 수정 성공")
    void updateReview_Success() throws Exception {
        // given
        MercenaryReview review = new MercenaryReview(match, team1, user1, 5, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);
        MercenaryReview savedReview = mercenaryReviewRepository.save(review);

        MercenaryReviewRequestDto updateRequest = new MercenaryReviewRequestDto(
                match.getMatchId(),
                team1.getTeamId(),
                user1.getId(),
                1, // 평점 1점으로 수정
                ReviewBinaryEvaluation.BAD, // 나쁨으로 수정
                ReviewBinaryEvaluation.BAD, // 나쁨으로 수정
                ReviewSkillLevel.LOWER // 표기보다 낮음으로 수정
        );

        // when & then
        mockMvc.perform(put("/api/mercenary-reviews/{reviewId}", savedReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent())
                .andDo(print());

        // DB 검증
        MercenaryReview updatedReview = mercenaryReviewRepository.findById(savedReview.getId()).get();
        assertThat(updatedReview.getRating()).isEqualTo(1);
        assertThat(updatedReview.getPunctualityReview()).isEqualTo(ReviewBinaryEvaluation.BAD);
        assertThat(updatedReview.getSportsmanshipReview()).isEqualTo(ReviewBinaryEvaluation.BAD);
        assertThat(updatedReview.getSkillLevelReview()).isEqualTo(ReviewSkillLevel.LOWER);
    }

    @Test
    @DisplayName("DELETE /api/mercenary-reviews/{reviewId} - 리뷰 삭제 성공")
    void deleteReview_Success() throws Exception {
        // given
        MercenaryReview review = new MercenaryReview(match, team1, user1, 5, ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);
        MercenaryReview savedReview = mercenaryReviewRepository.save(review);

        // when & then
        mockMvc.perform(delete("/api/mercenary-reviews/{reviewId}", savedReview.getId()))
                .andExpect(status().isNoContent())
                .andDo(print());

        // DB 검증
        assertThat(mercenaryReviewRepository.findById(savedReview.getId())).isEmpty();
    }

    // 테스트 데이터 생성용 메서드

    private User createUser(String name, String email, String universityEmail, String phoneNumber) {
        return User.create(name, "아마추어", email, universityEmail, "encodedPassword", phoneNumber,
                "미드필더", email.contains("hallym") ? "한림대학교" : "강원대학교", "컴퓨터공학과", "23", "테스트용 유저입니다.");
    }

    private Team createTeam(String name, User captain) {
        return new Team(name, captain, captain.getUniversity().name(), TeamType.OTHER, TeamSkillLevel.AMATEUR, "용병 모집 테스트용 팀입니다.");
    }

    private Venue createVenue(String name) {
        return new Venue(name, "주소", BigDecimal.valueOf(37.8), BigDecimal.valueOf(127.7), "010-1234-5678",
                "테스트", 3000L);
    }

    private Match createMatch(Team team1, Team team2, Venue venue, MatchStatus status) {
        return new Match(team1, team2, LocalDate.now().minusDays(1), LocalTime.of(19, 0), venue, status);
    }
}
