package com.shootdoori.match.controller;

import com.shootdoori.match.dto.MercenaryReviewResponseDto;
import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamReviewResponseDto> getAllTeamReview(@PathVariable Long teamid) {
    }

    @PostMapping("/teams/{teamId}")
    public void postTeamReview() {
    }

    @PutMapping("/teams/{teamId}")
    public void updateTeamReview() {
    }

    @DeleteMapping("/teams/{teamId}")
    public void deleteTeamReview() {
    }

    @GetMapping("/mercenarys/{mercenaryId}")
    public ResponseEntity<MercenaryReviewResponseDto> getMercenaryReview() {
    }

    @PostMapping("/mercenarys/{mercenaryId}")
    public void postMercenaryReview() {
    }

    @PutMapping("/mercenarys/{mercenaryId}")
    public void updateMercenaryReview() {
    }

    @DeleteMapping("/mercenarys/{mercenaryId}")
    public void deleteMercenaryReview() {
    }
}
