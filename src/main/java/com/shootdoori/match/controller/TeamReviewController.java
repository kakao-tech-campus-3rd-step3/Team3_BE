package com.shootdoori.match.controller;

import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/reviews")
public class TeamReviewController {
    private final ReviewService reviewService;
    public TeamReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping()
    public ResponseEntity<List<TeamReviewResponseDto>> getAll(@PathVariable Long teamId) {
        return new ResponseEntity<>(reviewService.getAllTeamReview(teamId), HttpStatus.OK);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<TeamReviewResponseDto> get(@PathVariable Long teamId, @PathVariable Long reviewId) {
        return new ResponseEntity<>(reviewService.getTeamReview(teamId, reviewId), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Void> post(@PathVariable Long teamId) {
        reviewService.postTeamReview(teamId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> update(@PathVariable Long teamId, @PathVariable Long reviewId) {
        return new ResponseEntity<>(reviewService.updateTeamReview(teamId, reviewId), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long teamId, @PathVariable Long reviewId) {
        return new ResponseEntity<>(reviewService.deleteTeamReview(teamId, reviewId), HttpStatus.NO_CONTENT);
    }
}
