package com.shootdoori.match.controller;

import com.shootdoori.match.dto.MatchRequestRequestDto;
import com.shootdoori.match.dto.MatchRequestResponseDto;
import com.shootdoori.match.dto.MatchConfirmedResponseDto;
import com.shootdoori.match.service.MatchRequestService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchApplicationController {

  private final MatchRequestService matchRequestService;

  public MatchApplicationController(MatchRequestService matchRequestService) {
    this.matchRequestService = matchRequestService;
  }

  @PostMapping("/{waitingId}/apply")
  public ResponseEntity<MatchRequestResponseDto> applyToMatch(
      @PathVariable Long waitingId,
      @RequestBody MatchRequestRequestDto requestDto
  ) {
    MatchRequestResponseDto response = matchRequestService.applyToMatch(waitingId, requestDto);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/applications/{applicationId}")
  public ResponseEntity<MatchRequestResponseDto> cancelMatchApplication(
    @PathVariable Long applicationId
  ) {
    MatchRequestResponseDto response = matchRequestService.cancelMatchApplication(applicationId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{teamId}/pending")
  public ResponseEntity<Slice<MatchRequestResponseDto>> getReceivedPendingApplications(
    @PathVariable Long teamId,
    @PageableDefault(size = 10, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Slice<MatchRequestResponseDto> slice =
      matchRequestService.getReceivedPendingApplications(teamId, pageable);
    return ResponseEntity.ok(slice);
  }

  @PatchMapping("/applications/{applicationId}/accept")
  public ResponseEntity<MatchConfirmedResponseDto> acceptApplication(
    @PathVariable Long applicationId
  ) {
    MatchConfirmedResponseDto response = matchRequestService.acceptApplication(applicationId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/applications/{applicationId}/reject")
  public ResponseEntity<MatchRequestResponseDto> rejectApplication(
    @PathVariable Long applicationId
  ) {
    MatchRequestResponseDto response = matchRequestService.rejectApplication(applicationId);
    return ResponseEntity.ok(response);
  }
}
