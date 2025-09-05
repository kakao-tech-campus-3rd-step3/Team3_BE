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

    public MatchTeamRequestDto getEnemyTeam(MatchTeamRequestDto) {
    }
}
