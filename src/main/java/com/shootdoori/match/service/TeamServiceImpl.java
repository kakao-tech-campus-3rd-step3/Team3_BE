package com.shootdoori.match.service;

import com.shootdoori.match.dto.CreateTeamRequestDto;
import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {
    private TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public CreateTeamResponseDto create(CreateTeamRequestDto requestDto) {
        return null;
    }
}
