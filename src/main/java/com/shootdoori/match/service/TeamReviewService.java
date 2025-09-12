package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.repository.TeamReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamReviewService {
    private final TeamReviewRepository teamReviewRepository;

    public TeamReviewService(TeamReviewRepository teamReviewRepository) {
        this.teamReviewRepository = teamReviewRepository;
    }

    public List<TeamReviewResponseDto> getAllTeamReviews(Long teamId) {
        return teamReviewRepository.findAllByReviewedTeamId(teamId).stream()
                .map(TeamReviewResponseDto::from)
                .toList();
    }

    public TeamReviewResponseDto getTeamReview(Long teamId, Long reviewId) {
        return teamReviewRepository.findByReviewedTeamIdAndId(teamId, reviewId);
    }
}
