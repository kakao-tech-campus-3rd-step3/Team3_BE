package com.shootdoori.match.controller;

import com.shootdoori.match.dto.*;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.MatchRequestService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/matches")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    public MatchRequestController(MatchRequestService matchRequestService) {
        this.matchRequestService = matchRequestService;
    }

    @GetMapping("/waiting")
    public ResponseEntity<Slice<MatchWaitingResponseDto>> getWaitingMatches(
        @LoginUser Long loginUserId,
        @RequestParam("selectDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectDate,
        @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
        @PageableDefault(size = 10, sort = "preferredTimeStart", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        MatchWaitingRequestDto requestDto = new MatchWaitingRequestDto(selectDate, startTime);
        Slice<MatchWaitingResponseDto> slice = matchRequestService.getWaitingMatches(loginUserId, requestDto, pageable);
        return ResponseEntity.ok(slice);
    }

    @PostMapping("/{waitingId}/request")
    public ResponseEntity<MatchRequestResponseDto> requestToMatch(
        @LoginUser Long loginUserId,
        @PathVariable Long waitingId,
        @RequestBody MatchRequestRequestDto requestDto
    ) {
        MatchRequestResponseDto response = matchRequestService.requestToMatch(loginUserId, waitingId, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<MatchRequestResponseDto> cancelMatchRequest(
        @LoginUser Long loginUserId,
        @PathVariable Long requestId
    ) {
        MatchRequestResponseDto response = matchRequestService.cancelMatchRequest(loginUserId, requestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receive/me/pending")
    public ResponseEntity<Slice<MatchRequestResponseDto>> getReceivedPendingRequests(
        @LoginUser Long loginUserId,
        @PageableDefault(size = 10, sort = "requestAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<MatchRequestResponseDto> slice =
            matchRequestService.getReceivedPendingRequests(loginUserId, pageable);
        return ResponseEntity.ok(slice);
    }

    @PatchMapping("/requests/{requestId}/accept")
    public ResponseEntity<MatchConfirmedResponseDto> acceptRequest(
        @LoginUser Long loginUserId,
        @PathVariable Long requestId
    ) {
        MatchConfirmedResponseDto response = matchRequestService.acceptRequest(loginUserId, requestId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/requests/{requestId}/reject")
    public ResponseEntity<MatchRequestResponseDto> rejectRequest(
        @LoginUser Long loginUserId,
        @PathVariable Long requestId
    ) {
        MatchRequestResponseDto response = matchRequestService.rejectRequest(loginUserId, requestId);
        return ResponseEntity.ok(response);
    }
}
