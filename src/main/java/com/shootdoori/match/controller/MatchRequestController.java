package com.shootdoori.match.controller;

import com.shootdoori.match.dto.*;
import com.shootdoori.match.service.MatchRequestService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchRequestController {

  private final MatchRequestService matchRequestService;

  public MatchRequestController(MatchRequestService matchRequestService) {
    this.matchRequestService = matchRequestService;
  }

  @GetMapping("/waiting")
  public ResponseEntity<Slice<MatchWaitingResponseDto>> getWaitingMatches(
    @RequestBody MatchWaitingRequestDto requestDto,
    @PageableDefault(size = 10, sort = "preferredTimeStart", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    Slice<MatchWaitingResponseDto> slice = matchRequestService.getWaitingMatches(requestDto, pageable);
    return ResponseEntity.ok(slice);
  }

  @PostMapping("/{waitingId}/request")
  public ResponseEntity<MatchRequestResponseDto> requestToMatch(
      @PathVariable Long waitingId,
      @RequestBody MatchRequestRequestDto requestDto
  ) {
    MatchRequestResponseDto response = matchRequestService.requestToMatch(waitingId, requestDto);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/requests/{requestId}")
  public ResponseEntity<MatchRequestResponseDto> cancelMatchRequest(
    @PathVariable Long requestId
  ) {
    MatchRequestResponseDto response = matchRequestService.cancelMatchRequest(requestId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{teamId}/pending")
  public ResponseEntity<Slice<MatchRequestResponseDto>> getReceivedPendingRequests(
    @PathVariable Long teamId,
    @PageableDefault(size = 10, sort = "requestAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Slice<MatchRequestResponseDto> slice =
      matchRequestService.getReceivedPendingRequests(teamId, pageable);
    return ResponseEntity.ok(slice);
  }

  @PatchMapping("/requests/{requestId}/accept")
  public ResponseEntity<MatchConfirmedResponseDto> acceptRequest(
    @PathVariable Long requestId
  ) {
    MatchConfirmedResponseDto response = matchRequestService.acceptRequest(requestId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/requests/{requestId}/reject")
  public ResponseEntity<MatchRequestResponseDto> rejectRequest(
    @PathVariable Long requestId
  ) {
    MatchRequestResponseDto response = matchRequestService.rejectRequest(requestId);
    return ResponseEntity.ok(response);
  }
}
