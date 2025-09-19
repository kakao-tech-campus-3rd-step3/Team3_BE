package com.shootdoori.match.controller;

import com.shootdoori.match.dto.MatchApplicationRequestDto;
import com.shootdoori.match.dto.MatchApplicationResponseDto;
import com.shootdoori.match.dto.MatchConfirmedResponseDto;
import com.shootdoori.match.service.MatchApplicationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @GetMapping("/{teamId}/pending")
  public ResponseEntity<Slice<MatchApplicationResponseDto>> getReceivedPendingApplications(
    @PathVariable Long teamId,
    @PageableDefault(size = 10, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Slice<MatchApplicationResponseDto> slice =
      matchApplicationService.getReceivedPendingApplications(teamId, pageable);
    return ResponseEntity.ok(slice);
  }

  @PatchMapping("/applications/{applicationId}/accept")
  public ResponseEntity<MatchConfirmedResponseDto> acceptApplication(
    @PathVariable Long applicationId
  ) {
    MatchConfirmedResponseDto response = matchApplicationService.acceptApplication(applicationId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/applications/{applicationId}/reject")
  public ResponseEntity<MatchApplicationResponseDto> rejectApplication(
    @PathVariable Long applicationId
  ) {
    MatchApplicationResponseDto response = matchApplicationService.rejectApplication(applicationId);
    return ResponseEntity.ok(response);
  }
}
