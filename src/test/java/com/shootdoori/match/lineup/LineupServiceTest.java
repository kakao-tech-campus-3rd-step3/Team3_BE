package com.shootdoori.match.lineup;

import com.shootdoori.match.dto.LineupRequestDto;
import com.shootdoori.match.dto.LineupResponseDto;
import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.entity.lineup.LineupStatus;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.user.UserPosition;
import com.shootdoori.match.exception.common.CreationFailException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import com.shootdoori.match.service.LineupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private MatchRepository matchRepository;
    @Mock
    private MatchWaitingRepository matchWaitingRepository;
    @Mock
    private MatchRequestRepository matchRequestRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;

    private LineupRequestDto requestDto;
    private Lineup savedLineup; // 리포지토리에서 반환될 엔티티
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

        requestDto = new LineupRequestDto(
                1L, // matchId
                1L, // waitingId
                1L, // requestId
                1L, // teamMemberId
                UserPosition.GK,
                true
        );

        savedLineup = new Lineup(
                mockMatch,
                mockMatchWaiting,
                mockMatchRequest,
                mockTeamMember,
                UserPosition.GK,
                true
        );

        ReflectionTestUtils.setField(savedLineup, "id", 1L);
    }

    @Test
    @DisplayName("라인업 생성 - 성공")
    void createLineup_Success() {
        // given
        given(matchRepository.getReferenceById(1L)).willReturn(mockMatch);
        given(matchWaitingRepository.getReferenceById(1L)).willReturn(mockMatchWaiting);
        given(matchRequestRepository.getReferenceById(1L)).willReturn(mockMatchRequest);
        given(teamMemberRepository.getReferenceById(1L)).willReturn(mockTeamMember);

        given(lineupRepository.saveAndFlush(any(Lineup.class))).willReturn(savedLineup);

        // when
        LineupResponseDto responseDto = lineupService.createLineup(requestDto);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isEqualTo(1L);
        assertThat(responseDto.position()).isEqualTo(UserPosition.GK);
        assertThat(responseDto.isStarter()).isTrue();
    }

    @Test
    @DisplayName("라인업 생성 - 실패 (DB 제약 조건 위반)")
    void createLineup_Failure_DataIntegrityViolation() {
        // given
        given(matchRepository.getReferenceById(1L)).willReturn(mockMatch);
        given(matchWaitingRepository.getReferenceById(1L)).willReturn(mockMatchWaiting);
        given(matchRequestRepository.getReferenceById(1L)).willReturn(mockMatchRequest);
        given(teamMemberRepository.getReferenceById(1L)).willReturn(mockTeamMember);
        given(lineupRepository.saveAndFlush(any(Lineup.class)))
                .willThrow(new DataIntegrityViolationException("Test DB Error"));

        // when & then
        CreationFailException exception = assertThrows(CreationFailException.class, () -> {
            lineupService.createLineup(requestDto);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_CREATION_FAILED);

        // verify
        verify(lineupRepository, times(1)).saveAndFlush(any(Lineup.class));
    }

    @Test
    @DisplayName("팀 ID로 모든 라인업 조회 - 성공 (결과 1개)")
    void getAllLineupsByTeamId_Success() {
        // given
        List<Lineup> lineups = List.of(savedLineup);
        given(lineupRepository.findByTeamMemberTeamTeamId(1L)).willReturn(lineups);

        // when
        List<LineupResponseDto> responseDtos = lineupService.getAllLineupsByTeamId(1L);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.size()).isEqualTo(1);
        assertThat(responseDtos.get(0).id()).isEqualTo(1L);
        assertThat(responseDtos.get(0).position()).isEqualTo(UserPosition.GK);

        // verify
        verify(lineupRepository, times(1)).findByTeamMemberTeamTeamId(1L);
    }

    @Test
    @DisplayName("팀 ID로 모든 라인업 조회 - 성공 (결과 없음)")
    void getAllLineupsByTeamId_Success_Empty() {
        // given
        given(lineupRepository.findByTeamMemberTeamTeamId(1L)).willReturn(Collections.emptyList());

        // when
        List<LineupResponseDto> responseDtos = lineupService.getAllLineupsByTeamId(1L);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos).isEmpty();

        // verify
        verify(lineupRepository, times(1)).findByTeamMemberTeamTeamId(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (DTO) - 성공")
    void getLineupById_Success() {
        // given
        given(lineupRepository.findById(1L)).willReturn(Optional.of(savedLineup));

        // when
        LineupResponseDto responseDto = lineupService.getLineupById(1L);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isEqualTo(1L);
        assertThat(responseDto.position()).isEqualTo(UserPosition.GK);

        // verify
        verify(lineupRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (DTO) - 실패 (라인업 없음)")
    void getLineupById_NotFound() {
        // given
        given(lineupRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.getLineupById(1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);

        // verify
        verify(lineupRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (Entity) - 성공")
    void findByIdForEntity_Success() {
        // given
        given(lineupRepository.findById(1L)).willReturn(Optional.of(savedLineup));

        // when
        Lineup foundEntity = lineupService.findByIdForEntity(1L);

        // then
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getId()).isEqualTo(1L);
        assertThat(foundEntity).isEqualTo(savedLineup);

        // verify
        verify(lineupRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 ID로 조회 (Entity) - 실패 (라인업 없음)")
    void findByIdForEntity_NotFound() {
        // given
        given(lineupRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.findByIdForEntity(1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);

        // verify
        verify(lineupRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("라인업 수정 - 성공")
    void updateLineup_Success() {
        // given
        LineupRequestDto updateDto = new LineupRequestDto(2L, 2L, 2L, 2L, UserPosition.DF, false);

        Match mockMatch2 = mock(Match.class);
        MatchWaiting mockMatchWaiting2 = mock(MatchWaiting.class);
        MatchRequest mockMatchRequest2 = mock(MatchRequest.class);

        given(matchRepository.getReferenceById(2L)).willReturn(mockMatch2);
        given(matchWaitingRepository.getReferenceById(2L)).willReturn(mockMatchWaiting2);
        given(matchRequestRepository.getReferenceById(2L)).willReturn(mockMatchRequest2);

        given(lineupRepository.findById(1L)).willReturn(Optional.of(savedLineup));

        given(mockMatch2.getMatchId()).willReturn(2L);
        given(mockMatchWaiting2.getWaitingId()).willReturn(2L);
        given(mockMatchRequest2.getRequestId()).willReturn(2L);

        // when
        LineupResponseDto responseDto = lineupService.updateLineup(1L, updateDto);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.position()).isEqualTo(UserPosition.DF);
        assertThat(responseDto.isStarter()).isFalse();
        assertThat(responseDto.matchId()).isEqualTo(2L); // ID도 변경되었는지 확인
        assertThat(savedLineup.getPosition()).isEqualTo(UserPosition.DF);
        assertThat(savedLineup.getIsStarter()).isFalse();
        assertThat(savedLineup.getMatch()).isEqualTo(mockMatch2); // 내부 엔티티 참조가 변경되었는지 확인

        // verify
        verify(lineupRepository, times(1)).findById(1L);
        verify(matchRepository, times(1)).getReferenceById(2L);
        verify(matchWaitingRepository, times(1)).getReferenceById(2L);
        verify(matchRequestRepository, times(1)).getReferenceById(2L);
    }

    @Test
    @DisplayName("라인업 수정 - 실패 (라인업 없음)")
    void updateLineup_NotFound() {
        // given
        LineupRequestDto updateDto = new LineupRequestDto(2L, 2L, 2L, 2L, UserPosition.DF, false);
        given(lineupRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.updateLineup(1L, updateDto);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);

        // verify
        verify(lineupRepository, times(1)).findById(1L);
        verify(matchRepository, never()).getReferenceById(anyLong());
        verify(matchWaitingRepository, never()).getReferenceById(anyLong());
        verify(matchRequestRepository, never()).getReferenceById(anyLong());
    }

    @Test
    @DisplayName("라인업 삭제 - 성공")
    void deleteLineup_Success() {
        // given
        given(lineupRepository.existsById(1L)).willReturn(true);
        doNothing().when(lineupRepository).deleteById(1L);

        // when
        lineupService.deleteLineup(1L);

        // then

        // verify
        verify(lineupRepository, times(1)).existsById(1L);
        verify(lineupRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("라인업 삭제 - 실패 (라인업 없음)")
    void deleteLineup_NotFound() {
        // given
        given(lineupRepository.existsById(1L)).willReturn(false);

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lineupService.deleteLineup(1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LINEUP_NOT_FOUND);

        // verify
        verify(lineupRepository, times(1)).existsById(1L);
        verify(lineupRepository, never()).deleteById(1L);
    }
}