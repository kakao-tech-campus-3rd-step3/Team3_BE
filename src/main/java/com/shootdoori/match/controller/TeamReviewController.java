package com.shootdoori.match.controller;

import com.shootdoori.match.dto.TeamReviewRequestDto;
import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.service.TeamReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/reviews")
public class TeamReviewController {
    private final TeamReviewService reviewService;
    public TeamReviewController(TeamReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping()
    public ResponseEntity<List<TeamReviewResponseDto>> getAll(@PathVariable Long teamId) {
        return new ResponseEntity<>(reviewService.getAll(teamId), HttpStatus.OK);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<TeamReviewResponseDto> get(@PathVariable Long teamId, @PathVariable Long reviewId) {
        return new ResponseEntity<>(reviewService.get(teamId, reviewId), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Void> post(@RequestBody TeamReviewRequestDto request) {
        reviewService.post(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> update(@PathVariable Long reviewId, @RequestBody TeamReviewRequestDto request) {
        reviewService.update(reviewId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId) {
        reviewService.delete(reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
