package com.shootdoori.match.controller;

import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.service.TeamMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams/{teamId}/members")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    public TeamMemberController(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
    }

//    @GetMapping
//    public ResponseEntity<List<TeamMemberResponseDto>> findAllByTeamId(@PathVariable Long teamId) {
//        ...
//    }

    @PostMapping
    public ResponseEntity<TeamMemberResponseDto> create(@PathVariable Long teamId,
        @RequestBody TeamMemberRequestDto requestDto) {

    }

//    @GetMapping("/{memberId}")
//    public ResponseEntity<TeamMemberResponseDto> findById(@PathVariable Long teamId,
//        @PathVariable Long memberId) {
//
//    }
//
//    @PutMapping("/{memberId}")
//    public ResponseEntity<Void> update(@PathVariable Long teamId,
//        @PathVariable Long memberId,
//        @RequestBody UpdateTeamMemberRequestDto request) {
//
//    }
//
//    @DeleteMapping("/{memberId}")
//    public ResponseEntity<Void> delete(@PathVariable Long teamId,
//        @PathVariable Long memberId) {
//
//    }
}
