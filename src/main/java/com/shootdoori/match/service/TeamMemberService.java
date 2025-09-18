package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamMemberMapper;
import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.dto.UpdateTeamMemberRequestDto;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.AlreadyTeamMemberException;
import com.shootdoori.match.exception.TeamMemberNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.exception.UserNotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final ProfileRepository profileRepository;
    private final TeamMemberMapper teamMemberMapper;

    public TeamMemberService(TeamMemberRepository teamMemberRepository,
        TeamRepository teamRepository, ProfileRepository profileRepository,
        TeamMemberMapper teamMemberMapper) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.profileRepository = profileRepository;
        this.teamMemberMapper = teamMemberMapper;
    }

    public TeamMemberResponseDto create(Long teamId, TeamMemberRequestDto requestDto) {

        Long userId = requestDto.userId();

        Team team = teamRepository.findById(teamId).orElseThrow(() ->
            new TeamNotFoundException(teamId));

        User user = profileRepository.findById(userId).orElseThrow(
            () -> new UserNotFoundException(userId));

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, userId)) {
            throw new AlreadyTeamMemberException();
        }

        team.validateSameUniversity(user);

        team.validateCanAcceptNewMember();

        TeamMemberRole teamMemberRole = TeamMemberRole.fromDisplayName(requestDto.role());

        TeamMember teamMember = new TeamMember(team, user, teamMemberRole);
        TeamMember savedTeamMember = teamMemberRepository.save(teamMember);

        return teamMemberMapper.toTeamMemberResponseDto(savedTeamMember);
    }

    @Transactional(readOnly = true)
    public TeamMemberResponseDto findByTeamIdAndUserId(Long teamId, Long userId) {
        TeamMember teamMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, userId)
            .orElseThrow(() -> new TeamMemberNotFoundException());

        return teamMemberMapper.toTeamMemberResponseDto(teamMember);
    }

    @Transactional(readOnly = true)
    public Page<TeamMemberResponseDto> findAllByTeamId(Long teamId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("teamMemberId").ascending());

        Page<TeamMember> teamMemberPage = teamMemberRepository.findAllByTeam_TeamId(teamId, pageable);

        return teamMemberPage.map(teamMemberMapper::toTeamMemberResponseDto);
    }

    public TeamMemberResponseDto update(Long teamId, Long userId,
        UpdateTeamMemberRequestDto requestDto) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new TeamNotFoundException(teamId));

        TeamMember teamMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, userId)
            .orElseThrow(() -> new TeamMemberNotFoundException());

        teamMember.changeRole(team, requestDto);

        return teamMemberMapper.toTeamMemberResponseDto(teamMember);
    }

    public void delete(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new TeamNotFoundException(teamId));

        TeamMember teamMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, userId)
            .orElseThrow(() -> new TeamMemberNotFoundException());

        team.removeMember(teamMember);
    }
}
