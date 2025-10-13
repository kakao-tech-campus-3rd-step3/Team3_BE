package com.shootdoori.match.review;

import com.shootdoori.match.dto.TeamReviewRequestDto;
import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.review.ReviewBinaryEvaluation;
import com.shootdoori.match.entity.review.ReviewSkillLevel;
import com.shootdoori.match.entity.review.TeamReview;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.repository.TeamReviewRepository;
import com.shootdoori.match.service.TeamReviewService;
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
class TeamReviewServiceTest {

    @InjectMocks
    private TeamReviewService teamReviewService;

    @Mock
    private TeamReviewRepository teamReviewRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private Team reviewedTeamMock;
    @Mock
    private Team reviewerTeamMock;
    @Mock
    private Match matchMock;

    @DisplayName("특정 팀의 모든 리뷰 조회 성공")
    @Test
    void getAll_Success() {
        // given (준비)
        Long reviewedTeamId = 1L;
        TeamReview review1 = createMockTeamReview(1L, 101L, 201L, reviewedTeamId);
        TeamReview review2 = createMockTeamReview(2L, 102L, 202L, reviewedTeamId);

        given(teamReviewRepository.findAllByReviewedTeamTeamId(reviewedTeamId)).willReturn(List.of(review1, review2));

        // when (실행)
        List<TeamReviewResponseDto> result = teamReviewService.getAll(reviewedTeamId);

        // then (검증)
        assertThat(result).hasSize(2);
        assertThat(result.get(0).teamReviewId()).isEqualTo(1L);
        assertThat(result.get(0).matchId()).isEqualTo(101L);
        assertThat(result.get(1).teamReviewId()).isEqualTo(2L);
        assertThat(result.get(1).matchId()).isEqualTo(102L);
        then(teamReviewRepository).should().findAllByReviewedTeamTeamId(reviewedTeamId);
    }

    @DisplayName("특정 리뷰 단건 조회 성공")
    @Test
    void get_Success() {
        // given
        Long reviewedTeamId = 1L;
        Long reviewId = 1L;
        TeamReview review = createMockTeamReview(reviewId, 101L, 201L, reviewedTeamId);

        given(teamReviewRepository.findByReviewedTeamTeamIdAndId(reviewedTeamId, reviewId)).willReturn(review);

        // when
        TeamReviewResponseDto result = teamReviewService.get(reviewedTeamId, reviewId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.teamReviewId()).isEqualTo(reviewId);
        assertThat(result.matchId()).isEqualTo(101L);
        assertThat(result.reviewerTeamId()).isEqualTo(201L);
        assertThat(result.reviewedTeamId()).isEqualTo(reviewedTeamId);
        then(teamReviewRepository).should().findByReviewedTeamTeamIdAndId(reviewedTeamId, reviewId);
    }

    @DisplayName("리뷰 등록 성공")
    @Test
    void post_Success() {
        // given
        TeamReviewRequestDto requestDto = createTeamReviewRequestDto();

        given(matchRepository.findById(requestDto.matchId())).willReturn(Optional.of(matchMock));
        given(teamRepository.findById(requestDto.reviewerTeamId())).willReturn(Optional.of(reviewerTeamMock));
        given(teamRepository.findById(requestDto.reviewedTeamId())).willReturn(Optional.of(reviewedTeamMock));

        given(matchMock.getStatus()).willReturn(MatchStatus.FINISHED);

        // when
        teamReviewService.post(requestDto);

        // then
        ArgumentCaptor<TeamReview> captor = ArgumentCaptor.forClass(TeamReview.class);
        then(teamReviewRepository).should().save(captor.capture()); // save에 전달된 TeamReview 객체 캡처

        TeamReview savedReview = captor.getValue();
        assertThat(savedReview.getMatch()).isEqualTo(matchMock);
        assertThat(savedReview.getReviewerTeam()).isEqualTo(reviewerTeamMock);
        assertThat(savedReview.getReviewedTeam()).isEqualTo(reviewedTeamMock);
        assertThat(savedReview.getRating()).isEqualTo(requestDto.rating());
        assertThat(savedReview.getPunctualityReview()).isEqualTo(requestDto.punctualityReview());
    }

