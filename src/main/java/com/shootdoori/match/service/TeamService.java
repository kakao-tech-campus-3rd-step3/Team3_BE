package com.shootdoori.match.service;

import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.TeamDetailResponseDto;
import com.shootdoori.match.dto.TeamMapper;
import com.shootdoori.match.dto.TeamRequestDto;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.value.UniversityName;
import java.util.Objects;
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
    private final TeamMemberService teamMemberService;
    private final TeamMapper teamMapper;
    private final MatchRequestService matchRequestService;
    private final MatchCreateService matchCreateService;
    private final MatchCompleteService matchCompleteService;


    public TeamService(ProfileRepository profileRepository, TeamRepository teamRepository,
        TeamMemberService teamMemberService, TeamMapper teamMapper,
        MatchRequestService matchRequestService,
        MatchCreateService matchCreateService, MatchCompleteService matchCompleteService) {
        this.profileRepository = profileRepository;
        this.teamRepository = teamRepository;
        this.teamMemberService = teamMemberService;
        this.teamMapper = teamMapper;
        this.matchRequestService = matchRequestService;
        this.matchCreateService = matchCreateService;
        this.matchCompleteService = matchCompleteService;
    }

    public CreateTeamResponseDto create(TeamRequestDto requestDto, Long userId) {
        User captain = profileRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.CAPTAIN_NOT_FOUND));
        Team team = TeamMapper.toEntity(requestDto, captain);
        team.recruitMember(captain, TeamMemberRole.LEADER);
        Team savedTeam = teamRepository.save(team);

        return teamMapper.toCreateTeamResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public TeamDetailResponseDto findById(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(id)));

        return teamMapper.toTeamDetailResponse(team);
    }

    @Transactional(readOnly = true)
    public Team findByIdForEntity(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(id)));
    }

    @Transactional(readOnly = true)
    public Page<TeamDetailResponseDto> findAllByUniversity(int page, int size, String university, boolean includeDeleted) {
        Pageable jpaPageable = PageRequest.of(page, size, Sort.by("teamName").ascending());
        Pageable nativePageable = PageRequest.of(page, size, Sort.by("team_name").ascending());
        
        Page<Team> teamPage = includeDeleted 
            ? teamRepository.findAllByUniversityIncludingDeleted(university, nativePageable)
            : teamRepository.findAllByUniversity(UniversityName.of(university), jpaPageable);

        return teamPage.map(teamMapper::toTeamDetailResponse);
    }

    public TeamDetailResponseDto update(Long id, TeamRequestDto requestDto, Long userId) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(id)));

        if (!Objects.equals(team.getCaptain().getId(), userId)) {
            throw new NoPermissionException(ErrorCode.CAPTAIN_ONLY_OPERATION);
        }

        team.changeTeamInfo(requestDto.name(), requestDto.university(),
            requestDto.skillLevel(), requestDto.description());

        return teamMapper.toTeamDetailResponse(team);
    }

    public void delete(Long id, Long userId) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(id)));

        team.delete(userId);
        teamRepository.save(team);
    }

    public TeamDetailResponseDto restore(Long id, Long userId) {
        Team team = teamRepository.findByTeamIdIncludingDeleted(id).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(id)));

        teamMemberService.ensureNotMemberOfAnyTeam(userId);

        team.restore(userId);
        teamRepository.save(team);

        return teamMapper.toTeamDetailResponse(team);
    }
}
