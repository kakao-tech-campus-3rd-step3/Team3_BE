package com.shootdoori.match.review;

import com.shootdoori.match.dto.MercenaryReviewRequestDto;
import com.shootdoori.match.dto.MercenaryReviewResponseDto;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.review.MercenaryReview;
import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.MercenaryReviewRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.service.MercenaryReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MercenaryReviewServiceTest {

    @InjectMocks
    private MercenaryReviewService mercenaryReviewService;

    @Mock
    private MercenaryReviewRepository mercenaryReviewRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private User mercenaryUserMock;
    @Mock
    private Team reviewerTeamMock;
    @Mock
    private Match matchMock;


    @DisplayName("특정 용병의 모든 리뷰 조회 성공")
    @Test
    void getAll_Success() {
        // given
        Long mercenaryUserId = 1L;
        MercenaryReview review1 = createMockMercenaryReview(10L, 101L, 201L, mercenaryUserId);
        MercenaryReview review2 = createMockMercenaryReview(11L, 102L, 202L, mercenaryUserId);

        given(mercenaryReviewRepository.findAllByMercenaryUserId(mercenaryUserId)).willReturn(List.of(review1, review2));

        // when
        List<MercenaryReviewResponseDto> result = mercenaryReviewService.getAll(mercenaryUserId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).mercenaryReviewId()).isEqualTo(10L);
        assertThat(result.get(1).mercenaryReviewId()).isEqualTo(11L);
        then(mercenaryReviewRepository).should().findAllByMercenaryUserId(mercenaryUserId);
    }

    @DisplayName("특정 용병 리뷰 단건 조회 성공")
    @Test
    void get_Success() {
        // given
        Long mercenaryUserId = 1L;
        Long reviewId = 10L;
        MercenaryReview review = createMockMercenaryReview(reviewId, 101L, 201L, mercenaryUserId);
        given(mercenaryReviewRepository.findByMercenaryUserIdAndId(mercenaryUserId, reviewId)).willReturn(review);

        // when
        MercenaryReviewResponseDto result = mercenaryReviewService.get(mercenaryUserId, reviewId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.mercenaryReviewId()).isEqualTo(reviewId);
        assertThat(result.matchId()).isEqualTo(101L);
        assertThat(result.reviewerTeamId()).isEqualTo(201L);
        assertThat(result.userId()).isEqualTo(mercenaryUserId);
        then(mercenaryReviewRepository).should().findByMercenaryUserIdAndId(mercenaryUserId, reviewId);
    }

    @DisplayName("용병 리뷰 등록 성공")
    @Test
    void post_Success() {
        // given
        MercenaryReviewRequestDto requestDto = createMercenaryReviewRequestDto();

        given(matchRepository.findById(requestDto.matchId())).willReturn(Optional.of(matchMock));
        given(teamRepository.findById(requestDto.reviewerTeamId())).willReturn(Optional.of(reviewerTeamMock));
        given(profileRepository.findById(requestDto.userId())).willReturn(Optional.of(mercenaryUserMock));

        given(matchMock.getStatus()).willReturn(MatchStatus.FINISHED);

        // when
        mercenaryReviewService.post(requestDto);

        // then
        ArgumentCaptor<MercenaryReview> captor = ArgumentCaptor.forClass(MercenaryReview.class);
        then(mercenaryReviewRepository).should().save(captor.capture());

        MercenaryReview savedReview = captor.getValue();
        assertThat(savedReview.getMatch()).isEqualTo(matchMock);
        assertThat(savedReview.getReviewerTeam()).isEqualTo(reviewerTeamMock);
        assertThat(savedReview.getMercenaryUser()).isEqualTo(mercenaryUserMock);
        assertThat(savedReview.getRating()).isEqualTo(requestDto.rating());
    }

    @DisplayName("용병 리뷰 등록 실패 - 매치를 찾을 수 없음")
    @Test
    void post_Fail_WhenMatchNotFound() {
        // given
        MercenaryReviewRequestDto requestDto = createMercenaryReviewRequestDto();
        given(matchRepository.findById(requestDto.matchId())).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> mercenaryReviewService.post(requestDto));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MATCH_NOT_FOUND);
    }

    @DisplayName("용병 리뷰 수정 성공")
    @Test
    void update_Success() {
        // given
        Long reviewId = 10L;
        MercenaryReviewRequestDto requestDto = new MercenaryReviewRequestDto(1L, 2L, 3L, 3,
                ReviewBinaryEvaluation.BAD, ReviewBinaryEvaluation.BAD, ReviewSkillLevel.SIMILAR);

        MercenaryReview existingReview = new MercenaryReview(null, null, null, 5,
                ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);

        given(mercenaryReviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
        given(matchRepository.findById(requestDto.matchId())).willReturn(Optional.of(matchMock));
        given(teamRepository.findById(requestDto.reviewerTeamId())).willReturn(Optional.of(reviewerTeamMock));
        given(profileRepository.findById(requestDto.userId())).willReturn(Optional.of(mercenaryUserMock));

        given(matchMock.getStatus()).willReturn(MatchStatus.FINISHED);

        // when
        mercenaryReviewService.update(reviewId, requestDto);

        // then
        then(mercenaryReviewRepository).should().findById(reviewId);
        assertThat(existingReview.getRating()).isEqualTo(3);
        assertThat(existingReview.getPunctualityReview()).isEqualTo(ReviewBinaryEvaluation.BAD);
        assertThat(existingReview.getMatch()).isEqualTo(matchMock);
        assertThat(existingReview.getReviewerTeam()).isEqualTo(reviewerTeamMock);
        assertThat(existingReview.getMercenaryUser()).isEqualTo(mercenaryUserMock);
    }

    @DisplayName("용병 리뷰 수정 실패 - 리뷰를 찾을 수 없음")
    @Test
    void update_Fail_WhenReviewNotFound() {
        // given
        Long reviewId = 10L;
        MercenaryReviewRequestDto requestDto = createMercenaryReviewRequestDto();
        given(mercenaryReviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> mercenaryReviewService.update(reviewId, requestDto));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MERCENARY_REVIEW_NOT_FOUND);
    }

    @DisplayName("용병 리뷰 삭제 성공")
    @Test
    void delete_Success() {
        // given
        Long reviewId = 10L;
        willDoNothing().given(mercenaryReviewRepository).deleteById(reviewId);

        // when
        mercenaryReviewService.delete(reviewId);

        // then
        then(mercenaryReviewRepository).should().deleteById(reviewId);
    }


    // 테스트 데이터 생성용 메서드

    private MercenaryReview createMockMercenaryReview(Long reviewId, Long matchId, Long reviewerTeamId, Long mercenaryUserId) {
        MercenaryReview review = mock(MercenaryReview.class);
        Match match = mock(Match.class);
        Team reviewerTeam = mock(Team.class);
        User mercenaryUser = mock(User.class);

        given(review.getId()).willReturn(reviewId);
        given(review.getMatch()).willReturn(match);
        given(match.getMatchId()).willReturn(matchId);
        given(review.getReviewerTeam()).willReturn(reviewerTeam);
        given(reviewerTeam.getTeamId()).willReturn(reviewerTeamId);
        given(review.getMercenaryUser()).willReturn(mercenaryUser);
        given(mercenaryUser.getId()).willReturn(mercenaryUserId);
        given(review.getRating()).willReturn(5);

        return review;
    }

    private MercenaryReviewRequestDto createMercenaryReviewRequestDto() {
        return new MercenaryReviewRequestDto(1L, 2L, 3L, 5,
                ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.HIGHER);
    }
}