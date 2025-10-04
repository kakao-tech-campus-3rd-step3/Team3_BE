package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchSummaryProjection;
import com.shootdoori.match.dto.RecentMatchesResponseDto;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.MatchRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.shootdoori.match.repository.TeamMemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchStartService {

  private final MatchRepository matchRepository;

  private final TeamMemberRepository teamMemberRepository;

  public MatchStartService(MatchRepository matchRepository, TeamMemberRepository teamMemberRepository) {
    this.matchRepository = matchRepository;
    this.teamMemberRepository = teamMemberRepository;
  }

  @Transactional(readOnly = true)
  public List<RecentMatchesResponseDto> getMatchesByStatus(
      Long loginUserId,
      MatchStatus status,
      LocalDate cursorDate,
      LocalTime cursorTime,
      Pageable pageable
  ) {
      TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

      Team team = teamMember.getTeam();

      Slice<MatchSummaryProjection> slice = isFirstPageRequest(cursorDate, cursorTime)
              ? matchRepository.findFirstPageMatchSummariesByTeamIdAndStatus(team.getTeamId(), status, pageable)
              : matchRepository.findMatchSummariesByTeamIdAndStatus(team.getTeamId(), status, cursorDate, cursorTime, pageable);

      return slice.getContent().stream()
        .map(RecentMatchesResponseDto::from)
        .toList();
  }

  private boolean isFirstPageRequest(LocalDate cursorDate, LocalTime cursorTime) {
      return cursorDate == null && cursorTime == null;
  }
}