    @DisplayName("리뷰 등록 실패 - 경기가 존재하지 않을 경우")
    @Test
    void post_Fail_WhenMatchNotFound() {
        // given
        TeamReviewRequestDto requestDto = createTeamReviewRequestDto();
        given(matchRepository.findById(requestDto.matchId())).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamReviewService.post(requestDto));
        assertThat(exception.getMessage()).isEqualTo("해당 매치를 찾을 수 없습니다.");
    }

    @DisplayName("리뷰 수정 성공")
    @Test
    void update_Success() {
        // given
        Long reviewId = 1L;
        TeamReviewRequestDto requestDto = new TeamReviewRequestDto(1L, 2L, 3L, 3,
                ReviewBinaryEvaluation.BAD, ReviewBinaryEvaluation.BAD, ReviewSkillLevel.SIMILAR);
        TeamReview existingReview = new TeamReview(null, null, null, 5,
                ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.SIMILAR);

        given(teamReviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
        given(matchRepository.findById(anyLong())).willReturn(Optional.of(matchMock));
        given(teamRepository.findById(2L)).willReturn(Optional.of(reviewerTeamMock));
        given(teamRepository.findById(3L)).willReturn(Optional.of(reviewedTeamMock));

        given(matchMock.getStatus()).willReturn(MatchStatus.FINISHED);

        // when
        teamReviewService.update(reviewId, requestDto);

        // then
        then(teamReviewRepository).should().findById(reviewId);

        assertThat(existingReview.getRating()).isEqualTo(3);
        assertThat(existingReview.getPunctualityReview()).isEqualTo(ReviewBinaryEvaluation.BAD);
        assertThat(existingReview.getSkillLevelReview()).isEqualTo(ReviewSkillLevel.SIMILAR);
        assertThat(existingReview.getMatch()).isEqualTo(matchMock); // Match 객체가 교체되었는지 확인
    }

    @DisplayName("리뷰 수정 실패 - 리뷰가 존재하지 않을 경우")
    @Test
    void update_Fail_WhenReviewNotFound() {
        // given
        Long reviewId = 1L;
        TeamReviewRequestDto requestDto = createTeamReviewRequestDto();

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamReviewService.post(requestDto));
        assertThat(exception.getMessage()).isEqualTo("해당 매치를 찾을 수 없습니다.");
    }

    @DisplayName("리뷰 삭제 성공")
    @Test
    void delete_Success() {
        // given
        Long reviewId = 1L;
        willDoNothing().given(teamReviewRepository).deleteById(reviewId);

        // when
        teamReviewService.delete(reviewId);

        // then
        then(teamReviewRepository).should().deleteById(reviewId);
    }

    // 테스트 데이터 생성용 메서드

    private TeamReview createMockTeamReview(Long reviewId, Long matchId, Long reviewerId, Long reviewedId) {
        TeamReview review = mock(TeamReview.class);
        Match match = mock(Match.class);
        Team reviewer = mock(Team.class);
        Team reviewed = mock(Team.class);

        given(review.getId()).willReturn(reviewId);
        given(review.getMatch()).willReturn(match);
        given(match.getMatchId()).willReturn(matchId);
        given(review.getReviewerTeam()).willReturn(reviewer);
        given(reviewer.getTeamId()).willReturn(reviewerId);
        given(review.getReviewedTeam()).willReturn(reviewed);
        given(reviewed.getTeamId()).willReturn(reviewedId);
        given(review.getRating()).willReturn(5); // 기본값 설정

        return review;
    }

    private TeamReviewRequestDto createTeamReviewRequestDto() {
        return new TeamReviewRequestDto(1L, 2L, 3L, 5,
                ReviewBinaryEvaluation.GOOD, ReviewBinaryEvaluation.GOOD, ReviewSkillLevel.HIGHER);
    }
}