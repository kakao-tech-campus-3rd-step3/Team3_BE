package com.shootdoori.match.dto;

import com.shootdoori.match.entity.TeamSkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    private TeamMapper() {
    }

    public static Team toEntity(TeamRequestDto requestDto, User captain) {
        return new Team(
            requestDto.name(),
            captain,
            requestDto.university(),
            parseToTeamType(requestDto.teamType()),
            parseToSkillLevel(requestDto.skillLevel()),
            requestDto.description()
        );
    }

    public CreateTeamResponseDto toCreateTeamResponse(Team team) {
        Long id = team.getTeamId();
        return new CreateTeamResponseDto(id, "팀이 성공적으로 생성되었습니다.", "/api/teams/" + id);
    }

    public TeamDetailResponseDto toTeamDetailResponse(Team team) {
        return new TeamDetailResponseDto(team.getTeamId(),
            team.getTeamName().name(),
            team.getDescription().description(),
            team.getUniversity().name(),
            team.getSkillLevel(),
            team.getTeamType(),
            team.getMemberCount().count(),
            team.getCreatedAt().toString());
    }

    private static TeamType parseToTeamType(String value) {
        if (value == null) {
            return TeamType.OTHER;
        }

        return TeamType.fromDisplayName(value);
    }

    private static TeamSkillLevel parseToSkillLevel(String value) {
        if (value == null) {
            return TeamSkillLevel.AMATEUR;
        }

        return TeamSkillLevel.fromDisplayName(value);
    }
}
