package com.shootdoori.match.controller;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.service.MatchCreateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchCreateController {

  private final MatchCreateService matchCreateService;

  public MatchCreateController(MatchCreateService matchCreateService) {
    this.matchCreateService = matchCreateService;
  }

  @PostMapping
  public ResponseEntity<MatchCreateResponseDto> createMatch(
      @RequestBody MatchCreateRequestDto matchCreateRequestDto
  ) {
    MatchCreateResponseDto response = matchCreateService.createMatch(matchCreateRequestDto);
    return ResponseEntity.ok(response);
  }
}
