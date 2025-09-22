package com.shootdoori.match.controller;

import com.shootdoori.match.dto.MercenaryReviewRequestDto;
import com.shootdoori.match.dto.MercenaryReviewResponseDto;
import com.shootdoori.match.service.MercenaryReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class MercenaryReviewController {
    private final MercenaryReviewService reviewService;
    public MercenaryReviewController(MercenaryReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping()
    public ResponseEntity<List<MercenaryReviewResponseDto>> getAll(@RequestParam Long profileId) {
        return new ResponseEntity<>(reviewService.getAll(profileId), HttpStatus.OK);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<MercenaryReviewResponseDto> get(@RequestParam Long profileId, @PathVariable Long reviewId) {
        return new ResponseEntity<>(reviewService.get(profileId, reviewId), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Void> post(@RequestBody MercenaryReviewRequestDto request) {
        reviewService.post(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> update(@PathVariable Long reviewId, @RequestBody MercenaryReviewRequestDto request) {
        reviewService.update(reviewId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId) {
        reviewService.delete(reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
