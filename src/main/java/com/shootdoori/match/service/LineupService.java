package com.shootdoori.match.service;

import com.shootdoori.match.dto.LineupMemberRequestDto;
import com.shootdoori.match.dto.LineupMemberResponseDto;
import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.entity.lineup.LineupMember;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
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
    private final LineupRepository lineupRepository;

    public LineupService(LineupMemberRepository lineupMemberRepository,
                         TeamMemberRepository teamMemberRepository,
                         LineupRepository lineupRepository) {
        this.lineupMemberRepository = lineupMemberRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.lineupRepository = lineupRepository;
    }

    public List<LineupMemberResponseDto> getLineupById(Long lineupId) {
        return lineupMemberRepository.findAllByLineupId(lineupId).stream()
                .map(LineupMemberResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LineupMemberResponseDto> createLineup(List<LineupMemberRequestDto> requestDtos, Long userId) {
        Lineup lineup = new Lineup();
        lineupRepository.save(lineup);

        Map<Long, TeamMember> teamMemberMap = createTeamMemberMap(requestDtos);

        // 모든 팀원이 같은 팀에 속한다고 가정하고, 대표로 한 명만 권한 검사를 수행합니다.
        TeamMember representativeMember = teamMemberMap.values().iterator().next();
        representativeMember.checkCaptainPermission(userId);

        List<LineupMember> lineupsToSave = requestDtos.stream()
                .map(dto -> createLineupMemberFromList(dto, teamMemberMap, lineup))
                .collect(Collectors.toList());

        List<LineupMember> savedLineupMembers = lineupMemberRepository.saveAll(lineupsToSave);
        return savedLineupMembers.stream()
                .map(LineupMemberResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LineupMemberResponseDto> updateLineup(Long id, List<LineupMemberRequestDto> requestDtos, Long userId) {
        Lineup lineup = lineupRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));

        lineupMemberRepository.deleteAllByLineupId(id);

        Map<Long, TeamMember> teamMemberMap = createTeamMemberMap(requestDtos);

        // 모든 팀원이 같은 팀에 속한다고 가정하고, 대표로 한 명만 권한 검사를 수행합니다.
        TeamMember representativeMember = teamMemberMap.values().iterator().next();
        representativeMember.checkCaptainPermission(userId);

        List<LineupMember> lineupsToSave = requestDtos.stream()
                .map(dto -> createLineupMemberFromList(dto, teamMemberMap, lineup))
                .collect(Collectors.toList());

        List<LineupMember> savedLineupMembers = lineupMemberRepository.saveAll(lineupsToSave);
        return savedLineupMembers.stream()
                .map(LineupMemberResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteLineup(Long id, Long userId) {
        LineupMember lineupMember = lineupMemberRepository.findFirstByLineupId(id).orElseThrow(() -> new NotFoundException(ErrorCode.LINEUP_NOT_FOUND));
        lineupMember.getTeamMember().checkCaptainPermission(userId);
        lineupMemberRepository.deleteAllByLineupId(id);
        lineupRepository.deleteById(id);
    }

    private Map<Long, TeamMember> createTeamMemberMap(List<LineupMemberRequestDto> requestDtos) {
        Set<Long> teamMemberIds = requestDtos.stream()
                .map(LineupMemberRequestDto::teamMemberId)
                .collect(Collectors.toSet());

        Map<Long, TeamMember> teamMemberMap = teamMemberRepository.findAllById(teamMemberIds).stream()
                .collect(Collectors.toMap(TeamMember::getId, teamMember -> teamMember));

        if (teamMemberIds.size() != teamMemberMap.size()) {
            throw new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }

        return teamMemberMap;
    }

    private LineupMember createLineupMemberFromList(LineupMemberRequestDto dto, Map<Long, TeamMember> teamMemberMap, Lineup lineup) {
        TeamMember teamMember = teamMemberMap.get(dto.teamMemberId());
        if (teamMember == null) {
            throw new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }

        return new LineupMember(
                teamMember,
                lineup,
                dto.position(),
                dto.isStarter()
        );
    }
}
