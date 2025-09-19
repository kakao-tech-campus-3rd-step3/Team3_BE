package com.shootdoori.match.controller;

import com.shootdoori.match.dto.JoinQueueApproveRequestDto;
import com.shootdoori.match.dto.JoinQueueRejectRequestDto;
import com.shootdoori.match.dto.JoinQueueRequestDto;
import com.shootdoori.match.dto.JoinQueueResponseDto;
import com.shootdoori.match.service.JoinQueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams/{teamId}/join-queue")
public class JoinQueueController {

    private final JoinQueueService joinQueueService;

    public JoinQueueController(JoinQueueService joinQueueService) {
        this.joinQueueService = joinQueueService;
    }

    @PostMapping
    public ResponseEntity<JoinQueueResponseDto> create(
        @PathVariable Long teamId,
        @RequestBody JoinQueueRequestDto requestDto
        // TODO: JWT 구현 이후에 Resolver 활용한 유저 ID 주입 필요 (현재는 JoinQueueRequestDto에 존재)
    ) {
        return new ResponseEntity<>(joinQueueService.create(teamId, requestDto),
            HttpStatus.CREATED);
    }

    @PostMapping("/{joinQueueId}/approve")
    public ResponseEntity<JoinQueueResponseDto> approve(
        @PathVariable Long teamId,
        @PathVariable Long joinQueueId,
        @RequestBody JoinQueueApproveRequestDto requestDto
        // TODO: JWT 구현 이후에 Resolver 활용한 approver User ID 주입 필요 (현재는 JoinQueueApproveRequestDto에 존재)
    ) {
        return new ResponseEntity<>(joinQueueService.approve(teamId, joinQueueId, requestDto),
            HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/{joinQueueId}/reject")
    public ResponseEntity<JoinQueueResponseDto> reject(
        @PathVariable Long teamId,
        @PathVariable Long joinQueueId,
        @RequestBody JoinQueueRejectRequestDto requestDto
        // TODO: JWT 구현 이후에 Resolver 활용한 approver User ID 주입 필요 (현재는 JoinQueueRejectRequestDto에 존재)
    ) {
        return new ResponseEntity<>(joinQueueService.reject(teamId, joinQueueId, requestDto),
            HttpStatus.OK);
    }
}
