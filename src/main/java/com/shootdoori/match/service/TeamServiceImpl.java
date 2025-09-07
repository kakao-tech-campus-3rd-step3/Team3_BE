package com.shootdoori.match.service;

import com.shootdoori.match.dto.CreateTeamRequestDto;
import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.TeamDetailResponseDto;
import com.shootdoori.match.dto.TeamMapper;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.CaptainNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private TeamRepository teamRepository;
    private TeamMapper teamMapper;


    public TeamServiceImpl(TeamRepository teamRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    @Override
    public CreateTeamResponseDto create(CreateTeamRequestDto requestDto, User captain) {
        if (captain == null) {
            throw new CaptainNotFoundException("팀장 정보가 없습니다.");
        }

        Team team = TeamMapper.toEntity(requestDto, captain);
        Team savedTeam = teamRepository.save(team);

        return TeamMapper.toCreateTeamResponse(savedTeam);
    }

    @Override
    public TeamDetailResponseDto findById(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException("해당 팀을 찾을 수 없습니다. id = " + id));

        return TeamMapper.teamDetailResponse(team);
    }

    @Override
    public void delete(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException("해당 팀을 찾을 수 없습니다. id = " + id));

        teamRepository.delete(team);
    }
}
