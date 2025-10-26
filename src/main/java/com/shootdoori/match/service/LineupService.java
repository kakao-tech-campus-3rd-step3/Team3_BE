package com.shootdoori.match.service;

import com.shootdoori.match.dto.LineupMemberRequestDto;
import com.shootdoori.match.dto.LineupMemberResponseDto;
import com.shootdoori.match.entity.lineup.LineupMember;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.exception.common.CreationFailException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineupService {

    private final LineupMemberRepository lineupMemberRepository;
    private final TeamMemberRepository teamMemberRepository;

    public LineupService(LineupMemberRepository lineupMemberRepository,
                         TeamMemberRepository teamMemberRepository) {
        this.lineupMemberRepository = lineupMemberRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public List<LineupMemberResponseDto> getAllLineupsByTeamId(Long teamId) {
        return lineupMemberRepository.findByTeamMemberTeamTeamId(teamId).stream()
                .map(LineupMemberResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LineupMemberResponseDto> createLineup(List<LineupMemberRequestDto> requestDtos, Long userId) {

        Set<Long> teamMemberIds = requestDtos.stream()
                .map(LineupMemberRequestDto::teamMemberId)
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
                            teamMember,
                            dto.position(),
                            dto.isStarter()
                    );
                })
                .collect(Collectors.toList());

        try {
            List<LineupMember> savedLineupMembers = lineupMemberRepository.saveAllAndFlush(lineupsToSave);
            return savedLineupMembers.stream()
                    .map(LineupMemberResponseDto::from)
                    .collect(Collectors.toList());
        } catch (DataIntegrityViolationException e) {
            throw new CreationFailException(ErrorCode.LINEUP_CREATION_FAILED);
        }
    }

    public LineupMemberResponseDto getLineupById(Long id) {
        LineupMember lineupMember = lineupMemberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
        return LineupMemberResponseDto.from(lineupMember);
    }

    public LineupMember findByIdForEntity(Long id) {
        return lineupMemberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
    }

    @Transactional
    public LineupMemberResponseDto updateLineup(Long id, LineupMemberRequestDto requestDto, Long userId) {
        LineupMember lineupMember = lineupMemberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));

        lineupMember.getTeamMember().checkCaptainPermission(userId);

        lineupMember.update(
                requestDto.position(),
                requestDto.isStarter()
        );

        return LineupMemberResponseDto.from(lineupMember);
    }

    @Transactional
    public void deleteLineup(Long id, Long userId) {
        LineupMember lineupMember = lineupMemberRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
        lineupMember.getTeamMember().checkCaptainPermission(userId);
        lineupMemberRepository.delete(lineupMember);
    }
}
