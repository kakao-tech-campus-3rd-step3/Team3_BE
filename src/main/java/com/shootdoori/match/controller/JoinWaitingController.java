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
        // TODO: JWT 구현 이후에 Resolver 활용한 유저 ID 주입 필요 (현재는 JoinWaitingRequestDto에 존재)
        // -> JoinWaitingRequestDto 에서 applicantId 필드 제거, @LoginUser로 얻은 loginUserId가 이를 대체하고 서비스로 넘기도록 변경
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
        // TODO: JWT 구현 이후에 Resolver 활용한 approver TeamMember ID 주입 필요 (현재는 JoinWaitingApproveRequestDto에 존재)
        // -> JoinWaitingApproveRequestDto의 팀멤버 id에 해당하는 approverId 제거, @LoginUser로 얻은 아이디로 서비스에서 팀멤버 아이디 조회하여 처리
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
        // TODO: JWT 구현 이후에 Resolver 활용한 approver TeamMember ID 주입 필요 (현재는 JoinWaitingRejectRequestDto에 존재)
        // -> JoinWaitingRejectRequestDto의 팀멤버 id에 해당하는 approverId 제거, @LoginUser로 얻은 아이디로 서비스에서 팀멤버 아이디 조회하여 처리
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
        // TODO: JWT 구현 이후에 Resolver 활용한 requester User ID 주입 필요 (현재는 JoinWaitingCancelRequestDto에 존재)
        // -> JoinWaitingCancelRequestDto의 유저 id에 해당하는 requesterId 제거, @LoginUser로 얻은 아이디로 서비스에서 팀멤버 아이디 조회하여 처리
    ) {
        return new ResponseEntity<>(joinWaitingService.cancel(teamId, joinWaitingId, loginUserId, requestDto),
            HttpStatus.OK);
    }

    @GetMapping("/api/teams/{teamId}/join-waiting")
    public ResponseEntity<Page<JoinWaitingResponseDto>> findPending(
        @PathVariable Long teamId,
        @RequestParam(defaultValue = "PENDING") JoinWaitingStatus status,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(joinWaitingService.findPending(teamId, status, pageable),
            HttpStatus.OK);
    }

    @GetMapping("/api/users/me/join-waiting")
    public ResponseEntity<Page<JoinWaitingResponseDto>> findByApplicant(
        @LoginUser Long loginUserId,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
        /*
            TODO: JWT 구현 이후에 Resolver 활용한 유저 ID 주입 필요 (현재는 PathVariable로 받음)
            TODO: API endpoint를 전반적으로 수정할 필요성이 있는지 체크 필요
            // PathVariable을 @LoginUser로 대체 / API endpoint 수정 {userId} -> me 로
         */
    ) {
        return new ResponseEntity<>(joinWaitingService.findAllByApplicant_IdAndStatusIn(loginUserId, pageable),
            HttpStatus.OK);
    }
}
