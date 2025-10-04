package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamReviewRequestDto;
import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.review.TeamReview;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.repository.TeamReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamReviewService {
    private final TeamReviewRepository teamReviewRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public TeamReviewService(TeamReviewRepository teamReviewRepository,
                             TeamRepository teamRepository,
                             MatchRepository matchRepository) {
        this.teamReviewRepository = teamReviewRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamReviewResponseDto> getAll(Long teamId) {
        return teamReviewRepository.findAllByReviewedTeamTeamId(teamId).stream()
                .map(TeamReviewResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamReviewResponseDto get(Long teamId, Long reviewId) {
        return TeamReviewResponseDto.from(teamReviewRepository.findByReviewedTeamTeamIdAndId(teamId, reviewId));
    }

    @Transactional
    public void post(TeamReviewRequestDto teamReviewRequestDto) {
        Match match = matchRepository.findById(teamReviewRequestDto.matchId())
                .orElseThrow(() -> new IllegalArgumentException("match doesn't exist"));

        Team reviewerTeam = teamRepository.findById(teamReviewRequestDto.reviewerTeamId())
                .orElseThrow(() -> new IllegalArgumentException("team doesn't exist"));

        Team reviewedTeam = teamRepository.findById(teamReviewRequestDto.reviewedTeamId())
                .orElseThrow(() -> new IllegalArgumentException("team doesn't exist"));

        TeamReview teamReview = TeamReview.from(match, reviewerTeam, reviewedTeam,
                teamReviewRequestDto.rating(), teamReviewRequestDto.punctualityReview(),
                teamReviewRequestDto.sportsmanshipReview(), teamReviewRequestDto.skillLevelReview());
        teamReviewRepository.save(teamReview);
    }

    @Transactional
    public void update(Long reviewId, TeamReviewRequestDto teamReviewRequestDto) {
        TeamReview teamReview = teamReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("teamReview doesn't exist"));

        Match match = matchRepository.findById(teamReviewRequestDto.matchId())
                .orElseThrow(() -> new IllegalArgumentException("match doesn't exist"));

        Team reviewerTeam = teamRepository.findById(teamReviewRequestDto.reviewerTeamId())
                .orElseThrow(() -> new IllegalArgumentException("team doesn't exist"));

        Team reviewedTeam = teamRepository.findById(teamReviewRequestDto.reviewedTeamId())
                .orElseThrow(() -> new IllegalArgumentException("team doesn't exist"));

        teamReview.update(TeamReview.from(match, reviewerTeam, reviewedTeam,
                teamReviewRequestDto.rating(), teamReviewRequestDto.punctualityReview(),
                teamReviewRequestDto.sportsmanshipReview(), teamReviewRequestDto.skillLevelReview()));
    }

    public void delete(Long reviewId) {
        teamReviewRepository.deleteById(reviewId);
    }
}
