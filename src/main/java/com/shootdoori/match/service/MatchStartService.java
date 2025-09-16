package com.shootdoori.match.service;

import com.shootdoori.match.dto.RecentMatchesResponseDto;
import com.shootdoori.match.entity.Match;
import com.shootdoori.match.entity.MatchStatus;
import com.shootdoori.match.repository.MatchRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchStartService {

  private final MatchRepository matchRepository;

  public MatchStartService(MatchRepository matchRepository) {
    this.matchRepository = matchRepository;
  }

  @Transactional(readOnly = true)
  public List<RecentMatchesResponseDto> getMatchesByStatus(
      Long teamId,
      MatchStatus status,
      LocalDate cursorDate,
      LocalTime cursorTime,
      Pageable pageable
  ) {
      Slice<Match> slice = isFirstPageRequest(cursorDate, cursorTime)
              ? matchRepository.findFirstPageMatchesByTeamIdAndStatus(teamId, status, pageable)
              : matchRepository.findMatchesByTeamIdAndStatus(teamId, status, cursorDate, cursorTime, pageable);

      return slice.getContent().stream()
        .map(RecentMatchesResponseDto::from)
        .toList();
  }

  private boolean isFirstPageRequest(LocalDate cursorDate, LocalTime cursorTime) {
      return cursorDate == null && cursorTime == null;
  }
}
