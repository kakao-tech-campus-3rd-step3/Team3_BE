package com.shootdoori.match.controller;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.dto.MatchWaitingCancelResponseDto;
import com.shootdoori.match.dto.MatchWaitingResponseDto;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.MatchCreateService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
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
        @LoginUser Long loginUserId,
        @RequestBody MatchCreateRequestDto matchCreateRequestDto
    ) {
        MatchCreateResponseDto response = matchCreateService.createMatch(loginUserId, matchCreateRequestDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/waiting/{matchWaitingId}/cancel")
    public ResponseEntity<MatchWaitingCancelResponseDto> cancelMatchWaiting(
        @LoginUser Long loginUserId,
        @PathVariable Long matchWaitingId
    ) {
        MatchWaitingCancelResponseDto response = matchCreateService.cancelMatchWaiting(loginUserId, matchWaitingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/waiting/me")
    public ResponseEntity<Slice<MatchWaitingResponseDto>> getMyWaitingMatches(
        @LoginUser Long loginUserId,
        @PageableDefault(size = 10, sort = "audit.createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<MatchWaitingResponseDto> response = matchCreateService.getMyWaitingMatches(loginUserId, pageable);
        return ResponseEntity.ok(response);
    }

}
