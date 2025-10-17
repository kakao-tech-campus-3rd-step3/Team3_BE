package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchSummaryProjection;
import com.shootdoori.match.dto.RecentMatchesResponseDto;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.repository.MatchRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class MatchStartService {

    private final MatchRepository matchRepository;
    private final TeamMemberService teamMemberService;

    public MatchStartService(MatchRepository matchRepository,
                             TeamMemberService teamMemberService) {
        this.matchRepository = matchRepository;
        this.teamMemberService = teamMemberService;
    }

    @Transactional(readOnly = true)
    public List<RecentMatchesResponseDto> getMatchesByStatus(
        Long loginUserId,
        MatchStatus status,
        LocalDate cursorDate,
        LocalTime cursorTime,
        Pageable pageable
    ) {
        TeamMember teamMember = teamMemberService.findByIdForEntity(loginUserId);

        Slice<MatchSummaryProjection> slice = matchRepository.findMatchSummariesByTeamIdAndStatus(
            teamMember.getTeamId(),
            status,
            cursorDate,
            cursorTime,
            pageable
        );

        return slice.getContent().stream()
            .map(RecentMatchesResponseDto::from)
            .toList();
    }
}
