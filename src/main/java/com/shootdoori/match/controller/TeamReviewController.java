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
    public ResponseEntity<List<TeamReviewResponseDto>> getAllTeamReview() {
        return new ResponseEntity<>(reviewService.getAllTeamReview(), HttpStatus.OK);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<TeamReviewResponseDto> getTeamReview(@PathVariable Long reviewId) {

    }

    @PostMapping("/{reviewId}")
    public void postTeamReview() {
    }

    @PutMapping("/{reviewId}")
    public void updateTeamReview() {
    }

    @DeleteMapping("/{reviewId}")
    public void deleteTeamReview() {
    }
}
