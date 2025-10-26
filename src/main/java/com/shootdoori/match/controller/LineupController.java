package com.shootdoori.match.controller;

import com.shootdoori.match.dto.LineupMemberRequestDto;
import com.shootdoori.match.dto.LineupMemberResponseDto;
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
    public ResponseEntity<List<LineupMemberResponseDto>> createLineup(@RequestBody List<LineupMemberRequestDto> requestDtos,
                                                                      @LoginUser Long userId) {
        return new ResponseEntity<>(lineupService.createLineup(requestDtos, userId), HttpStatus.CREATED);
    }

    //TODO 라인업아이디로 조회하도록 서비스 변경
    @GetMapping("/{id}")
    public ResponseEntity<List<LineupMemberResponseDto>> getLineupById(@PathVariable Long id) {
        return new ResponseEntity<>(lineupService.getLineupById(id), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<List<LineupMemberResponseDto>> updateLineup(@PathVariable Long id,
                                                                @RequestBody List<LineupMemberRequestDto> requestDtos,
                                                                @LoginUser Long userId) {
        return new ResponseEntity<>(lineupService.updateLineup(id, requestDtos, userId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLineup(@PathVariable Long id,
                                             @LoginUser Long userId) {
        lineupService.deleteLineup(id, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
