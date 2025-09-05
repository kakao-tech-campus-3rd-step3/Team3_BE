package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchTeamRequestDto;
import org.springframework.stereotype.Service;

@Service
public class MatchCompleteService {
    private TeamRepository teamRepository;
    private MatchRepository matchRepository;

    public MatchCompleteService(TeamRepository teamRepository, MatchRepository matchRepository){
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public MatchTeamRequestDto getEnemyTeam(MatchTeamRequestDto matchTeamRequestDto) {
        int enemyTeamId;
        Match match = matchRepository.findByMatchId(matchTeamRequestDto.matchId());

        if(match.team1Id == matchTeamRequestDto.teamId()){
            enemyTeamId = match.team2Id;
        }
        else {
            enemyTeamId = match.team1Id;
        }

        Team enemyTeam = teamRepository.findByTeamId(enemyTeamId);
        return new TeamResponseDto(enemyTeam);
    }
}
