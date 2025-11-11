package com.shootdoori.match.controller;

import com.shootdoori.match.dto.JoinWaitingApproveRequestDto;
import com.shootdoori.match.dto.JoinWaitingCancelRequestDto;
import com.shootdoori.match.dto.JoinWaitingRejectRequestDto;
import com.shootdoori.match.dto.JoinWaitingRequestDto;
import com.shootdoori.match.dto.JoinWaitingResponseDto;
import com.shootdoori.match.entity.team.join.JoinWaitingStatus;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.JoinWaitingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinWaitingController {

    private final JoinWaitingService joinWaitingService;

    public JoinWaitingController(JoinWaitingService joinWaitingService) {
        this.joinWaitingService = joinWaitingService;
    }

    @PostMapping("/api/teams/{teamId}/join-waiting")
    public ResponseEntity<JoinWaitingResponseDto> create(
        @PathVariable Long teamId,
        @LoginUser Long loginUserId,
        @RequestBody JoinWaitingRequestDto requestDto
    ) {
        return new ResponseEntity<>(joinWaitingService.create(teamId, loginUserId, requestDto),
            HttpStatus.CREATED);
    }

    @PostMapping("/api/teams/{teamId}/join-waiting/{joinWaitingId}/approve")
    public ResponseEntity<JoinWaitingResponseDto> approve(
        @PathVariable Long teamId,
        @PathVariable Long joinWaitingId,
        @LoginUser Long loginUserId,
        @RequestBody JoinWaitingApproveRequestDto requestDto
    ) {
        return new ResponseEntity<>(joinWaitingService.approve(teamId, joinWaitingId, loginUserId, requestDto),
            HttpStatus.OK);
    }

    @PostMapping("/api/teams/{teamId}/join-waiting/{joinWaitingId}/reject")
    public ResponseEntity<JoinWaitingResponseDto> reject(
        @PathVariable Long teamId,
        @PathVariable Long joinWaitingId,
        @LoginUser Long loginUserId,
        @RequestBody JoinWaitingRejectRequestDto requestDto
    ) {
        return new ResponseEntity<>(joinWaitingService.reject(teamId, joinWaitingId, loginUserId, requestDto),
            HttpStatus.OK);
    }

    @PostMapping("/api/teams/{teamId}/join-waiting/{joinWaitingId}/cancel")
    public ResponseEntity<JoinWaitingResponseDto> cancel(
        @PathVariable Long teamId,
        @PathVariable Long joinWaitingId,
        @LoginUser Long loginUserId,
        @RequestBody JoinWaitingCancelRequestDto requestDto
    ) {
        return new ResponseEntity<>(joinWaitingService.cancel(teamId, joinWaitingId, loginUserId, requestDto),
            HttpStatus.OK);
    }

    @GetMapping("/api/teams/{teamId}/join-waiting")
    public ResponseEntity<Page<JoinWaitingResponseDto>> findPending(
        @PathVariable Long teamId,
        @RequestParam(defaultValue = "PENDING") JoinWaitingStatus status,
        @PageableDefault(size = 10, sort = "audit.createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(joinWaitingService.findPending(teamId, status, pageable),
            HttpStatus.OK);
    }

    @GetMapping("/api/users/me/join-waiting")
    public ResponseEntity<Page<JoinWaitingResponseDto>> findByApplicant(
        @LoginUser Long loginUserId,
        @PageableDefault(size = 10, sort = "audit.createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(joinWaitingService.findAllByApplicantIdAndStatusIn(loginUserId, pageable),
            HttpStatus.OK);
    }
}
