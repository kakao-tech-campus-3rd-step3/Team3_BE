package com.shootdoori.match.service;

import com.shootdoori.match.dto.RecentMatchesResponseDto;
import com.shootdoori.match.entity.Match;
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
  public List<RecentMatchesResponseDto> getRecentCompletedMatches(
      Long teamId,
      LocalDate cursorDate,
      LocalTime cursorTime,
      Pageable pageable
  ) {
    if (cursorDate == null) cursorDate = LocalDate.of(9999, 12, 31);
    if (cursorTime == null) cursorTime = LocalTime.MAX;

    Slice<Match> slice = matchRepository.findCompletedMatchesByTeamId(
        teamId, cursorDate, cursorTime, pageable
    );

    return slice.getContent().stream()
        .map(RecentMatchesResponseDto::from)
        .toList();
  }
}
