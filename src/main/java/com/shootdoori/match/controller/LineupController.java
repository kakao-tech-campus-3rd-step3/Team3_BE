package com.shootdoori.match.controller;

import com.shootdoori.match.dto.LineupRequestDto;
import com.shootdoori.match.dto.LineupResponseDto;
import com.shootdoori.match.resolver.LoginUser;
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

    @PostMapping()
    public ResponseEntity<List<LineupResponseDto>> createLineup(@RequestBody List<LineupRequestDto> requestDtos,
                                                                @LoginUser Long userId) {
        return new ResponseEntity<>(lineupService.createLineup(requestDtos, userId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LineupResponseDto>> getLineupsByTeamId(@RequestParam(required = true) Long teamId) {
        return new ResponseEntity<>(lineupService.getAllLineupsByTeamId(teamId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LineupResponseDto> getLineupById(@PathVariable Long id) {
        return new ResponseEntity<>(lineupService.getLineupById(id), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LineupResponseDto> updateLineup(@PathVariable Long id,
                                                          @RequestBody LineupRequestDto requestDto,
                                                          @LoginUser Long userId) {
        return new ResponseEntity<>(lineupService.updateLineup(id, requestDto, userId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLineup(@PathVariable Long id,
                                             @LoginUser Long userId) {
        lineupService.deleteLineup(id, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
