package com.shootdoori.match.lineup;

import com.shootdoori.match.dto.LineupMemberRequestDto;
import com.shootdoori.match.dto.LineupMemberResponseDto;
import com.shootdoori.match.entity.lineup.LineupMember;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.common.Position;
import com.shootdoori.match.exception.common.CreationFailException;
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
import org.springframework.dao.DataIntegrityViolationException;
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
    private LineupMemberRepository lineupMemberRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchWaitingRepository matchWaitingRepository;
    @Mock
    private MatchRequestRepository matchRequestRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;

    private LineupMemberRequestDto requestDto;
    private LineupMember savedLineupMember;
    private Match mockMatch;
    private MatchWaiting mockMatchWaiting;
    private MatchRequest mockMatchRequest;
    private TeamMember mockTeamMember;

    @BeforeEach
    void setUp() {
        mockMatch = mock(Match.class);
        mockMatchWaiting = mock(MatchWaiting.class);
        mockMatchRequest = mock(MatchRequest.class);
        mockTeamMember = mock(TeamMember.class);

        requestDto = new LineupMemberRequestDto(
                1L,
                1L,
                1L,
                1L,
                Position.GK,
                true
        );

        savedLineupMember = new LineupMember(
                mockMatch,
                mockMatchWaiting,
                mockMatchRequest,
                mockTeamMember,
                Position.GK,
                true
        );

        ReflectionTestUtils.setField(savedLineupMember, "id", 1L);
    }

    @Test
    @DisplayName("라인업 일괄 생성 - 성공 (여러 항목)")
    void createLineup_List_Success_Multiple() {
        // given
        TeamMember mockTeamMember2 = mock(TeamMember.class);
        given(mockTeamMember2.getId()).willReturn(2L);

        LineupMemberRequestDto requestDto2 = new LineupMemberRequestDto(1L, 1L, 1L, 2L, Position.DF, true);
        List<LineupMemberRequestDto> requestDtos = List.of(requestDto, requestDto2);

        LineupMember savedLineupMember2 = new LineupMember(mockMatch, mockMatchWaiting, mockMatchRequest, mockTeamMember2, Position.DF, true);
        ReflectionTestUtils.setField(savedLineupMember2, "id", 2L);

        List<LineupMember> savedLineupMembers = List.of(savedLineupMember, savedLineupMember2);

        Set<Long> teamMemberIds = Set.of(1L, 2L);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(List.of(mockTeamMember, mockTeamMember2));
        doNothing().when(mockTeamMember).checkCaptainPermission(1L);
        given(matchRepository.getReferenceById(1L)).willReturn(mockMatch);
        given(matchWaitingRepository.getReferenceById(1L)).willReturn(mockMatchWaiting);
        given(matchRequestRepository.getReferenceById(1L)).willReturn(mockMatchRequest);
        given(lineupMemberRepository.saveAllAndFlush(any(List.class))).willReturn(savedLineupMembers);
        given(mockTeamMember.getId()).willReturn(1L);

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.createLineup(requestDtos, 1L);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.size()).isEqualTo(2);
        assertThat(responseDtos.get(0).id()).isEqualTo(1L);
        assertThat(responseDtos.get(0).position()).isEqualTo(Position.GK);
        assertThat(responseDtos.get(1).id()).isEqualTo(2L);
        assertThat(responseDtos.get(1).position()).isEqualTo(Position.DF);
        verify(teamMemberRepository, times(1)).findAllById(teamMemberIds);
        verify(mockTeamMember, times(1)).checkCaptainPermission(1L);
        verify(lineupMemberRepository, times(1)).saveAllAndFlush(any(List.class));
    }

    @Test
    @DisplayName("라인업 일괄 생성 - 성공 (단일 항목)")
    void createLineup_List_Success_Single() {
        // given
        List<LineupMemberRequestDto> requestDtos = List.of(requestDto);
        List<LineupMember> savedLineupMembers = List.of(savedLineupMember);

        Set<Long> teamMemberIds = Set.of(1L);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(List.of(mockTeamMember));
        doNothing().when(mockTeamMember).checkCaptainPermission(1L);
        given(matchRepository.getReferenceById(1L)).willReturn(mockMatch);
        given(matchWaitingRepository.getReferenceById(1L)).willReturn(mockMatchWaiting);
        given(matchRequestRepository.getReferenceById(1L)).willReturn(mockMatchRequest);
        given(lineupMemberRepository.saveAllAndFlush(any(List.class))).willReturn(savedLineupMembers);
        given(mockTeamMember.getId()).willReturn(1L);

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.createLineup(requestDtos, 1L);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.size()).isEqualTo(1);
        assertThat(responseDtos.get(0).id()).isEqualTo(1L);
        verify(teamMemberRepository, times(1)).findAllById(teamMemberIds);
        verify(mockTeamMember, times(1)).checkCaptainPermission(1L);
        verify(lineupMemberRepository, times(1)).saveAllAndFlush(any(List.class));
    }

    @Test
    @DisplayName("라인업 일괄 생성 - 성공 (빈 리스트)")
    void createLineup_List_Success_EmptyList() {
        // given
        List<LineupMemberRequestDto> requestDtos = Collections.emptyList();

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.createLineup(requestDtos, 1L);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos).isEmpty();
        verify(teamMemberRepository, never()).findAllById(any());
        verify(lineupMemberRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("라인업 일괄 생성 - 실패 (팀 멤버 조회 실패)")
    void createLineup_List_Failure_TeamMemberNotFound() {
        // given
        List<LineupMemberRequestDto> requestDtos = List.of(requestDto);
        Set<Long> teamMemberIds = Set.of(1L);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(Collections.emptyList());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.createLineup(requestDtos, 1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        verify(teamMemberRepository, times(1)).findAllById(teamMemberIds);
        verify(mockTeamMember, never()).checkCaptainPermission(anyLong());
        verify(lineupMemberRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("라인업 일괄 생성 - 실패 (DB 제약 조건 위반)")
    void createLineup_List_Failure_DataIntegrityViolation() {
        // given
        List<LineupMemberRequestDto> requestDtos = List.of(requestDto);

        Set<Long> teamMemberIds = Set.of(1L);
        given(teamMemberRepository.findAllById(teamMemberIds)).willReturn(List.of(mockTeamMember));
        doNothing().when(mockTeamMember).checkCaptainPermission(1L);
        given(matchRepository.getReferenceById(1L)).willReturn(mockMatch);
        given(matchWaitingRepository.getReferenceById(1L)).willReturn(mockMatchWaiting);
        given(matchRequestRepository.getReferenceById(1L)).willReturn(mockMatchRequest);
        given(mockTeamMember.getId()).willReturn(1L);
        given(lineupMemberRepository.saveAllAndFlush(any(List.class)))
                .willThrow(new DataIntegrityViolationException("Test DB Error"));

        // when & then
        CreationFailException exception = assertThrows(CreationFailException.class, () -> {
            lineupService.createLineup(requestDtos, 1L);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_CREATION_FAILED);
        verify(lineupMemberRepository, times(1)).saveAllAndFlush(any(List.class));
    }

    @Test
    @DisplayName("매치 ID로 모든 라인업 조회 - 성공 (결과 1개)")
    void getAllLineupsByTeamId_Success() {
        // given
        List<LineupMember> lineupMembers = List.of(savedLineupMember);
        given(lineupMemberRepository.findByTeamMemberTeamTeamId(1L)).willReturn(lineupMembers);

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.getAllLineupsByTeamId(1L);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.size()).isEqualTo(1);
        assertThat(responseDtos.get(0).id()).isEqualTo(1L);
        assertThat(responseDtos.get(0).position()).isEqualTo(Position.GK);
        verify(lineupMemberRepository, times(1)).findByTeamMemberTeamTeamId(1L);
    }

    @Test
    @DisplayName("매치 ID로 모든 라인업 조회 - 성공 (결과 없음)")
    void getAllLineupsByTeamId_Success_Empty() {
        // given
        given(lineupMemberRepository.findByTeamMemberTeamTeamId(1L)).willReturn(Collections.emptyList());

        // when
        List<LineupMemberResponseDto> responseDtos = lineupService.getAllLineupsByTeamId(1L);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos).isEmpty();
        verify(lineupMemberRepository, times(1)).findByTeamMemberTeamTeamId(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (DTO) - 성공")
    void getLineupById_Success() {
        // given
        given(lineupMemberRepository.findById(1L)).willReturn(Optional.of(savedLineupMember));

        // when
        LineupMemberResponseDto responseDto = lineupService.getLineupById(1L);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isEqualTo(1L);
        assertThat(responseDto.position()).isEqualTo(Position.GK);
        verify(lineupMemberRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (DTO) - 실패 (라인업 없음)")
    void getLineupById_NotFound() {
        // given
        given(lineupMemberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.getLineupById(1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);
        verify(lineupMemberRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (Entity) - 성공")
    void findByIdForEntity_Success() {
        // given
        given(lineupMemberRepository.findById(1L)).willReturn(Optional.of(savedLineupMember));

        // when
        LineupMember foundEntity = lineupService.findByIdForEntity(1L);

        // then
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getId()).isEqualTo(1L);
        assertThat(foundEntity).isEqualTo(savedLineupMember);
        verify(lineupMemberRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (Entity) - 실패 (라인업 없음)")
    void findByIdForEntity_NotFound() {
        // given
        given(lineupMemberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.findByIdForEntity(1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);
        verify(lineupMemberRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 수정 - 성공")
    void updateLineup_Success() {
        // given
        LineupMemberRequestDto updateDto = new LineupMemberRequestDto(2L, 2L, 2L, 2L, Position.DF, false);

        Match mockMatch2 = mock(Match.class);
        MatchWaiting mockMatchWaiting2 = mock(MatchWaiting.class);
        MatchRequest mockMatchRequest2 = mock(MatchRequest.class);

        given(matchRepository.getReferenceById(2L)).willReturn(mockMatch2);
        given(matchWaitingRepository.getReferenceById(2L)).willReturn(mockMatchWaiting2);
        given(matchRequestRepository.getReferenceById(2L)).willReturn(mockMatchRequest2);

        given(lineupMemberRepository.findById(1L)).willReturn(Optional.of(savedLineupMember));

        given(mockMatch2.getMatchId()).willReturn(2L);
        given(mockMatchWaiting2.getWaitingId()).willReturn(2L);
        given(mockMatchRequest2.getRequestId()).willReturn(2L);

        // when
        LineupMemberResponseDto responseDto = lineupService.updateLineup(1L, updateDto, 1L);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.position()).isEqualTo(Position.DF);
        assertThat(responseDto.isStarter()).isFalse();
        assertThat(responseDto.matchId()).isEqualTo(2L);
        assertThat(savedLineupMember.getPosition()).isEqualTo(Position.DF);
        assertThat(savedLineupMember.getIsStarter()).isFalse();
        assertThat(savedLineupMember.getMatch()).isEqualTo(mockMatch2);
        verify(lineupMemberRepository, times(1)).findById(1L);
        verify(matchRepository, times(1)).getReferenceById(2L);
        verify(matchWaitingRepository, times(1)).getReferenceById(2L);
        verify(matchRequestRepository, times(1)).getReferenceById(2L);
    }

    @Test
    @DisplayName("라인업 수정 - 실패 (라인업 없음)")
    void updateLineup_NotFound() {
        // given
        LineupMemberRequestDto updateDto = new LineupMemberRequestDto(2L, 2L, 2L, 2L, Position.DF, false);
        given(lineupMemberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.updateLineup(1L, updateDto, 1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);
        verify(lineupMemberRepository, times(1)).findById(1L);
        verify(matchRepository, never()).getReferenceById(anyLong());
        verify(matchWaitingRepository, never()).getReferenceById(anyLong());
        verify(matchRequestRepository, never()).getReferenceById(anyLong());
    }

    @Test
    @DisplayName("라인업 삭제 - 성공")
    void deleteLineup_Success() {
        // given
        given(lineupMemberRepository.findById(1L)).willReturn(Optional.ofNullable(savedLineupMember));
        doNothing().when(lineupMemberRepository).delete(savedLineupMember);

        // when
        lineupService.deleteLineup(1L, 1L);

        // then
        verify(lineupMemberRepository, times(1)).findById(1L);
        verify(lineupMemberRepository, times(1)).delete(savedLineupMember);
    }

    @Test
    @DisplayName("라인업 삭제 - 실패 (라인업 없음)")
    void deleteLineup_NotFound() {
        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.deleteLineup(2L, 1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);
        verify(lineupMemberRepository, times(1)).findById(2L);
        verify(lineupMemberRepository, never()).deleteById(1L);
    }
}
