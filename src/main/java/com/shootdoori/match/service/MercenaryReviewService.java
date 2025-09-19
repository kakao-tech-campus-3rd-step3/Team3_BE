package com.shootdoori.match.service;

import com.shootdoori.match.dto.MercenaryReviewRequestDto;
import com.shootdoori.match.dto.MercenaryReviewResponseDto;
import com.shootdoori.match.entity.Match;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.entity.MercenaryReview;
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

    public List<MercenaryReviewResponseDto> getAll(Long userId) {
        return mercenaryReviewRepository.findAllByMercenaryUserId(userId).stream()
                .map(MercenaryReviewResponseDto::from)
                .toList();
    }

    public MercenaryReviewResponseDto get(Long userId, Long reviewId) {
        return MercenaryReviewResponseDto.from(mercenaryReviewRepository.findByMercenaryUserIdAndId(userId, reviewId));
    }

    @Transactional
    public void post(MercenaryReviewRequestDto mercenaryReviewRequestDto) {
        Match match = matchRepository.findById(mercenaryReviewRequestDto.matchId())
                .orElseThrow(() -> new IllegalArgumentException("match doesn't exist"));

        Team reviewerTeam = teamRepository.findById(mercenaryReviewRequestDto.reviewerTeamId())
                .orElseThrow(() -> new IllegalArgumentException("team doesn't exist"));

        User mercenary = profileRepository.findById(mercenaryReviewRequestDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("profile doesn't exist"));

        MercenaryReview mercenaryReview = new MercenaryReview(match, reviewerTeam, mercenary,
                mercenaryReviewRequestDto.rating(), mercenaryReviewRequestDto.punctualityReview(),
                mercenaryReviewRequestDto.sportsmanshipReview(), mercenaryReviewRequestDto.skillLevelReview());
        mercenaryReviewRepository.save(mercenaryReview);
    }

    @Transactional
    public void update(Long reviewId, MercenaryReviewRequestDto mercenaryReviewRequestDto) {
        MercenaryReview mercenaryReview = mercenaryReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("mercenaryReview doesn't exist"));

        Match match = matchRepository.findById(mercenaryReviewRequestDto.matchId())
                .orElseThrow(() -> new IllegalArgumentException("match doesn't exist"));

        Team reviewerTeam = teamRepository.findById(mercenaryReviewRequestDto.reviewerTeamId())
                .orElseThrow(() -> new IllegalArgumentException("team doesn't exist"));

        User mercenary = profileRepository.findById(mercenaryReviewRequestDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("profile doesn't exist"));

        MercenaryReview newMercenaryReview = new MercenaryReview(match, reviewerTeam, mercenary,
                mercenaryReviewRequestDto.rating(), mercenaryReviewRequestDto.punctualityReview(),
                mercenaryReviewRequestDto.sportsmanshipReview(), mercenaryReviewRequestDto.skillLevelReview());

        mercenaryReview.update(newMercenaryReview);
    }

    public void delete(Long reviewId) {
        mercenaryReviewRepository.deleteById(reviewId);
    }
}
