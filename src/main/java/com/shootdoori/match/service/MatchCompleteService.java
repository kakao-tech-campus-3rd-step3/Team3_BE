package com.shootdoori.match.service;

import com.shootdoori.match.dto.EnemyTeamResponseDto;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchCompleteService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamMemberRepository teamMemberRepository;

    public MatchCompleteService(TeamRepository teamRepository,
                                MatchRepository matchRepository,
                                TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Transactional(readOnly = true)
    public EnemyTeamResponseDto findEnemyTeam(Long loginUserId, Long matchId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_NOT_FOUND,
                String.valueOf(matchId)));

        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team myTeam = teamMember.getTeam();

        Team enemyTeam = match.findEnemyTeam(myTeam);
        return EnemyTeamResponseDto.from(enemyTeam);
    }

    @Transactional
    public void deleteAllByTeamId(Long teamId) {
        matchRepository.deleteAllByTeamId(teamId);
    }
}
