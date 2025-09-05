package com.shootdoori.match.controller;
import com.shootdoori.match.dto.MatchTeamRequestDto;
import com.shootdoori.match.service.MatchCompleteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchCompleteController {
    private final MatchCompleteService matchCompleteService;

    public MatchCompleteController(MatchCompleteService matchCompleteService) {
        this.matchCompleteService = matchCompleteService;
    }


    /**
     * 특정 매치의 상대팀 정보 조회
     * @param matchTeamDto 매치 id, 내 팀 id
     * @return TeamResponseDto JSON
     */
    @GetMapping("/enemyTeam")
    public TeamResponseDto getEnemyTeam(@RequestBody MatchTeamRequestDto matchTeamDto) {
        return new ResponseEntity<>(matchCompleteService.getEnemyTeam(matchTeamDto), HttpStatus.OK);
    }
}
