package com.shootdoori.match.lineup;

import com.fasterxml.jackson.databind.JsonNode; // JsonNode import 추가
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
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

    @Autowired private LineupRepository lineupRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private MatchWaitingRepository matchWaitingRepository;
    @Autowired private MatchRequestRepository matchRequestRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private VenueRepository venueRepository;

    private User savedUser1, savedUser2;
    private Team savedTeam1, savedTeam2;
    private Venue savedVenue;
    private TeamMember savedTeamMember1, savedTeamMember2;
    private Match savedMatch;
    private MatchWaiting savedMatchWaiting;
    private MatchRequest savedMatchRequest;

    @BeforeEach
    void setUp() {
        savedUser1 = User.create("주장1", "아마추어", "captain1@test.ac.kr", "password123",
                "captain1_kakao", "공격수", "테스트대학교", "컴퓨터공학과", "20", "주장1입니다.");
        savedUser2 = User.create("주장2", "세미프로", "captain2@test.ac.kr", "password123",
                "captain2_kakao", "수비수", "테스트대학교", "경영학과", "21", "주장2입니다.");
        profileRepository.saveAll(List.of(savedUser1, savedUser2));

        savedVenue = new Venue("테스트 경기장", "서울시 테스트구",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"),
                "02-123-4567", "샤워실 있음", 10000L);
        venueRepository.save(savedVenue);

        savedTeam1 = new Team("팀A", savedUser1, "테스트대학교", TeamType.CENTRAL_CLUB,
                TeamSkillLevel.AMATEUR, "팀A입니다.");
        savedTeam2 = new Team("팀B", savedUser2, "테스트대학교", TeamType.DEPARTMENT_CLUB,
                TeamSkillLevel.SEMI_PRO, "팀B입니다.");
        teamRepository.saveAll(List.of(savedTeam1, savedTeam2));

        savedTeamMember1 = new TeamMember(savedTeam1, savedUser1, TeamMemberRole.LEADER);
        savedTeamMember2 = new TeamMember(savedTeam2, savedUser2, TeamMemberRole.LEADER);
        teamMemberRepository.saveAll(List.of(savedTeamMember1, savedTeamMember2));

        savedMatch = new Match(savedTeam1, savedTeam2, LocalDate.now().plusDays(7),
                LocalTime.of(18, 0), savedVenue, MatchStatus.MATCHED);
        matchRepository.save(savedMatch);

        savedMatchWaiting = new MatchWaiting(savedTeam1, LocalDate.now().plusDays(8),
                LocalTime.of(14, 0), LocalTime.of(16, 0), savedVenue,
                MatchWaitingSkillLevel.AMATEUR, MatchWaitingSkillLevel.SEMI_PRO,
                false, "대기1", MatchWaitingStatus.WAITING, LocalDateTime.now().plusDays(1));
        matchWaitingRepository.save(savedMatchWaiting);

        savedMatchRequest = new MatchRequest(savedMatchWaiting, savedTeam2, savedTeam1, "매치 요청합니다.");
        savedMatchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, LocalDateTime.now());
        matchRequestRepository.save(savedMatchRequest);
    }

    @Test
    @DisplayName("POST /api/lineups - 라인업 생성 (단일 항목 리스트) (201 CREATED)")
    void createLineup_List_Success_SingleItem_IntegrationTest() throws Exception {
        // given (준비)
        LineupRequestDto requestDto = new LineupRequestDto(
                savedMatch.getMatchId(),
                savedMatchWaiting.getWaitingId(),
                savedMatchRequest.getRequestId(),
                savedTeamMember1.getId(), // 1번팀 멤버
                UserPosition.GK,
                true
        );

        List<LineupRequestDto> requestList = List.of(requestDto);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser1.getId(), // 1번팀 주장으로 인증
                null,
                Collections.emptyList()
        );

        // when (실행)
        ResultActions actions = mockMvc.perform(post("/api/lineups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList))
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then (검증)
        MvcResult result = actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists()) // ID가 생성되었는지
                .andExpect(jsonPath("$[0].position").value("GK"))
                .andExpect(jsonPath("$[0].teamMemberId").value(savedTeamMember1.getId()))
                .andDo(print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Long savedLineupId = objectMapper.readTree(jsonResponse).get(0).get("id").asLong();

        assertThat(lineupRepository.findById(savedLineupId)).isPresent();
    }

    @Test
    @DisplayName("POST /api/lineups - 라인업 일괄 생성 (다중 항목 리스트) (201 CREATED)")
    void createLineup_List_Success_MultipleItems_IntegrationTest() throws Exception {
        // given (준비)
        User savedUser3 = User.create("멤버1", "아마추어", "member1@test.ac.kr", "password123",
                "member1_kakao", "미드필더", "테스트대학교", "체육학과", "22", "멤버1입니다.");
        profileRepository.save(savedUser3);
        TeamMember savedTeamMember3 = new TeamMember(savedTeam1, savedUser3, TeamMemberRole.MEMBER);
        teamMemberRepository.save(savedTeamMember3);

        LineupRequestDto requestDto1 = new LineupRequestDto(
                savedMatch.getMatchId(), null, null,
                savedTeamMember1.getId(), // 1번팀 멤버 (주장)
                UserPosition.GK, true
        );
        LineupRequestDto requestDto2 = new LineupRequestDto(
                savedMatch.getMatchId(), null, null,
                savedTeamMember3.getId(), // 1번팀 멤버 (일반)
                UserPosition.DF, true
        );

        List<LineupRequestDto> requestList = List.of(requestDto1, requestDto2);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser1.getId(), // 1번팀 주장으로 인증 (주장이 멤버 등록)
                null,
                Collections.emptyList()
        );

        // when (실행)
        ResultActions actions = mockMvc.perform(post("/api/lineups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList))
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then (검증)
        MvcResult result = actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].position").value("GK"))
                .andExpect(jsonPath("$[1].position").value("DF"))
                .andDo(print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        Long savedLineupId1 = responseNode.get(0).get("id").asLong();
        Long savedLineupId2 = responseNode.get(1).get("id").asLong();

        // 2개 항목 모두 DB에 저장되었는지 확인
        assertThat(lineupRepository.findById(savedLineupId1)).isPresent();
        assertThat(lineupRepository.findById(savedLineupId2)).isPresent();
    }


    @Test
    @DisplayName("GET /api/lineups/{id} - 라인업 단건 조회 통합 테스트 (200 OK)")
    void getLineupById_IntegrationTest() throws Exception {
        // given (준비)
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
    @DisplayName("GET /api/lineups/{id} - 라인업 조회 실패 통합 테스트 (404 NOT_FOUND)")
    void getLineupById_NotFound_IntegrationTest() throws Exception {
        // when (실행)
        ResultActions actions = mockMvc.perform(get("/api/lineups/{id}", 99999L) // 존재하지 않을 ID
                .accept(MediaType.APPLICATION_JSON));

        // then (검증)
        actions.andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("DELETE /api/lineups/{id} - 라인업 삭제 통합 테스트 (204 NO_CONTENT)")
    void deleteLineup_IntegrationTest() throws Exception {
        // given (준비)
        Lineup testLineup = new Lineup(
                savedMatch, savedMatchWaiting, savedMatchRequest, savedTeamMember1,
                UserPosition.DF, false
        );
        Lineup savedLineup = lineupRepository.save(testLineup);
        Long savedLineupId = savedLineup.getId();
        assertThat(lineupRepository.existsById(savedLineupId)).isTrue();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser1.getId(),
                null,
                Collections.emptyList()
        );

        // when (실행)
        ResultActions actions = mockMvc.perform(delete("/api/lineups/{id}", savedLineupId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then (검증)
        actions.andExpect(status().isNoContent());
        assertThat(lineupRepository.existsById(savedLineupId)).isFalse();
    }

    @Test
    @DisplayName("PATCH /api/lineups/{id} - 라인업 수정 통합 테스트 (200 OK)")
    void updateLineup_IntegrationTest() throws Exception {
        // given (준비)
        Lineup originalLineup = new Lineup(
                savedMatch, savedMatchWaiting, savedMatchRequest, savedTeamMember1,
                UserPosition.GK, true
        );
        Lineup savedLineup = lineupRepository.save(originalLineup);
        Long savedLineupId = savedLineup.getId();
        LineupRequestDto updateDto = new LineupRequestDto(
                savedMatch.getMatchId(),
                savedMatchWaiting.getWaitingId(),
                savedMatchRequest.getRequestId(),
                savedTeamMember1.getId(),
                UserPosition.MF, false
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser1.getId(),
                null,
                Collections.emptyList()
        );

        // when (실행)
        ResultActions actions = mockMvc.perform(patch("/api/lineups/{id}", savedLineupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then (검증)
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLineupId))
                .andExpect(jsonPath("$.position").value("MF"))
                .andExpect(jsonPath("$.isStarter").value(false));
        Lineup updatedLineup = lineupRepository.findById(savedLineupId)
                .orElseThrow(() -> new AssertionError("라인업이 DB에 없습니다."));

        assertThat(updatedLineup.getPosition()).isEqualTo(UserPosition.MF);
        assertThat(updatedLineup.getIsStarter()).isFalse();
    }
}