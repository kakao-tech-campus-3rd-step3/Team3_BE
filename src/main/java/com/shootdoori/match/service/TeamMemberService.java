package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamMemberMapper;
import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.dto.UpdateTeamMemberRequestDto;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedException;
import com.shootdoori.match.exception.ErrorCode;
import com.shootdoori.match.exception.NotFoundException;
import com.shootdoori.match.exception.ErrorCode;
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
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        User user = profileRepository.findById(userId).orElseThrow(
            () -> new NotFoundException(ErrorCode.USER_NOT_FOUND, String.valueOf(userId)));

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, userId)) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }

        team.validateSameUniversity(user);

        team.validateCanAcceptNewMember();

        TeamMemberRole teamMemberRole = TeamMemberRole.fromDisplayName(requestDto.role());

        team.recruitMember(user, teamMemberRole);
        teamRepository.save(team);

        TeamMember savedTeamMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId,
            userId).orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        return teamMemberMapper.toTeamMemberResponseDto(savedTeamMember);
    }

    @Transactional(readOnly = true)
    public TeamMemberResponseDto findByTeamIdAndUserId(Long teamId, Long userId) {
        TeamMember teamMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        return teamMemberMapper.toTeamMemberResponseDto(teamMember);
    }

    @Transactional(readOnly = true)
    public Page<TeamMemberResponseDto> findAllByTeamId(Long teamId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<TeamMember> teamMemberPage = teamMemberRepository.findAllByTeam_TeamId(teamId,
            pageable);

        return teamMemberPage.map(teamMemberMapper::toTeamMemberResponseDto);
    }

    public TeamMemberResponseDto update(Long teamId, Long userId,
        UpdateTeamMemberRequestDto requestDto) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        TeamMember teamMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        teamMember.changeRole(team, TeamMemberRole.fromDisplayName(requestDto.role()));

        return teamMemberMapper.toTeamMemberResponseDto(teamMember);
    }

    public void delete(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        TeamMember teamMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        team.removeMember(teamMember);
        teamRepository.save(team);
    }
}
