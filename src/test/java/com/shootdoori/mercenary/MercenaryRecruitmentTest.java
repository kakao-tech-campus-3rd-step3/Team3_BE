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
import org.springframework.test.util.ReflectionTestUtils;

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
@DisplayName("용병 모집 테스트")
class MercenaryRecruitmentTest {

    @InjectMocks
    private MercenaryRecruitmentService mercenaryRecruitmentService;

    @Mock
    private MercenaryRecruitmentRepository recruitmentRepository;

    @Mock
    private TeamRepository teamRepository;

    private Team testTeam;
    private MercenaryRecruitment testRecruitment;
    private RecruitmentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testTeam = Fixture.createTeam();
        ReflectionTestUtils.setField(testTeam, "teamId", 1L);

        testRecruitment = Fixture.createRecruitment(testTeam);
        createRequest = Fixture.createRecruitmentRequest(testTeam.getTeamId());
    }

    @Test
    @DisplayName("용병 모집 공고 생성 - 성공")
    void create_success() {
        // given
        given(teamRepository.findById(createRequest.teamId())).willReturn(Optional.of(testTeam));
        given(recruitmentRepository.save(any(MercenaryRecruitment.class))).willReturn(testRecruitment);

        // when
        RecruitmentResponse response = mercenaryRecruitmentService.create(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo(Fixture.MESSAGE);
        verify(teamRepository).findById(testTeam.getTeamId());
        verify(recruitmentRepository).save(any(MercenaryRecruitment.class));
    }

    @Test
    @DisplayName("용병 모집 공고 생성 - 실패 (유효하지 않은 포지션)")
    void create_fail_invalidPosition() {
        // given
        RecruitmentCreateRequest request = new RecruitmentCreateRequest(
            testTeam.getTeamId(),
            Fixture.MATCH_DATE,
            Fixture.MATCH_START_TIME,
            Fixture.MESSAGE,
            "존재하지 않는 포지션",
            Fixture.SKILL_LEVEL
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
            testTeam.getTeamId(),
            LocalDate.now().minusDays(1),
            Fixture.MATCH_START_TIME,
            Fixture.MESSAGE,
            Fixture.POSITION,
            Fixture.SKILL_LEVEL
        );

        given(teamRepository.findById(request.teamId())).willReturn(Optional.of(testTeam));

        // when & then
        assertThatIllegalArgumentException().isThrownBy(() -> mercenaryRecruitmentService.create(request));
    }

    @Test
    @DisplayName("용병 모집 공고 생성 - 실패 (팀 없음)")
    void create_fail_teamNotFound() {
        // given
        long nonExistentTeamId = 999L;
        RecruitmentCreateRequest request = Fixture.createRecruitmentRequest(nonExistentTeamId);
        given(teamRepository.findById(nonExistentTeamId)).willReturn(Optional.empty());

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
        assertThat(responsePage.getContent().get(0).message()).isEqualTo(Fixture.MESSAGE);
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
        assertThat(response.message()).isEqualTo(Fixture.MESSAGE);
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

    static class Fixture {
        public static final String MESSAGE = "Test Message";
        public static final String POSITION = "공격수";
        public static final String SKILL_LEVEL = "아마추어";
        public static final LocalDate MATCH_DATE = LocalDate.now().plusDays(1);
        public static final LocalTime MATCH_START_TIME = LocalTime.of(18, 0);

        public static Team createTeam() {
            return new Team(
                "두리FC",
                User.create(
                    "김학생", "아마추어", "student@example.com", "student@kangwon.ac.kr",
                    "asdf02~!", "010-1234-5678", "공격수", "강원대학교", "컴퓨터공학과", "20", "안녕하세요!"),
                "강원대학교", TeamType.CENTRAL_CLUB, SkillLevel.AMATEUR, "즐겜해요~"
            );
        }

        public static MercenaryRecruitment createRecruitment(Team team) {
            return MercenaryRecruitment.create(
                team,
                MATCH_DATE,
                MATCH_START_TIME,
                MESSAGE,
                Position.fromDisplayName(POSITION),
                SkillLevel.fromDisplayName(SKILL_LEVEL)
            );
        }

        public static RecruitmentCreateRequest createRecruitmentRequest(Long teamId) {
            return new RecruitmentCreateRequest(
                teamId,
                MATCH_DATE,
                MATCH_START_TIME,
                MESSAGE,
                POSITION,
                SKILL_LEVEL
            );
        }
    }
}