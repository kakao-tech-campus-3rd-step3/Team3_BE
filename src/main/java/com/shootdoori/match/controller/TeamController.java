package com.shootdoori.match.controller;

import com.shootdoori.match.dto.CreateTeamRequestDto;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<Team> create(@RequestBody CreateTeamRequestDto requestDto) {

    }
}