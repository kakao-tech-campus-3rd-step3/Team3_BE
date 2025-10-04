package com.shootdoori.match.dto;

import com.shootdoori.match.entity.team.TeamMember;
import org.springframework.stereotype.Component;

@Component
public class TeamMemberMapper {

    private TeamMemberMapper() {
        
    }


    public TeamMemberResponseDto toTeamMemberResponseDto(TeamMember teamMember) {
        return new TeamMemberResponseDto(teamMember.getId(),
            teamMember.getUser().getId(),
            teamMember.getUser().getName(),
            teamMember.getUser().getEmail(),
            teamMember.getUser().getPosition().toString(),
            teamMember.getRole().toString(),
            teamMember.getJoinedAt());
    }
}
