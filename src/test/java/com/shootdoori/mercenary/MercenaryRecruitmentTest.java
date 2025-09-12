package com.shootdoori.mercenary;

import com.shootdoori.match.dto.RecruitmentCreateRequest;
import com.shootdoori.match.dto.RecruitmentResponse;
import com.shootdoori.match.dto.RecruitmentUpdateRequest;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.repository.MercenaryRecruitmentRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.service.MercenaryRecruitmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("용병 모집 서비스 단위 테스트")
class MercenaryRecruitmentTest {

    @InjectMocks
    private MercenaryRecruitmentService mercenaryRecruitmentService;

    @Mock
    private MercenaryRecruitmentRepository recruitmentRepository;

    @Mock
    private TeamRepository teamRepository;

    private final Team testTeam = new Team(
        "두리FC",
        User.create(
            "김학생",
            "student@example.com",
            "student@kangwon.ac.kr",
            "010-1234-5678",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "안녕하세요! 축구를 좋아하는 대학생입니다."
        ),
        "강원대학교",
        TeamType.CENTRAL_CLUB,
        SkillLevel.AMATEUR,
        "즐겜해요~"
    );

    private MercenaryRecruitment testRecruitment;

    @BeforeEach
    void setUp() {
        testRecruitment = MercenaryRecruitment.create(
            testTeam,
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            "Test Message",
            Position.CF,
            SkillLevel.AMATEUR
        );
    }

    @Test
    @DisplayName("용병 모집 공고 생성 - 성공")
    void create_success() {
        // given
        RecruitmentCreateRequest request = new RecruitmentCreateRequest(
            1L,
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            "Test Message",
            "골키퍼",
            "아마추어"
        );

        given(teamRepository.findById(request.teamId())).willReturn(Optional.of(testTeam));
        given(recruitmentRepository.save(any(MercenaryRecruitment.class))).willReturn(testRecruitment);

        // when
        RecruitmentResponse response = mercenaryRecruitmentService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("Test Message");
        verify(teamRepository).findById(1L);
        verify(recruitmentRepository).save(any(MercenaryRecruitment.class));
    }

    @Test
    @DisplayName("용병 모집 공고 생성 - 실패 (유효하지 않은 포지션)")
    void create_fail_invalidPosition() {
        // given
        RecruitmentCreateRequest request = new RecruitmentCreateRequest(
            1L,
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            "Test Message",
            "존재하지 않는 포지션",
            "아마추어"
        );

        given(teamRepository.findById(request.teamId())).willReturn(Optional.of(testTeam));

        // when & then
        assertThatIllegalArgumentException().isThrownBy(() -> mercenaryRecruitmentService.create(request));
    }

    @Test
    @DisplayName("용병 모집 공고 생성 - 실패 (경기 날짜가 과거)")
    void create_fail_matchDateInThePast() {
        // given
        RecruitmentCreateRequest request = new RecruitmentCreateRequest(
            1L,
            LocalDate.now().minusDays(1),
            LocalTime.of(18, 0),
            "Test Message",
            "공격수",
            "아마추어"
        );

        given(teamRepository.findById(request.teamId())).willReturn(Optional.of(testTeam));

        // when & then
        assertThatIllegalArgumentException().isThrownBy(() -> mercenaryRecruitmentService.create(request));
    }

    @Test
    @DisplayName("용병 모집 공고 생성 - 실패 (팀 없음)")
    void create_fail_teamNotFound() {
        // given
        RecruitmentCreateRequest request = new RecruitmentCreateRequest(999L, null, null, null, null, null);
        given(teamRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThrows(TeamNotFoundException.class, () -> {
            mercenaryRecruitmentService.create(request);
        });
    }

    @Test
    @DisplayName("용병 모집 공고 전체 조회 (페이지네이션) - 성공")
    void findAllPages_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<MercenaryRecruitment> recruitments = Collections.singletonList(testRecruitment);
        Page<MercenaryRecruitment> recruitmentPage = new PageImpl<>(recruitments, pageable, recruitments.size());

        given(recruitmentRepository.findAll(pageable)).willReturn(recruitmentPage);

        // when
        Page<RecruitmentResponse> responsePage = mercenaryRecruitmentService.findAllPages(pageable);

        // then
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getContent().get(0).message()).isEqualTo("Test Message");
    }

    @Test
    @DisplayName("용병 모집 공고 단건 조회 - 성공")
    void findById_success() {
        // given
        given(recruitmentRepository.findById(1L)).willReturn(Optional.of(testRecruitment));

        // when
        RecruitmentResponse response = mercenaryRecruitmentService.findById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("Test Message");
    }

    @Test
    @DisplayName("용병 모집 공고 수정 - 성공")
    void update_success() {
        // given
        RecruitmentUpdateRequest updateRequest = new RecruitmentUpdateRequest(
            LocalDate.now().plusDays(2),
            LocalTime.of(20, 0),
            "Updated Message",
            "미드필더",
            "아마추어"
        );
        given(recruitmentRepository.findById(1L)).willReturn(Optional.of(testRecruitment));

        // when
        RecruitmentResponse response = mercenaryRecruitmentService.update(1L, updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("Updated Message");
        assertThat(response.matchDate()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    @DisplayName("용병 모집 공고 삭제 - 성공")
    void delete_success() {
        // given
        given(recruitmentRepository.findById(1L)).willReturn(Optional.of(testRecruitment));

        // when
        mercenaryRecruitmentService.delete(1L);

        // then
        verify(recruitmentRepository).deleteById(1L);
    }
}
