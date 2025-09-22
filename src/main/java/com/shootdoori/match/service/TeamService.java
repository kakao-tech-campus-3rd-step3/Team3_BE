package com.shootdoori.match.service;

import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.TeamDetailResponseDto;
import com.shootdoori.match.dto.TeamMapper;
import com.shootdoori.match.dto.TeamRequestDto;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.CaptainNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.value.UniversityName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamService {

    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;


    public TeamService(ProfileRepository profileRepository, TeamRepository teamRepository,
        TeamMapper teamMapper) {
        this.profileRepository = profileRepository;
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    public CreateTeamResponseDto create(TeamRequestDto requestDto, User captain) {
        if (captain == null) {
            throw new CaptainNotFoundException();
        }

        /*
            TODO: JWT 토큰에서 captain 정보 받아와야 함. 현재는 하나의 User를 생성해 captain으로 대체하였음.
         */
        captain = profileRepository.save(captain);

        Team team = TeamMapper.toEntity(requestDto, captain);
        Team savedTeam = teamRepository.save(team);

        return teamMapper.toCreateTeamResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public TeamDetailResponseDto findById(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException(id));

        return teamMapper.toTeamDetailResponse(team);
    }

    @Transactional(readOnly = true)
    public Page<TeamDetailResponseDto> findAllByUniversity(int page, int size, String university) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("teamName").ascending());

        UniversityName universityName = UniversityName.of(university);
        Page<Team> teamPage = teamRepository.findAllByUniversity(universityName, pageable);

        return teamPage.map(teamMapper::toTeamDetailResponse);
    }

    public TeamDetailResponseDto update(Long id, TeamRequestDto requestDto) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException(id));

        team.changeTeamInfo(requestDto.name(), requestDto.university(),
            requestDto.skillLevel(), requestDto.description());

        return teamMapper.toTeamDetailResponse(team);
    }

    public void delete(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new TeamNotFoundException(id));

        teamRepository.delete(team);
    }
}
