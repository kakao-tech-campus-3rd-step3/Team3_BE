package com.shootdoori.match.service;

import com.shootdoori.match.dto.EnemyTeamResponseDto;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchCompleteService {

    private final MatchRepository matchRepository;
    private final TeamMemberService teamMemberService;

    public MatchCompleteService(MatchRepository matchRepository,
                                TeamMemberService teamMemberService) {
        this.matchRepository = matchRepository;
        this.teamMemberService = teamMemberService;
    }

    @Transactional(readOnly = true)
    public EnemyTeamResponseDto findEnemyTeam(Long loginUserId, Long matchId) {
        Match match = findByIdForEntity(matchId);

        TeamMember teamMember = teamMemberService.findByIdForEntity(loginUserId);

        Team enemyTeam = match.findEnemyTeam(teamMember);
        return EnemyTeamResponseDto.from(enemyTeam);
    }

    @Transactional(readOnly = true)
    public Match findByIdForEntity(Long matchId) {
        return matchRepository.findById(matchId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_NOT_FOUND,
                String.valueOf(matchId)));
    }

    @Transactional
    public void deleteAllByTeamId(Long teamId) {
        matchRepository.deleteAllByTeamId(teamId);
    }

}
