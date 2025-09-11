package com.shootdoori.match.controller;

import com.shootdoori.match.dto.MatchApplicationRequestDto;
import com.shootdoori.match.dto.MatchApplicationResponseDto;
import com.shootdoori.match.service.MatchApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchApplicationController {

  private final MatchApplicationService matchApplicationService;

  public MatchApplicationController(MatchApplicationService matchApplicationService) {
    this.matchApplicationService = matchApplicationService;
  }

  @PostMapping("/{waitingId}/apply")
  public ResponseEntity<MatchApplicationResponseDto> applyToMatch(
      @PathVariable Long waitingId,
      @RequestBody MatchApplicationRequestDto requestDto
  ) {
    MatchApplicationResponseDto response = matchApplicationService.applyToMatch(waitingId, requestDto);
    return ResponseEntity.ok(response);
  }
}
