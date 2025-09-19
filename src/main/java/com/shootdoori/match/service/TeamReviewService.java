package com.shootdoori.match.service;

import com.shootdoori.match.dto.TeamReviewRequestDto;
import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.entity.Match;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamReview;
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

    public List<TeamReviewResponseDto> getAllTeamReviews(Long teamId) {
        return teamReviewRepository.findAllByReviewedTeamTeamId(teamId).stream()
                .map(TeamReviewResponseDto::from)
                .toList();
    }

    public TeamReviewResponseDto getTeamReview(Long teamId, Long reviewId) {
        return TeamReviewResponseDto.from(teamReviewRepository.findByReviewedTeam_TeamIdAndId(teamId, reviewId));
    }

    @Transactional
    public void postTeamReview(TeamReviewRequestDto teamReviewRequestDto) {
        Match match = matchRepository.findByMatchId(teamReviewRequestDto.matchId()) ;
        Team reviewerTeam = teamRepository.findByTeamId(teamReviewRequestDto.reviewerTeamId());
        Team reviewedTeam = teamRepository.findByTeamId(teamReviewRequestDto.reviewedTeamId()) ;
        TeamReview teamReview = TeamReview.from(match, reviewerTeam, reviewedTeam,
                teamReviewRequestDto.rating(), teamReviewRequestDto.punctualityReview(),
                teamReviewRequestDto.sportsmanshipReview(), teamReviewRequestDto.skillLevelReview());
        teamReviewRepository.save(teamReview);
    }

    @Transactional
    public void updateTeamReview(Long reviewId, TeamReviewRequestDto teamReviewRequestDto) {
        TeamReview teamReview = teamReviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 리뷰를 찾을 수 없습니다."));
        Match match = matchRepository.findByMatchId(teamReviewRequestDto.matchId()) ;
        Team reviewerTeam = teamRepository.findByTeamId(teamReviewRequestDto.reviewerTeamId());
        Team reviewedTeam = teamRepository.findByTeamId(teamReviewRequestDto.reviewedTeamId()) ;
        teamReview.update(TeamReview.from(match, reviewerTeam, reviewedTeam,
                teamReviewRequestDto.rating(), teamReviewRequestDto.punctualityReview(),
                teamReviewRequestDto.sportsmanshipReview(), teamReviewRequestDto.skillLevelReview()));
    }

    public void deleteTeamReview(Long reviewId) {
        teamReviewRepository.deleteById(reviewId);
    }
}
