package com.shootdoori.match.dto;

import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;

public class TeamMapper {

    private TeamMapper() {
    }

    public static Team toEntity(CreateTeamRequestDto requestDto, User captain) {
        return new Team(
            requestDto.name(),
            captain,
            requestDto.university(),
            parseToTeamType(requestDto.teamType()),
            requestDto.memberCount(),
            parseToSkillLevel(requestDto.skillLevel()),
            requestDto.description()
        );
    }

    public static CreateTeamResponseDto toCreateTeamResponse(Team team) {
        Long id = team.getTeamId();
        return new CreateTeamResponseDto(id, "팀이 성공적으로 생성되었습니다.", "/api/teams/" + id);
    }

    public static TeamDetailResponseDto teamDetailResponse(Team team) {
        return new TeamDetailResponseDto(team.getTeamId(),
            team.getTeamName(),
            team.getDescription(),
            team.getUniversity(),
            team.getSkillLevel(),
            team.getTeamType(),
            team.getMemberCount(),
            team.getCreatedAt().toString());
    }

    /**
     * 문자열 입력("중앙동아리", "과동아리", 그 외)을 Enum TeamType 으로 변환하는 메서드
     *
     * @param value 요청 DTO에서 넘어온 문자열
     * @return TeamType Enum 값
     */
    private static TeamType parseToTeamType(String value) {
        if (value == null) {
            return TeamType.기타;
        }

        return TeamType.from(value);
    }

    /**
     * 문자열 입력("프로", "세미프로", 그 외)을 Enum SkillLevel 으로 변환하는 메서드
     *
     * @param value 요청 DTO에서 넘어온 문자열
     * @return SkillLevel Enum 값
     */
    private static SkillLevel parseToSkillLevel(String value) {
        if (value == null) {
            return SkillLevel.아마추어;
        }

        return SkillLevel.from(value);
    }
}
