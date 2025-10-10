package com.shootdoori.match.controller;

import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.TeamDetailResponseDto;
import com.shootdoori.match.dto.TeamRequestDto;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<CreateTeamResponseDto> create(
        @RequestBody TeamRequestDto requestDto,
        @LoginUser Long userId) {

        return new ResponseEntity<>(teamService.create(requestDto, userId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> findById(@PathVariable Long id) {

        return new ResponseEntity<>(teamService.findById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<TeamDetailResponseDto>> findAllByUniversity(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam String university
    ) {

        return new ResponseEntity<>(teamService.findAllByUniversity(page, size, university),
            HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> update(
        @PathVariable Long id,
        @RequestBody TeamRequestDto requestDto,
        @LoginUser Long userId
    ) {
        return new ResponseEntity<TeamDetailResponseDto>(teamService.update(id, requestDto, userId),
            HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
        @LoginUser Long userId) {
        teamService.delete(id, userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> restore(@PathVariable Long id,
        @LoginUser Long userId) {

        return new ResponseEntity<>(teamService.restore(id, userId), HttpStatus.OK);
    }
}