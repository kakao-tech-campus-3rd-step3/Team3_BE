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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;


    public TeamService(TeamRepository teamRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    public CreateTeamResponseDto create(CreateTeamRequestDto requestDto, User captain) {
        if (captain == null) {
            throw new CaptainNotFoundException("팀장 정보가 없습니다.");
        }

        Team team = TeamMapper.toEntity(requestDto, captain);
        Team savedTeam = teamRepository.save(team);

        return teamMapper.toCreateTeamResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public TeamDetailResponseDto findById(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException("해당 팀을 찾을 수 없습니다. id = " + id));

        return teamMapper.toTeamDetailResponse(team);
    }

    @Transactional(readOnly = true)
    public Page<TeamDetailResponseDto> findAllByUniversity(int page, int size, String university) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("teamName").ascending());

        Page<Team> teamPage = teamRepository.findAllByUniversity(university, pageable);

        return teamPage.map(teamMapper::toTeamDetailResponse);
    }

    public TeamDetailResponseDto update(Long id, TeamRequestDto requestDto) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException("해당 팀을 찾을 수 없습니다. id = " + id));

        team.changeTeamInfo(requestDto.name(), requestDto.university(),
            requestDto.skillLevel(), requestDto.description());

        return teamMapper.toTeamDetailResponse(team);
    }

    public void delete(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException("해당 팀을 찾을 수 없습니다. id = " + id));

        teamRepository.delete(team);
    }
}