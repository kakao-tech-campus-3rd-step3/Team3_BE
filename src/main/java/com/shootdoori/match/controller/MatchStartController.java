package com.shootdoori.match.controller;

import com.shootdoori.match.dto.RecentMatchesResponseDto;
import com.shootdoori.match.entity.MatchStatus;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.MatchStartService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class MatchStartController {

  private final MatchStartService matchService;

  public MatchStartController(MatchStartService matchStartService) {
    this.matchService = matchStartService;
  }

  @GetMapping("/me/matches")
  public ResponseEntity<List<RecentMatchesResponseDto>> getRecentCompletedMatches(
          @LoginUser Long loginUserId,
          @RequestParam(required = false) MatchStatus status,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cursorDate,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime cursorTime,
          @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
  ) {
    List<RecentMatchesResponseDto> matches =
        matchService.getMatchesByStatus(loginUserId, status, cursorDate, cursorTime, PageRequest.of(0, size));

    return ResponseEntity.ok(matches);
  }
}
