package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamMemberMapper;
import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.dto.UpdateTeamMemberRequestDto;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DifferentException;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import java.util.List;
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

    public TeamMemberResponseDto create(Long teamId,
        TeamMemberRequestDto requestDto,
        Long captainId) {

        Long userId = requestDto.userId();

        Team team = teamRepository.findById(teamId).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        if (!team.getCaptain().getId().equals(captainId)) {
            throw new NoPermissionException(ErrorCode.CAPTAIN_ONLY_OPERATION);
        }

        User user = profileRepository.findById(userId).orElseThrow(
            () -> new NotFoundException(ErrorCode.USER_NOT_FOUND, String.valueOf(userId)));

        if (teamMemberRepository.existsByUser_Id(userId)) {
            throw new DuplicatedException(ErrorCode.ALREADY_OTHER_TEAM_MEMBER);
        }

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

    public TeamMemberResponseDto update(Long teamId, Long targetUserId,
        UpdateTeamMemberRequestDto requestDto, Long loginUserId) {

        Team team = teamRepository.findById(teamId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        TeamMember actor = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        TeamMember targetMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId,
                targetUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        if (isForbiddenSelfRoleChange(targetUserId, loginUserId, actor)) {
            throw new DuplicatedException(ErrorCode.SELF_DELEGATION_NOT_ALLOWED);
        }

        if (!actor.getRole().canMakeJoinDecision()) {
            throw new NoPermissionException(ErrorCode.INSUFFICIENT_ROLE_FOR_ROLE_CHANGE);
        }

        targetMember.changeRole(team, TeamMemberRole.fromDisplayName(requestDto.role()));

        return teamMemberMapper.toTeamMemberResponseDto(targetMember);
    }

    public void leave(Long teamId, Long loginUserId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        TeamMember loginMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId,
                loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        if (loginMember.isCaptain()) {
            throw new NoPermissionException(ErrorCode.LEADER_CANNOT_LEAVE);
        }

        team.removeMember(loginMember);
        teamRepository.save(team);
    }

    public void kick(Long teamId, Long userId, Long loginUserId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        TeamMember loginMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId,
                loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        TeamMember targetMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        if (!loginMember.getRole().canKick(targetMember.getRole())) {
            throw new NoPermissionException(ErrorCode.INSUFFICIENT_ROLE_FOR_KICK);
        }

        team.removeMember(targetMember);
        teamRepository.save(team);
    }

    public TeamMemberResponseDto delegateLeadership(Long teamId, Long currentUserId,
        Long targetMemberId) {
        List<TeamMember> members = prepareDelegationMembers(teamId, currentUserId, targetMemberId);

        TeamMember currentMember = members.get(0);
        TeamMember targetMember = members.get(1);

        currentMember.delegateLeadership(targetMember);

        return teamMemberMapper.toTeamMemberResponseDto(targetMember);
    }

    public TeamMemberResponseDto delegateViceLeadership(Long teamId, Long currentUserId,
        Long targetMemberId) {
        List<TeamMember> members = prepareDelegationMembers(teamId, currentUserId, targetMemberId);

        TeamMember currentMember = members.get(0);
        TeamMember targetMember = members.get(1);

        currentMember.delegateViceLeadership(targetMember);

        return teamMemberMapper.toTeamMemberResponseDto(targetMember);
    }

    public void ensureNotMemberOfAnyTeam(Long userId) {
        if (teamMemberRepository.existsByUser_Id(userId)) {
            throw new DuplicatedException(ErrorCode.ALREADY_OTHER_TEAM_MEMBER);
        }
    }

    private boolean isForbiddenSelfRoleChange(Long targetUserId, Long loginUserId,
        TeamMember actor) {
        return loginUserId.equals(targetUserId) && (actor.isCaptain() || actor.isViceCaptain());
    }

    private List<TeamMember> prepareDelegationMembers(Long teamId, Long currentUserId,
        Long targetMemberId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        TeamMember currentMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId,
            currentUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        TeamMember targetMember = teamMemberRepository.findById(targetMemberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        if (!team.equals(targetMember.getTeam())) {
            throw new DifferentException(ErrorCode.DIFFERENT_TEAM_DELEGATION_NOT_ALLOWED);
        }

        return List.of(currentMember, targetMember);
    }
}
