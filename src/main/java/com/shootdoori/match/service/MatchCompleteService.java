package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchTeamRequestDto;
import com.shootdoori.match.dto.TeamResponseDto;
import com.shootdoori.match.entity.Match;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.stereotype.Service;

@Service
public class MatchCompleteService {
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public MatchCompleteService(TeamRepository teamRepository, MatchRepository matchRepository){
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * 매치 정보 조회 후 상대 팀 정보 조회하는 서비스
     * @param matchTeamRequestDto 매치 id, 내 팀 id
     * @return TeamResponseDto 적 팀 정보
     */
    public TeamResponseDto getEnemyTeam(MatchTeamRequestDto matchTeamRequestDto) {
        Long enemyTeamId;
        Match match = matchRepository.findByMatchId(matchTeamRequestDto.matchId());

        Team enemyTeam = match.findEnemyTeam(matchTeamRequestDto.teamId());
        return new TeamResponseDto(enemyTeam);
    }
}
