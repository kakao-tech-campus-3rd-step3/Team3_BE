package com.shootdoori.match.controller;

import com.shootdoori.match.dto.EnemyTeamResponseDto;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.MatchCompleteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchCompleteController {

    private final MatchCompleteService matchCompleteService;

    public MatchCompleteController(MatchCompleteService matchCompleteService) {
        this.matchCompleteService = matchCompleteService;
    }

    @GetMapping("/{matchId}/enemyTeam")
    public ResponseEntity<EnemyTeamResponseDto> getEnemyTeam(@LoginUser Long loginUserId,
                                                             @PathVariable Long matchId) {
        EnemyTeamResponseDto response = matchCompleteService.getEnemyTeam(loginUserId, matchId);
        return ResponseEntity.ok(response);
    }
}
