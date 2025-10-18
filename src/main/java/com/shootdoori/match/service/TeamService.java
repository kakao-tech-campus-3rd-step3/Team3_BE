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
    private final TeamMapper teamMapper;
    private final MatchRequestService matchRequestService;
    private final MatchCreateService matchCreateService;
    private final MatchCompleteService matchCompleteService;


    public TeamService(ProfileRepository profileRepository, TeamRepository teamRepository,
        TeamMapper teamMapper, MatchRequestService matchRequestService,
        MatchCreateService matchCreateService, MatchCompleteService matchCompleteService) {
        this.profileRepository = profileRepository;
        this.teamRepository = teamRepository;
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
    public Page<TeamDetailResponseDto> findAllByUniversity(int page, int size, String university) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("teamName").ascending());

        UniversityName universityName = UniversityName.of(university);
        Page<Team> teamPage = teamRepository.findAllByUniversity(universityName, pageable);

        return teamPage.map(teamMapper::toTeamDetailResponse);
    }

    public TeamDetailResponseDto update(Long id, TeamRequestDto requestDto, Long userId) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(id)));

        // 기존 팀장과 요청을 보낸 유저가 동일하지 않다면 권한 없음으로 거부
        if (!Objects.equals(team.getCaptain().getId(), userId)) {
            throw new NoPermissionException();
        }

        team.changeTeamInfo(requestDto.name(), requestDto.university(),
            requestDto.skillLevel(), requestDto.description());

        return teamMapper.toTeamDetailResponse(team);
    }

    public void delete(Long id, Long userId) {
        Team team = teamRepository.findById(id).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(id)));

        if (!Objects.equals(team.getCaptain().getId(), userId)) {
            throw new NoPermissionException();
        }

        matchRequestService.deleteAllByTeamId(id);
        matchCreateService.deleteAllByTeamId(id);
        matchCompleteService.deleteAllByTeamId(id);

        teamRepository.delete(team);
    }
}
