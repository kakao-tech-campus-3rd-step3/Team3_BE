package com.shootdoori.match.controller;

import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.service.TeamMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams/{teamId}/users")
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

        return new ResponseEntity<>(teamMemberService.create(teamId, requestDto),
            HttpStatus.CREATED);
    }

    @GetMapping("/{userId}}")
    public ResponseEntity<TeamMemberResponseDto> findByTeamIdAndUserId(@PathVariable Long teamId,
        @PathVariable Long userId) {
        return new ResponseEntity<>(teamMemberService.findByTeamIdAndUserId(teamId, userId),
            HttpStatus.OK);
    }

    //    @PutMapping("/{memberId}")
//    public ResponseEntity<Void> update(@PathVariable Long teamId,
//        @PathVariable Long memberId,
//        @RequestBody UpdateTeamMemberRequestDto request) {
//
//    }
//
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long teamId,
        @PathVariable Long userId) {
        teamMemberService.delete(teamId, userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
