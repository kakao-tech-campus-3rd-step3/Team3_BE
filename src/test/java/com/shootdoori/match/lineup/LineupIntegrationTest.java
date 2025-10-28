package com.shootdoori.match.lineup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shootdoori.match.dto.LineupMemberRequestDto;
import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.entity.lineup.LineupMember;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.common.Position;
import com.shootdoori.match.repository.*;
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
class LineupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired private LineupRepository lineupRepository;
    @Autowired private LineupMemberRepository lineupMemberRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private ProfileRepository profileRepository;

    private User savedUser1, savedUser2;
    private Team savedTeam1, savedTeam2;
    private TeamMember savedTeamMember1, savedTeamMember2;
    private Lineup savedLineup1;
    private Authentication authentication, fakeAuth;
    private LineupMemberRequestDto lineupMemberRequestDto1, lineupMemberRequestDto2;

    @BeforeEach
    void setUp() {
        savedLineup1 = new Lineup();
        lineupRepository.save(savedLineup1);

        savedUser1 = User.create("주장1", "아마추어", "captain1@test.ac.kr", "password123",
                "captain1_kakao", "FW", "테스트대학교", "컴퓨터공학과", "20", "주장1입니다.");
        savedUser2 = User.create("주장2", "세미프로", "captain2@test.ac.kr", "password123",
                "captain2_kakao", "DF", "테스트대학교", "경영학과", "21", "주장2입니다.");
        profileRepository.saveAll(List.of(savedUser1, savedUser2));

        savedTeam1 = new Team("팀A", savedUser1, "테스트대학교", TeamType.CENTRAL_CLUB,
                SkillLevel.AMATEUR, "팀A입니다.");
        savedTeam2 = new Team("팀B", savedUser2, "테스트대학교", TeamType.DEPARTMENT_CLUB,
                SkillLevel.SEMI_PRO, "팀B입니다.");
        teamRepository.saveAll(List.of(savedTeam1, savedTeam2));

        savedTeamMember1 = new TeamMember(savedTeam1, savedUser1, TeamMemberRole.LEADER);
        savedTeamMember2 = new TeamMember(savedTeam2, savedUser2, TeamMemberRole.LEADER);
        teamMemberRepository.saveAll(List.of(savedTeamMember1, savedTeamMember2));

        authentication = new UsernamePasswordAuthenticationToken(
                savedUser1.getId(),
                null,
                Collections.emptyList()
        );

        fakeAuth = new UsernamePasswordAuthenticationToken(
                999L,
                null,
                Collections.emptyList()
        );

        lineupMemberRequestDto1 = new LineupMemberRequestDto(
                savedTeamMember1.getId(),
                Position.GK,
                true
        );

        lineupMemberRequestDto2 = new LineupMemberRequestDto(
                savedTeamMember2.getId(),
                Position.DF,
                true
        );
    }

    @Test
    @DisplayName("POST /api/lineups - 라인업 생성 (단일 항목 리스트) (201 CREATED)")
    void createLineup_List_Success_SingleItem_IntegrationTest() throws Exception {
        // given
        List<LineupMemberRequestDto> requestList = List.of(lineupMemberRequestDto1);

        // when
        ResultActions actions = mockMvc.perform(post("/api/lineups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList))
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then
        MvcResult result = actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].position").value("GK"))
                .andExpect(jsonPath("$[0].teamMemberId").value(savedTeamMember1.getId()))
                .andDo(print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Long savedLineupId = objectMapper.readTree(jsonResponse).get(0).get("id").asLong();

        assertThat(lineupMemberRepository.findById(savedLineupId)).isPresent();
    }

    @Test
    @DisplayName("POST /api/lineups - 라인업 생성 (다중 항목 리스트) (201 CREATED)")
    void createLineup_List_Success_IntegrationTest() throws Exception {
        // given
        List<LineupMemberRequestDto> requestList = List.of(lineupMemberRequestDto1, lineupMemberRequestDto2);

        // when
        ResultActions actions = mockMvc.perform(post("/api/lineups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList))
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then
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
        assertThat(lineupMemberRepository.findById(savedLineupId1)).isPresent();
        assertThat(lineupMemberRepository.findById(savedLineupId2)).isPresent();
    }

    @Test
    @DisplayName("POST /api/lineups - 라인업 생성 실패 (권한 없음) (403 Forbidden)")
    void createLineup_List_Forbidden() throws Exception {
        // given
        List<LineupMemberRequestDto> requestList = List.of(lineupMemberRequestDto1, lineupMemberRequestDto2);

        // when
        ResultActions actions = mockMvc.perform(post("/api/lineups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList))
                .with(SecurityMockMvcRequestPostProcessors.authentication(fakeAuth)));

        // then
        MvcResult result = actions.andExpect(status().isForbidden())
                .andDo(print())
                .andReturn();
    }


    @Test
    @DisplayName("GET /api/lineups/{id} - 라인업 단건 조회 통합 테스트 (200 OK)")
    void getLineupById_IntegrationTest() throws Exception {
        // given
        LineupMember testLineupMember = new LineupMember(
                savedTeamMember1,
                savedLineup1,
                Position.FW, true
        );
        LineupMember savedLineupMember = lineupMemberRepository.save(testLineupMember);
        Long savedLineupId = savedLineup1.getId();
        Long savedLineupMemberId = savedLineupMember.getId();

        // when
        ResultActions actions = mockMvc.perform(get("/api/lineups/{id}", savedLineupId)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedLineupMemberId))
                .andExpect(jsonPath("$[0].position").value("FW"));
    }

    @Test
    @DisplayName("GET /api/lineups/{id} - 라인업 조회 실패 통합 테스트 (404 NOT_FOUND)")
    void getLineupById_NotFound_IntegrationTest() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/lineups/{id}", 999L)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("DELETE /api/lineups/{id} - 라인업 삭제 성공 (204 NO_CONTENT)")
    void deleteLineup_Success_IntegrationTest() throws Exception {
        // given
        LineupMember testLineupMember = new LineupMember(
                savedTeamMember1,
                savedLineup1,
                Position.DF, false
        );
        LineupMember savedLineupMember = lineupMemberRepository.save(testLineupMember);
        Long savedLineupId = savedLineup1.getId();
        Long savedLineupMemberId = savedLineupMember.getId();

        // when
        ResultActions actions = mockMvc.perform(delete("/api/lineups/{id}", savedLineupId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then
        actions.andExpect(status().isNoContent());
        assertThat(lineupMemberRepository.existsById(savedLineupMemberId)).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/lineups/{id} - 라인업 삭제 실패 (403 Forbidden)")
    void deleteLineup_Forbidden_IntegrationTest() throws Exception {
        LineupMember testLineupMember = new LineupMember(
                savedTeamMember1,
                savedLineup1,
                Position.DF, false
        );
        LineupMember savedLineupMember = lineupMemberRepository.save(testLineupMember);
        Long savedLineupId = savedLineup1.getId();
        Long savedLineupMemberId = savedLineupMember.getId();

        // when
        ResultActions actions = mockMvc.perform(delete("/api/lineups/{id}", savedLineupId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(fakeAuth)));

        // then
        actions.andExpect(status().isForbidden());
        assertThat(lineupMemberRepository.existsById(savedLineupMemberId)).isTrue();
    }


    @Test
    @DisplayName("PATCH /api/lineups/{id} - 라인업 수정 테스트 성공 (200 OK)")
    void updateLineup_Success_IntegrationTest() throws Exception {
        // given
        LineupMember originalLineupMember = new LineupMember(
                savedTeamMember1,
                savedLineup1,
                Position.GK,
                true
        );
        lineupMemberRepository.save(originalLineupMember);

        Long savedLineupId = savedLineup1.getId();
        LineupMemberRequestDto updateDto = new LineupMemberRequestDto(
                savedTeamMember1.getId(),
                Position.MF,
                false
        );

        List<LineupMemberRequestDto> updateList = List.of(updateDto);

        // when
        ResultActions actions = mockMvc.perform(patch("/api/lineups/{id}", savedLineupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateList))
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value("MF"))
                .andExpect(jsonPath("$[0].isStarter").value(false));
    }

    @Test
    @DisplayName("PATCH /api/lineups/{id} - 라인업 수정 실패 (403 Forbidden)")
    void updateLineup_Forbidden_IntegrationTest() throws Exception {
        // given
        LineupMember originalLineupMember = new LineupMember(
                savedTeamMember1,
                savedLineup1,
                Position.GK,
                true
        );
        LineupMember savedLineupMember = lineupMemberRepository.save(originalLineupMember);
        Long savedLineupId = savedLineup1.getId();
        Long savedLineupMemberId = savedLineupMember.getId();

        List<LineupMemberRequestDto> requestList = List.of(lineupMemberRequestDto2);

        // when
        ResultActions actions = mockMvc.perform(patch("/api/lineups/{id}", savedLineupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList))
                .with(SecurityMockMvcRequestPostProcessors.authentication(fakeAuth)));

        // then
        actions.andExpect(status().isForbidden());
        assertThat(lineupMemberRepository.existsById(savedLineupMemberId)).isTrue();
    }
}