package com.shootdoori.match.controller;

import com.shootdoori.match.dto.LineupRequestDto;
import com.shootdoori.match.dto.LineupResponseDto;
import com.shootdoori.match.service.LineupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineups")
public class LineupController {

    private final LineupService lineupService;

    // 생성자를 통한 의존성 주입
    public LineupController(LineupService lineupService) {
        this.lineupService = lineupService;
    }

    @PostMapping
    public ResponseEntity<LineupResponseDto> createLineup(@RequestBody LineupRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lineupService.createLineup(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<LineupResponseDto>> getAllLineups(@RequestParam(required = true) Long teamId) {
        return ResponseEntity.ok(lineupService.getAllLineupsByTeamId(teamId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LineupResponseDto> getLineupById(@PathVariable Long id) {
        return ResponseEntity.ok(lineupService.getLineupById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LineupResponseDto> updateLineup(@PathVariable Long id, @RequestBody LineupRequestDto requestDto) {
        return ResponseEntity.ok(lineupService.updateLineup(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLineup(@PathVariable Long id) {
        lineupService.deleteLineup(id);
        return ResponseEntity.noContent().build();
    }
}
