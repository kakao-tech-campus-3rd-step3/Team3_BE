package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.AlreadyTeamMemberException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.exception.UserNotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final ProfileRepository profileRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository,
        TeamRepository teamRepository, ProfileRepository profileRepository) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.profileRepository = profileRepository;
    }

    public TeamMemberResponseDto create(Long teamId, TeamMemberRequestDto requestDto) {

        Long userId = requestDto.userId();

        Team team = teamRepository.findById(teamId).orElseThrow(() ->
            new TeamNotFoundException(teamId));

        User user = profileRepository.findById(userId).orElseThrow(
            () -> new UserNotFoundException(userId));

        if (teamMemberRepository.existsByTeam_IdAndUser_Id(teamId, userId)) {
            throw new AlreadyTeamMemberException();
        }

        team.validateSameUniversity(user);

        team.validateCanAcceptNewMember();

        TeamMemberRole teamMemberRole = TeamMemberRole.fromDisplayName(requestDto.role());

        TeamMember teamMember = new TeamMember(team, user, teamMemberRole);
        TeamMember savedTeamMember = teamMemberRepository.save(teamMember);

        return new TeamMemberResponseDto(savedTeamMember.getId(),
            savedTeamMember.getUser().getId(),
            savedTeamMember.getUser().getName(),
            savedTeamMember.getUser().getEmail(),
            savedTeamMember.getUser().getPosition().toString(),
            savedTeamMember.getRole().toString(),
            savedTeamMember.getJoinedAt());
    }
}
