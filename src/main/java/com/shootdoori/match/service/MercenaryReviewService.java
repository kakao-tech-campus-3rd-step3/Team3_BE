package com.shootdoori.match.service;

import com.shootdoori.match.dto.MercenaryReviewRequestDto;
import com.shootdoori.match.dto.MercenaryReviewResponseDto;

import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.review.MercenaryReview;

import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;

import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.MercenaryReviewRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MercenaryReviewService {
    private final MercenaryReviewRepository mercenaryReviewRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public MercenaryReviewService(MercenaryReviewRepository mercenaryReviewRepository,
                                  ProfileRepository profileRepository,
                                  MatchRepository matchRepository,
                                  TeamRepository teamRepository) {
        this.mercenaryReviewRepository = mercenaryReviewRepository;
        this.profileRepository = profileRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<MercenaryReviewResponseDto> getAll(Long userId) {
        return mercenaryReviewRepository.findAllByMercenaryUserId(userId).stream()
                .map(MercenaryReviewResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MercenaryReviewResponseDto get(Long userId, Long reviewId) {
        return MercenaryReviewResponseDto.from(mercenaryReviewRepository.findByMercenaryUserIdAndId(userId, reviewId));
    }

    @Transactional
    public void post(MercenaryReviewRequestDto mercenaryReviewRequestDto) {
        Match match = matchRepository.findById(mercenaryReviewRequestDto.matchId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_NOT_FOUND));

        Team reviewerTeam = teamRepository.findById(mercenaryReviewRequestDto.reviewerTeamId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND));

        User mercenary = profileRepository.findById(mercenaryReviewRequestDto.userId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));

        MercenaryReview mercenaryReview = new MercenaryReview(match, reviewerTeam, mercenary,
                mercenaryReviewRequestDto.rating(), mercenaryReviewRequestDto.punctualityReview(),
                mercenaryReviewRequestDto.sportsmanshipReview(), mercenaryReviewRequestDto.skillLevelReview());
        mercenaryReviewRepository.save(mercenaryReview);
    }

    @Transactional
    public void update(Long reviewId, MercenaryReviewRequestDto mercenaryReviewRequestDto) {
        MercenaryReview mercenaryReview = mercenaryReviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MERCENARY_REVIEW_NOT_FOUND));

        Match match = matchRepository.findById(mercenaryReviewRequestDto.matchId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_NOT_FOUND));

        Team reviewerTeam = teamRepository.findById(mercenaryReviewRequestDto.reviewerTeamId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND));

        User mercenary = profileRepository.findById(mercenaryReviewRequestDto.userId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_NOT_FOUND));

        MercenaryReview newMercenaryReview = new MercenaryReview(match, reviewerTeam, mercenary,
                mercenaryReviewRequestDto.rating(), mercenaryReviewRequestDto.punctualityReview(),
                mercenaryReviewRequestDto.sportsmanshipReview(), mercenaryReviewRequestDto.skillLevelReview());

        mercenaryReview.update(newMercenaryReview);
    }

    public void delete(Long reviewId) {
        mercenaryReviewRepository.deleteById(reviewId);
    }
}
