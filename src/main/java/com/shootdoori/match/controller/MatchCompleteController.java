package com.shootdoori.match.controller;
import com.shootdoori.match.dto.MatchTeamRequestDto;
import com.shootdoori.match.service.MatchCompleteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchCompleteController {
    private final MatchCompleteService matchCompleteService;

    public MatchCompleteController(MatchCompleteService matchCompleteService) {
        this.matchCompleteService = matchCompleteService;
    }

    @GetMapping
    public TeamResponseDto getEnemyTeam(@RequestBody MatchTeamRequestDto matchTeamDto) {
        return new ResponseEntity<>(matchCompleteService.getEnemyTeam(matchTeamDto), HttpStatus.OK);
    }
}
