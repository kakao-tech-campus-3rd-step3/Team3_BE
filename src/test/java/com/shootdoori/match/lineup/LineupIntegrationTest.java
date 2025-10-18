package com.shootdoori.match.lineup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shootdoori.match.dto.LineupRequestDto;
import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.match.waiting.MatchWaitingSkillLevel;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.TeamSkillLevel;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.user.UserPosition;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.repository.*; // 모든 리포지토리 import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class LineupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // (변경) Service 대신, 데이터 준비를 위해 모든 Repository를 주입받습니다.
    @Autowired private LineupRepository lineupRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private MatchWaitingRepository matchWaitingRepository;
    @Autowired private MatchRequestRepository matchRequestRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private ProfileRepository profileRepository; // User용 리포지토리
    @Autowired private VenueRepository venueRepository;

    // --- 테스트용 공통 저장 엔티티 ---
    private User savedUser1, savedUser2;
    private Team savedTeam1, savedTeam2;
    private Venue savedVenue;
    private TeamMember savedTeamMember1, savedTeamMember2;
    private Match savedMatch;
    private MatchWaiting savedMatchWaiting;
    private MatchRequest savedMatchRequest;

    @BeforeEach
    void setUp() {
        // (변경) @Transactional에 의해 이 데이터는 테스트 메소드 종료 시 롤백됩니다.
        // 의존성 순서대로 엔티티를 생성하고 H2 DB에 저장합니다.

        // 1. User 생성 (2명)
        savedUser1 = User.create("주장1", "아마추어", "captain1@test.ac.kr", "password123",
                "captain1_kakao", "공격수", "테스트대학교", "컴퓨터공학과", "20", "주장1입니다.");
        savedUser2 = User.create("주장2", "세미프로", "captain2@test.ac.kr", "password123",
                "captain2_kakao", "수비수", "테스트대학교", "경영학과", "21", "주장2입니다.");
        profileRepository.saveAll(List.of(savedUser1, savedUser2));

        // 2. Venue 생성 (경기장)
        savedVenue = new Venue("테스트 경기장", "서울시 테스트구",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"),
                "02-123-4567", "샤워실 있음", 10000L);
        venueRepository.save(savedVenue);

        // 3. Team 생성 (2팀)
        savedTeam1 = new Team("팀A", savedUser1, "테스트대학교", TeamType.CENTRAL_CLUB,
                TeamSkillLevel.AMATEUR, "팀A입니다.");
        savedTeam2 = new Team("팀B", savedUser2, "테스트대학교", TeamType.DEPARTMENT_CLUB,
                TeamSkillLevel.SEMI_PRO, "팀B입니다.");
        teamRepository.saveAll(List.of(savedTeam1, savedTeam2));

        // 4. TeamMember 생성 (각 팀의 주장)
        // (Team 엔티티의 recruitMember를 사용하거나, TeamMember를 직접 생성)
        savedTeamMember1 = new TeamMember(savedTeam1, savedUser1, TeamMemberRole.LEADER);
        savedTeamMember2 = new TeamMember(savedTeam2, savedUser2, TeamMemberRole.LEADER);
        teamMemberRepository.saveAll(List.of(savedTeamMember1, savedTeamMember2));

        // (참고) Team의 memberCount도 수동으로 업데이트해줘야 할 수 있습니다.
        // Team 엔티티의 recruitMember() 메소드가 이를 처리한다면,
        // savedTeam1.recruitMember(savedUser1, TeamMemberRole.LEADER);
        // teamRepository.save(savedTeam1); 방식이 더 좋습니다.
        // 여기서는 TeamMember 생성자를 직접 사용했다고 가정합니다.

        // 5. Match 생성
        savedMatch = new Match(savedTeam1, savedTeam2, LocalDate.now().plusDays(7),
                LocalTime.of(18, 0), savedVenue, MatchStatus.MATCHED);
        matchRepository.save(savedMatch);

        // 6. MatchWaiting 생성
        savedMatchWaiting = new MatchWaiting(savedTeam1, LocalDate.now().plusDays(8),
                LocalTime.of(14, 0), LocalTime.of(16, 0), savedVenue,
                MatchWaitingSkillLevel.AMATEUR, MatchWaitingSkillLevel.SEMI_PRO,
                false, "대기1", MatchWaitingStatus.WAITING, LocalDateTime.now().plusDays(1));
        matchWaitingRepository.save(savedMatchWaiting);

        // 7. MatchRequest 생성
        savedMatchRequest = new MatchRequest(savedMatchWaiting, savedTeam2, savedTeam1, "매치 요청합니다.");
        savedMatchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, LocalDateTime.now());
        matchRequestRepository.save(savedMatchRequest);
    }

    @Test
    @DisplayName("POST /api/lineups - 라인업 생성 통합 테스트 (201 CREATED)")
    void createLineup_IntegrationTest() throws Exception {
        // given (준비)
        // DTO 생성 (H2 DB에 저장된 ID들을 사용)
        LineupRequestDto requestDto = new LineupRequestDto(
                savedMatch.getMatchId(),
                savedMatchWaiting.getWaitingId(),
                savedMatchRequest.getRequestId(),
                savedTeamMember1.getId(), // 1번팀 멤버
                UserPosition.GK,
                true
        );

        // when (실행)
        // MockMvc로 API 호출 -> Controller -> Service -> Repository -> H2 DB
        ResultActions actions = mockMvc.perform(post("/api/lineups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then (검증)
        // 1. HTTP 응답 검증
        MvcResult result = actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists()) // ID가 생성되었는지
                .andExpect(jsonPath("$.position").value("GK"))
                .andExpect(jsonPath("$.teamMemberId").value(savedTeamMember1.getId()))
                .andDo(print())
                .andReturn();

        // 2. (중요) 실제 H2 DB에 데이터가 저장되었는지 리포지토리로 검증
        String jsonResponse = result.getResponse().getContentAsString();
        // 응답 JSON에서 생성된 lineupId 추출
        Long savedLineupId = objectMapper.readTree(jsonResponse).get("id").asLong();

        assertThat(lineupRepository.findById(savedLineupId)).isPresent();
    }

    @Test
    @DisplayName("GET /api/lineups/{id} - 라인업 단건 조회 통합 테스트 (200 OK)")
    void getLineupById_IntegrationTest() throws Exception {
        // given (준비)
        // 테스트용 라인업을 H2 DB에 미리 저장
        Lineup testLineup = new Lineup(
                savedMatch, savedMatchWaiting, savedMatchRequest, savedTeamMember1,
                UserPosition.FW, true
        );
        Lineup savedLineup = lineupRepository.save(testLineup);
        Long savedLineupId = savedLineup.getId();

        // when (실행)
        ResultActions actions = mockMvc.perform(get("/api/lineups/{id}", savedLineupId)
                .accept(MediaType.APPLICATION_JSON));

        // then (검증)
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLineupId))
                .andExpect(jsonPath("$.position").value("FW"));
    }

    @Test
    @DisplayName("GET /api/lineups/{id} - 라인업 조회 실패 통합 테스트 (400 BAD_REQUEST)")
    void getLineupById_NotFound_IntegrationTest() throws Exception {
        // given (준비)
        // DB에 아무것도 저장하지 않음 (ID 999L는 존재하지 않음)

        // when (실행)
        // 존재하지 않는 ID (999L)로 조회
        ResultActions actions = mockMvc.perform(get("/api/lineups/{id}", 999L)
                .accept(MediaType.APPLICATION_JSON));

        // then (검증)
        // Service의 NotFoundException이 400 (BAD_REQUEST)으로 변환되는지 검증
        // (ErrorCode.LINEUP_NOT_FOUND의 HttpStatus가 BAD_REQUEST임)
        actions.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("DELETE /api/lineups/{id} - 라인업 삭제 통합 테스트 (204 NO_CONTENT)")
    void deleteLineup_IntegrationTest() throws Exception {
        // given (준비)
        // 삭제할 라인업을 H2 DB에 미리 저장
        Lineup testLineup = new Lineup(
                savedMatch, savedMatchWaiting, savedMatchRequest, savedTeamMember1,
                UserPosition.DF, false
        );
        Lineup savedLineup = lineupRepository.save(testLineup);
        Long savedLineupId = savedLineup.getId();

        // (DB 저장 확인)
        assertThat(lineupRepository.existsById(savedLineupId)).isTrue();

        // when (실행)
        ResultActions actions = mockMvc.perform(delete("/api/lineups/{id}", savedLineupId));

        // then (검증)
        // 1. HTTP 응답 검증
        actions.andExpect(status().isNoContent());

        // 2. (중요) 실제 H2 DB에서 데이터가 삭제되었는지 리포지토리로 검증
        assertThat(lineupRepository.existsById(savedLineupId)).isFalse();
    }

    @Test
    @DisplayName("PATCH /api/lineups/{id} - 라인업 수정 통합 테스트 (200 OK)")
    void updateLineup_IntegrationTest() throws Exception {
        // given (준비)
        // 1. 원본 라인업 저장
        Lineup originalLineup = new Lineup(
                savedMatch, savedMatchWaiting, savedMatchRequest, savedTeamMember1,
                UserPosition.GK, true // (원본: GK, true)
        );
        Lineup savedLineup = lineupRepository.save(originalLineup);
        Long savedLineupId = savedLineup.getId();

        // 2. 수정용 DTO 준비
        LineupRequestDto updateDto = new LineupRequestDto(
                savedMatch.getMatchId(),
                savedMatchWaiting.getWaitingId(),
                savedMatchRequest.getRequestId(),
                savedTeamMember1.getId(),
                UserPosition.MF, false // (변경: MF, false)
        );

        // when (실행)
        ResultActions actions = mockMvc.perform(patch("/api/lineups/{id}", savedLineupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)));

        // then (검증)
        // 1. HTTP 응답 검증
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLineupId))
                .andExpect(jsonPath("$.position").value("MF"))
                .andExpect(jsonPath("$.isStarter").value(false));

        // 2. (중요) 실제 H2 DB의 데이터가 변경되었는지 검증
        Lineup updatedLineup = lineupRepository.findById(savedLineupId)
                .orElseThrow(() -> new AssertionError("라인업이 DB에 없습니다."));

        assertThat(updatedLineup.getPosition()).isEqualTo(UserPosition.MF);
        assertThat(updatedLineup.getIsStarter()).isFalse();
    }
}