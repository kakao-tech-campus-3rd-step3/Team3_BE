package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.AlreadyTeamMemberException;
import com.shootdoori.match.exception.TeamNotFoundException;
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
            new TeamNotFoundException("해당 팀을 찾을 수 없습니다. id = " + teamId));

        User user = profileRepository.findById(userId).orElseThrow(
            () -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id = " + userId));

        if (teamMemberRepository.existsByTeam_IdAndUser_Id(teamId, userId)) {
            throw new AlreadyTeamMemberException("이미 해당 팀의 멤버입니다.");
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
