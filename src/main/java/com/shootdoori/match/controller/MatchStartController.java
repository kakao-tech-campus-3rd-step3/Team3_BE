package com.shootdoori.match.controller;

import com.shootdoori.match.dto.RecentMatchesResponseDto;
import com.shootdoori.match.service.MatchStartService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/teams")
public class MatchStartController {

  private final MatchStartService matchService;

  public MatchStartController(MatchStartService matchStartService) {
    this.matchService = matchStartService;
  }

  @GetMapping("/{teamId}/matches/completed")
  public ResponseEntity<List<RecentMatchesResponseDto>> getRecentCompletedMatches(
      @PathVariable Long teamId,
      @RequestParam(required = false) String cursorDate,
      @RequestParam(required = false) String cursorTime,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(0, size);

    LocalDate cursorD = StringUtils.hasText(cursorDate) ? LocalDate.parse(cursorDate) : null;
    LocalTime cursorT = StringUtils.hasText(cursorTime) ? LocalTime.parse(cursorTime) : null;

    List<RecentMatchesResponseDto> matches =
        matchService.getRecentCompletedMatches(teamId, cursorD, cursorT, pageable);

    return ResponseEntity.ok(matches);
  }
}
