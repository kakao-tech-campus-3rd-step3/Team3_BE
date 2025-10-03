package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchTeamRequestDto;
import com.shootdoori.match.dto.TeamResponseDto;
import com.shootdoori.match.entity.Match;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.stereotype.Service;

@Service
public class MatchCompleteService {
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamMemberRepository teamMemberRepository;

    public MatchCompleteService(TeamRepository teamRepository,
                                MatchRepository matchRepository,
                                TeamMemberRepository teamMemberRepository){
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    /**
     * 매치 정보 조회 후 상대 팀 정보 조회하는 서비스
     * @param matchTeamRequestDto 매치 id, 내 팀 id
     * @return TeamResponseDto 적 팀 정보
     */
    public TeamResponseDto getEnemyTeam(Long loginUserId, MatchTeamRequestDto matchTeamRequestDto) {
        Long enemyTeamId;
        Match match = matchRepository.findByMatchId(matchTeamRequestDto.matchId());

        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team team = teamMember.getTeam();

        Team enemyTeam = match.findEnemyTeam(team.getTeamId());
        return new TeamResponseDto(enemyTeam);
    }
}
