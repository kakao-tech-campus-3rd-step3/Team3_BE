package com.shootdoori.match.service;

import com.shootdoori.match.dto.LineupRequestDto;
import com.shootdoori.match.dto.LineupResponseDto;
import com.shootdoori.match.entity.lineup.LineupMember;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.exception.common.CreationFailException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineupService {

    private final LineupRepository lineupRepository;
    private final MatchRepository matchRepository;
    private final MatchWaitingRepository matchWaitingRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final TeamMemberRepository teamMemberRepository;

    public LineupService(LineupRepository lineupRepository,
                         MatchRepository matchRepository,
                         MatchWaitingRepository matchWaitingRepository,
                         MatchRequestRepository matchRequestRepository,
                         TeamMemberRepository teamMemberRepository) {
        this.lineupRepository = lineupRepository;
        this.matchRepository = matchRepository;
        this.matchWaitingRepository = matchWaitingRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public List<LineupResponseDto> getAllLineupsByTeamId(Long teamId) {
        return lineupRepository.findByTeamMemberTeamTeamId(teamId).stream()
                .map(LineupResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LineupResponseDto> createLineup(List<LineupRequestDto> requestDtos, Long userId) {
        if (requestDtos == null || requestDtos.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> teamMemberIds = requestDtos.stream()
                .map(LineupRequestDto::teamMemberId)
                .collect(Collectors.toSet());

        Map<Long, TeamMember> teamMemberMap = teamMemberRepository.findAllById(teamMemberIds).stream()
                .collect(Collectors.toMap(TeamMember::getId, teamMember -> teamMember));

        if (teamMemberIds.size() != teamMemberMap.size()) {
            throw new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }

        // 모든 팀원이 같은 팀에 속한다고 가정하고, 대표로 한 명만 권한 검사를 수행합니다.
        TeamMember representativeMember = teamMemberMap.values().iterator().next();
        representativeMember.checkCaptainPermission(userId);

        List<LineupMember> lineupsToSave = requestDtos.stream()
                .map(dto -> {
                    TeamMember teamMember = teamMemberMap.get(dto.teamMemberId());
                    if (teamMember == null) {
                        throw new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
                    }

                    return new LineupMember(
                            dto.matchId() != null ? matchRepository.getReferenceById(dto.matchId()) : null,
                            dto.waitingId() != null ? matchWaitingRepository.getReferenceById(dto.waitingId()) : null,
                            dto.requestId() != null ? matchRequestRepository.getReferenceById(dto.requestId()) : null,
                            teamMember,
                            dto.position(),
                            dto.isStarter()
                    );
                })
                .collect(Collectors.toList());

        try {
            List<LineupMember> savedLineupMembers = lineupRepository.saveAllAndFlush(lineupsToSave);
            return savedLineupMembers.stream()
                    .map(LineupResponseDto::from)
                    .collect(Collectors.toList());
        } catch (DataIntegrityViolationException e) {
            throw new CreationFailException(ErrorCode.LINEUP_CREATION_FAILED);
        }
    }

    public LineupResponseDto getLineupById(Long id) {
        LineupMember lineupMember = lineupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
        return LineupResponseDto.from(lineupMember);
    }

    public LineupMember findByIdForEntity(Long id) {
        return lineupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
    }

    @Transactional
    public LineupResponseDto updateLineup(Long id, LineupRequestDto requestDto, Long userId) {
        LineupMember lineupMember = lineupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));

        lineupMember.getTeamMember().checkCaptainPermission(userId);

        lineupMember.update(
                requestDto.matchId() != null ? matchRepository.getReferenceById(requestDto.matchId()) : null,
                requestDto.waitingId() != null ? matchWaitingRepository.getReferenceById(requestDto.waitingId()) : null,
                requestDto.requestId() != null ? matchRequestRepository.getReferenceById(requestDto.requestId()) : null,
                requestDto.position(),
                requestDto.isStarter()
        );

        return LineupResponseDto.from(lineupMember);
    }

    @Transactional
    public void deleteLineup(Long id, Long userId) {
        LineupMember lineupMember = lineupRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
        lineupMember.getTeamMember().checkCaptainPermission(userId);
        lineupRepository.delete(lineupMember);
    }
}
