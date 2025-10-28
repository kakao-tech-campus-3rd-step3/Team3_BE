package com.shootdoori.match.lineup;

import com.shootdoori.match.dto.LineupMemberRequestDto;
import com.shootdoori.match.dto.LineupMemberResponseDto;
import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.entity.lineup.LineupMember;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.common.Position;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import com.shootdoori.match.service.LineupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineupServiceTest {

    @InjectMocks
    private LineupService lineupService;

    @Mock
    private LineupRepository lineupRepository;
    @Mock
    private LineupMemberRepository lineupMemberRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;

    private LineupMemberRequestDto requestDto;
    private LineupMember savedLineupMember;
    private Lineup savedLineup;
    private TeamMember mockTeamMember;

    private final Long testTeamMemberId = 1L;
    private final Long createTestTeamMemberId = 2L;
    private final Long testLineupMemberId = 1L;
    private final Long createTestLineupMemberId = 2L;
    private final Long testLineupId = 1L;

    @BeforeEach
    void setUp() {
        mockTeamMember = mock(TeamMember.class);

        requestDto = new LineupMemberRequestDto(testTeamMemberId, Position.GK, true);

        savedLineup = new Lineup();
        ReflectionTestUtils.setField(savedLineup, "id", testLineupId);

        savedLineupMember = new LineupMember(mockTeamMember, savedLineup, Position.GK, true);
        ReflectionTestUtils.setField(savedLineupMember, "id", testLineupMemberId);
    }

    @Test
    @DisplayName("라인업 생성 - 성공 (여러 선수)")
    void createLineup_List_Success_Multiple() {
        // given
        TeamMember mockTeamMember2 = mock(TeamMember.class);
        given(mockTeamMember2.getId()).willReturn(createTestTeamMemberId);

        LineupMemberRequestDto requestDto2 = new LineupMemberRequestDto(createTestTeamMemberId, Position.DF, true);
        List<LineupMemberRequestDto> requestDtos = List.of(requestDto, requestDto2);

        LineupMember savedLineupMember2 = new LineupMember(mockTeamMember2, savedLineup, Position.DF, true);
        ReflectionTestUtils.setField(savedLineupMember2, "id", createTestLineupMemberId);

        List<LineupMember> savedLineupMembers = List.of(savedLineupMember, savedLineupMember2);

        Set<Long> teamMemberIds = Set.of(testTeamMemberId, createTestTeamMemberId);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(List.of(mockTeamMember, mockTeamMember2));
        doNothing().when(mockTeamMember).checkCaptainPermission(testTeamMemberId);
        given(lineupMemberRepository.saveAll(any(List.class))).willReturn(savedLineupMembers);
        given(mockTeamMember.getId()).willReturn(testTeamMemberId);

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.createLineup(requestDtos, testTeamMemberId);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.size()).isEqualTo(2);
        assertThat(responseDtos.get(0).id()).isEqualTo(testLineupMemberId);
        assertThat(responseDtos.get(0).position()).isEqualTo(Position.GK);
        assertThat(responseDtos.get(1).id()).isEqualTo(createTestLineupMemberId);
        assertThat(responseDtos.get(1).position()).isEqualTo(Position.DF);
        verify(teamMemberRepository, times(1)).findAllById(teamMemberIds);
        verify(mockTeamMember, times(1)).checkCaptainPermission(testTeamMemberId);
        verify(lineupMemberRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    @DisplayName("라인업 생성 - 성공 (단일 선수)")
    void createLineup_List_Success_Single() {
        // given
        List<LineupMemberRequestDto> requestDtos = List.of(requestDto);
        List<LineupMember> savedLineupMembers = List.of(savedLineupMember);

        Set<Long> teamMemberIds = Set.of(testTeamMemberId);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(List.of(mockTeamMember));
        doNothing().when(mockTeamMember).checkCaptainPermission(testTeamMemberId);
        given(lineupMemberRepository.saveAll(any(List.class))).willReturn(savedLineupMembers);
        given(mockTeamMember.getId()).willReturn(testTeamMemberId);

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.createLineup(requestDtos, testTeamMemberId);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.size()).isEqualTo(1);
        assertThat(responseDtos.get(0).id()).isEqualTo(testLineupMemberId);
        verify(teamMemberRepository, times(1)).findAllById(teamMemberIds);
        verify(mockTeamMember, times(1)).checkCaptainPermission(testTeamMemberId);
        verify(lineupMemberRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    @DisplayName("라인업 생성 - 실패 (팀 멤버 조회 실패)")
    void createLineup_List_Failure_TeamMemberNotFound() {
        // given
        List<LineupMemberRequestDto> requestDtos = List.of(requestDto);
        Set<Long> teamMemberIds = Set.of(testTeamMemberId);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(Collections.emptyList());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.createLineup(requestDtos, testTeamMemberId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        verify(teamMemberRepository, times(1)).findAllById(teamMemberIds);
        verify(mockTeamMember, never()).checkCaptainPermission(anyLong());
        verify(lineupMemberRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("ID로 라인업 조회 - 성공")
    void getAllLineupsByTeamId_Success() {
        // given
        List<LineupMember> lineupMembers = List.of(savedLineupMember);
        given(lineupMemberRepository.findAllByLineupId(testLineupId)).willReturn(lineupMembers);

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.getLineupById(testLineupId);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.size()).isEqualTo(1);
        assertThat(responseDtos.get(0).id()).isEqualTo(testLineupMemberId);
        assertThat(responseDtos.get(0).position()).isEqualTo(Position.GK);
    }

    @Test
    @DisplayName("ID로 라인업 조회 - 실패 (존재하지 않음)")
    void getLineupById_NotFound() {
        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.getLineupById(999L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);
    }

    @Test
    @DisplayName("라인업 수정 - 성공")
    void updateLineup_Success() {
        // given
        TeamMember mockTeamMember2 = mock(TeamMember.class);
        given(mockTeamMember2.getId()).willReturn(createTestTeamMemberId);

        LineupMemberRequestDto requestDto2 = new LineupMemberRequestDto(createTestTeamMemberId, Position.DF, true);
        List<LineupMemberRequestDto> updateDtos = List.of(requestDto, requestDto2);

        LineupMember savedLineupMember2 = new LineupMember(mockTeamMember2, savedLineup, Position.DF, true);
        ReflectionTestUtils.setField(savedLineupMember2, "id", createTestLineupMemberId);

        List<LineupMember> savedLineupMembers = List.of(savedLineupMember, savedLineupMember2);

        Set<Long> teamMemberIds = Set.of(testTeamMemberId, createTestTeamMemberId);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(List.of(mockTeamMember, mockTeamMember2));
        doNothing().when(mockTeamMember).checkCaptainPermission(testTeamMemberId);
        given(lineupMemberRepository.saveAll(any(List.class))).willReturn(savedLineupMembers);
        given(mockTeamMember.getId()).willReturn(testTeamMemberId);
        given(lineupRepository.findById(testLineupId)).willReturn(Optional.ofNullable(savedLineup));

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.updateLineup(testLineupId, updateDtos, testTeamMemberId);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.get(0).position()).isEqualTo(Position.GK);
        assertThat(responseDtos.get(0).isStarter()).isTrue();
        assertThat(responseDtos.get(1).position()).isEqualTo(Position.DF);
        assertThat(responseDtos.get(1).isStarter()).isTrue();
        assertThat(savedLineupMember.getPosition()).isEqualTo(Position.GK);
        assertThat(savedLineupMember.getIsStarter()).isTrue();
        assertThat(savedLineupMember2.getPosition()).isEqualTo(Position.DF);
        assertThat(savedLineupMember2.getIsStarter()).isTrue();
    }

    @Test
    @DisplayName("라인업 수정 - 실패 (라인업 없음)")
    void updateLineup_NotFound() {
        // given
        List<LineupMemberRequestDto> updateDtos = List.of(new LineupMemberRequestDto(createTestTeamMemberId, Position.DF, false));

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.updateLineup(999L, updateDtos, testTeamMemberId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);
    }

    @Test
    @DisplayName("라인업 삭제 - 성공")
    void deleteLineup_Success() {
        // given
        given(lineupMemberRepository.findFirstByLineupId(testLineupId)).willReturn(Optional.of(savedLineupMember));
        doNothing().when(lineupMemberRepository).deleteAllByLineupId(testLineupId);
        doNothing().when(lineupRepository).deleteById(testLineupId);

        // when
        lineupService.deleteLineup(testLineupId, testTeamMemberId);

        // then
        verify(lineupMemberRepository, times(1)).deleteAllByLineupId(testLineupId);
        verify(lineupRepository, times(1)).deleteById(testLineupId);
    }

    @Test
    @DisplayName("라인업 삭제 - 실패 (라인업 없음)")
    void deleteLineup_NotFound() {
        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.deleteLineup(999L, testLineupId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);
        verify(lineupMemberRepository, never()).deleteAllByLineupId(999L);
        verify(lineupRepository, never()).deleteById(testLineupId);
    }
}
