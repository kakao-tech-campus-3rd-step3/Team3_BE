package com.shootdoori.joinWaiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shootdoori.match.dto.JoinWaitingApproveRequestDto;
import com.shootdoori.match.dto.JoinWaitingCancelRequestDto;
import com.shootdoori.match.dto.JoinWaitingMapper;
import com.shootdoori.match.dto.JoinWaitingRejectRequestDto;
import com.shootdoori.match.dto.JoinWaitingRequestDto;
import com.shootdoori.match.dto.JoinWaitingResponseDto;
import com.shootdoori.match.entity.JoinWaiting;
import com.shootdoori.match.entity.JoinWaitingStatus;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedException;
import com.shootdoori.match.exception.NotFoundException;
import com.shootdoori.match.repository.JoinWaitingRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.service.JoinWaitingService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("JoinWaitingService 테스트")
public class JoinWaitingServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private JoinWaitingRepository joinWaitingRepository;

    @Mock
    private JoinWaitingMapper joinWaitingMapper;

    private JoinWaitingService joinWaitingService;

    private Team team;
    private User teamLeader;
    private User applicant;
    private User anotherUser;
    private TeamMember leaderMember;

    private static final Long TEAM_ID = 1L;
    private static final Long JOIN_WAITING_ID = 1L;
    private static final Long TEAM_MEMBER_ID = 1L;
    private static final int PAGE = 0;
    private static final int SIZE = 10;
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);

    private static final String MEMBER = TeamMemberRole.MEMBER.getDisplayName();

    @BeforeEach
    void setUp() {
        joinWaitingService = new JoinWaitingService(
            profileRepository,
            teamRepository,
            teamMemberRepository,
            joinWaitingRepository,
            joinWaitingMapper
        );

        teamLeader = User.create(
            "팀리더",
            "세미프로",
            "leader@example.com",
            "leader@kangwon.ac.kr",
            "Abcd1234!",
            "010-1111-1111",
            "미드필더",
            "강원대학교",
            "체육학과",
            "25",
            "팀을 이끌어가는 리더입니다."
        );

        applicant = User.create(
            "신청자",
            "아마추어",
            "applicant@example.com",
            "applicant@kangwon.ac.kr",
            "Abcd1234!",
            "010-2222-2222",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "22",
            "축구를 좋아하는 학생입니다."
        );

        anotherUser = User.create(
            "다른사용자",
            "프로",
            "other@example.com",
            "other@kangwon.ac.kr",
            "Abcd1234!",
            "010-3333-3333",
            "수비수",
            "강원대학교",
            "경영학과",
            "24",
            "저는 그저 다른 사용자입니다."
        );

        ReflectionTestUtils.setField(teamLeader, "id", 1L);
        ReflectionTestUtils.setField(applicant, "id", 2L);
        ReflectionTestUtils.setField(anotherUser, "id", 3L);

        team = new Team(
            "강원대 FC",
            teamLeader,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("세미프로"),
            "주 3회 연습합니다."
        );

        team.recruitMember(teamLeader, TeamMemberRole.LEADER);
        leaderMember = team.getMembers().get(0);
        ReflectionTestUtils.setField(leaderMember, "id", 1L);
    }

    @Nested
    @DisplayName("create 테스트")
    class CreateTest {

        @Test
        @DisplayName("create - 정상 생성")
        void create_success() {
            // given
            Long applicantId = applicant.getId();

            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(applicantId,
                "파트라슈처럼 뛰겠습니다.");

            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "파트라슈처럼 뛰겠습니다.");

            JoinWaitingResponseDto expected = new JoinWaitingResponseDto(
                JOIN_WAITING_ID, TEAM_ID, applicantId,
                JoinWaitingStatus.PENDING.getDisplayName(),
                null, null, null
            );

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID, applicantId))
                .thenReturn(false);
            when(joinWaitingRepository.existsByTeam_TeamIdAndApplicant_IdAndStatus(TEAM_ID,
                applicantId,
                JoinWaitingStatus.PENDING)).thenReturn(false);
            when(joinWaitingRepository.save(any(JoinWaiting.class))).thenReturn(joinWaiting);
            when(joinWaitingMapper.toJoinWaitingResponseDto(any(JoinWaiting.class))).thenReturn(
                expected);

            // when
            JoinWaitingResponseDto resultDto = joinWaitingService.create(TEAM_ID, requestDto);

            // then
            assertThat(resultDto).isEqualTo(expected);
            assertThat(resultDto.id()).isEqualTo(JOIN_WAITING_ID);
            assertThat(resultDto.status()).isEqualTo(JoinWaitingStatus.PENDING.getDisplayName());
        }

        @Test
        @DisplayName("create - 이미 팀원인 경우 예외")
        void create_alreadyMember_throws() {
            // given
            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(applicant.getId(),
                "파트라슈처럼 뛰겠습니다.");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(applicant.getId())).thenReturn(Optional.of(applicant));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID,
                applicant.getId())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> joinWaitingService.create(TEAM_ID, requestDto))
                .isInstanceOf(DuplicatedException.class);
        }

        @Test
        @DisplayName("create - 대기중 신청 중복 예외")
        void create_duplicatePending_throws() {
            // given
            Long applicantId = applicant.getId();

            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(applicantId,
                "파트라슈처럼 뛰겠습니다.");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID,
                applicantId)).thenReturn(
                false);
            when(joinWaitingRepository.existsByTeam_TeamIdAndApplicant_IdAndStatus(TEAM_ID,
                applicantId,
                JoinWaitingStatus.PENDING)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> joinWaitingService.create(TEAM_ID, requestDto))
                .isInstanceOf(DuplicatedException.class);
        }

        @Test
        @DisplayName("create - 팀 없음 예외")
        void create_teamNotFound_throws() {
            // given
            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(applicant.getId(),
                "파트라슈처럼 뛰겠습니다.");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> joinWaitingService.create(TEAM_ID, requestDto))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("create - 사용자 없음 예외")
        void create_userNotFound_throws() {
            // given
            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(applicant.getId(),
                "파트라슈처럼 뛰겠습니다.");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(applicant.getId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> joinWaitingService.create(TEAM_ID, requestDto))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("approve 테스트")
    class ApproveTest {

        @Test
        @DisplayName("approve - 정상 승인")
        void approve_success() {
            // given
            Long applicantId = applicant.getId();
            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
                TEAM_MEMBER_ID, MEMBER, "승인합니다.");

            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입요청");

            when(teamMemberRepository.findByIdAndTeam_TeamId(TEAM_MEMBER_ID, TEAM_ID))
                .thenReturn(Optional.of(leaderMember));
            when(joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(JOIN_WAITING_ID, TEAM_ID))
                .thenReturn(Optional.of(joinWaiting));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID, applicantId))
                .thenReturn(false);

            JoinWaitingResponseDto expected = new JoinWaitingResponseDto(
                JOIN_WAITING_ID, TEAM_ID, applicantId,
                JoinWaitingStatus.APPROVED.getDisplayName(),
                "승인합니다", teamLeader.toString(), FIXED_TIME
            );

            when(joinWaitingMapper.toJoinWaitingResponseDto(any(JoinWaiting.class))).thenReturn(
                expected);

            // when
            JoinWaitingResponseDto resultDto = joinWaitingService.approve(TEAM_ID, JOIN_WAITING_ID,
                requestDto);

            // then
            assertThat(resultDto).isEqualTo(expected);
            assertThat(resultDto.status()).isEqualTo(JoinWaitingStatus.APPROVED.getDisplayName());

            assertThat(team.getMembers()).hasSize(2);
            assertThat(team.getMembers().get(1).getUser()).isEqualTo(applicant);
            assertThat(team.getMembers().get(1).getRole()).isEqualTo(TeamMemberRole.MEMBER);
        }

        @Test
        @DisplayName("approve - 승인자 팀 멤버 없음 예외")
        void approve_teamMemberNotFound_throws() {
            // given
            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
                TEAM_MEMBER_ID, MEMBER, "승인합니다.");

            when(teamMemberRepository.findByIdAndTeam_TeamId(TEAM_MEMBER_ID, TEAM_ID))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> joinWaitingService.approve(TEAM_ID, JOIN_WAITING_ID, requestDto))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("approve - JoinWaiting 없음 예외")
        void approve_joinWaitingNotFound_throws() {
            // given
            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
                TEAM_MEMBER_ID, MEMBER, "승인합니다.");

            when(teamMemberRepository.findByIdAndTeam_TeamId(TEAM_MEMBER_ID, TEAM_ID))
                .thenReturn(Optional.of(leaderMember));
            when(joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(JOIN_WAITING_ID, TEAM_ID))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> joinWaitingService.approve(TEAM_ID, JOIN_WAITING_ID, requestDto))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("approve - 이미 팀원인 경우 예외")
        void approve_alreadyMember_throws() {
            // given
            Long applicantId = applicant.getId();

            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
                TEAM_MEMBER_ID, MEMBER, "승인합니다.");

            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입요청");

            when(teamMemberRepository.findByIdAndTeam_TeamId(TEAM_MEMBER_ID, TEAM_ID))
                .thenReturn(Optional.of(leaderMember));
            when(joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(JOIN_WAITING_ID, TEAM_ID))
                .thenReturn(Optional.of(joinWaiting));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID, applicantId))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(
                () -> joinWaitingService.approve(TEAM_ID, JOIN_WAITING_ID, requestDto))
                .isInstanceOf(DuplicatedException.class);
        }
    }

    @Nested
    @DisplayName("reject 테스트")
    class RejectTest {

        @Test
        @DisplayName("reject - 정상 거절")
        void reject_success() {
            // given
            Long approverId = leaderMember.getId();
            Long applicantId = applicant.getId();

            JoinWaitingRejectRequestDto requestDto = new JoinWaitingRejectRequestDto(
                approverId, "죄송합니다.");

            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입요청");

            when(teamMemberRepository.findByIdAndTeam_TeamId(approverId, TEAM_ID))
                .thenReturn(Optional.of(leaderMember));
            when(joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(JOIN_WAITING_ID, TEAM_ID))
                .thenReturn(Optional.of(joinWaiting));

            JoinWaitingResponseDto expected = new JoinWaitingResponseDto(
                JOIN_WAITING_ID, TEAM_ID, applicantId,
                JoinWaitingStatus.REJECTED.getDisplayName(),
                "죄송합니다", teamLeader.toString(), FIXED_TIME
            );
            when(joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting)).thenReturn(expected);

            // when
            JoinWaitingResponseDto resultDto = joinWaitingService.reject(TEAM_ID, JOIN_WAITING_ID,
                requestDto);

            // then
            assertThat(resultDto.status()).isEqualTo(JoinWaitingStatus.REJECTED.getDisplayName());
            assertThat(team.getMembers()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("cancel 테스트")
    class CancelTest {

        @Test
        @DisplayName("cancel - 신청자 정상 취소")
        void cancel_success() {
            // given
            Long requesterId = applicant.getId();
            Long applicantId = applicant.getId();

            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입요청");

            JoinWaitingCancelRequestDto requestDto = new JoinWaitingCancelRequestDto(
                requesterId, "개인 사정으로 취소합니다.");

            when(profileRepository.findById(requesterId)).thenReturn(Optional.of(applicant));
            when(joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(JOIN_WAITING_ID, TEAM_ID))
                .thenReturn(Optional.of(joinWaiting));

            JoinWaitingResponseDto expected = new JoinWaitingResponseDto(
                JOIN_WAITING_ID, TEAM_ID, applicantId,
                JoinWaitingStatus.CANCELED.getDisplayName(),
                "개인 사정으로 취소합니다.", applicant.toString(), FIXED_TIME
            );
            when(joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting)).thenReturn(expected);

            // when
            JoinWaitingResponseDto resultDto = joinWaitingService.cancel(TEAM_ID, JOIN_WAITING_ID,
                requestDto);

            // then
            assertThat(resultDto.status()).isEqualTo(JoinWaitingStatus.CANCELED.getDisplayName());
            assertThat(team.getMembers()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findPending 테스트")
    class FindPendingTest {

        @Test
        @DisplayName("findPending - 대기중 목록 페이징/매핑")
        void findPending_success() {
            // given
            JoinWaiting joinWaiting1 = JoinWaiting.create(team, applicant, "가입요청");
            JoinWaiting joinWaiting2 = JoinWaiting.create(team, anotherUser, "가입요청2");

            List<JoinWaiting> joinWaitingList = List.of(joinWaiting1, joinWaiting2);
            PageRequest pageRequest = PageRequest.of(PAGE, SIZE, Sort.by("teamName").ascending());
            Page<JoinWaiting> joinWaitingPage = new PageImpl<>(joinWaitingList, pageRequest, 2);

            JoinWaitingResponseDto responseDto1 = new JoinWaitingResponseDto(
                1L, TEAM_ID, applicant.getId(), JoinWaitingStatus.PENDING.getDisplayName(), null,
                null, null
            );
            JoinWaitingResponseDto responseDto2 = new JoinWaitingResponseDto(
                2L, TEAM_ID, anotherUser.getId(), JoinWaitingStatus.PENDING.getDisplayName(), null,
                null, null
            );

            Page<JoinWaitingResponseDto> resultDtoPage = new PageImpl<>(
                List.of(responseDto1, responseDto2),
                PageRequest.of(PAGE, SIZE, Sort.by("teamName").ascending()), 2);

            assertThat(resultDtoPage).hasSize(2);
            assertThat(resultDtoPage.getContent()).hasSize(2);
            assertThat(resultDtoPage.getContent().get(0).id()).isEqualTo(1L);
            assertThat(resultDtoPage.getContent().get(1).id()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("findAllByApplicant_IdAndStatusIn 테스트")
    class findAllByApplicantIdAndStatusIn {

        private Team anotherTeam;
        private List<JoinWaitingStatus> targetStatuses = List.of(JoinWaitingStatus.PENDING,
            JoinWaitingStatus.REJECTED);

        @BeforeEach
        void setUpForfindAllByApplicantIdAndStatusIn() {
            anotherTeam = new Team(
                "감자빵 FC",
                anotherUser,
                "강원대학교",
                TeamType.fromDisplayName("과동아리"),
                SkillLevel.fromDisplayName("아마추어"),
                "주 2회 연습합니다."
            );
            ReflectionTestUtils.setField(anotherTeam, "teamId", 2L);
        }

        @Test
        @DisplayName("findAllByApplicant_IdAndStatusIn - 사용자 별 가입 요청 목록 페이징 조회")
        void findAllByApplicant_IdAndStatusIn_success() {
            // given
            Long applicantId = applicant.getId();

            JoinWaiting joinWaiting1 = JoinWaiting.create(team, applicant, "가입요청");
            JoinWaiting joinWaiting2 = JoinWaiting.create(anotherTeam, applicant, "가입요청2");

            List<JoinWaiting> joinWaitingList = List.of(joinWaiting1, joinWaiting2);
            PageRequest pageRequest = PageRequest.of(PAGE, SIZE);
            Page<JoinWaiting> joinWaitingPage = new PageImpl<>(joinWaitingList, pageRequest, 2);

            JoinWaitingResponseDto responseDto1 = new JoinWaitingResponseDto(
                1L, 1L, applicantId, JoinWaitingStatus.PENDING.getDisplayName(),
                "저 잘 뜁니다 1", null, null
            );

            JoinWaitingResponseDto responseDto2 = new JoinWaitingResponseDto(
                2L, 2L, applicantId, JoinWaitingStatus.PENDING.getDisplayName(),
                "저 잘 뜁니다 2", null, null
            );

            when(profileRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
            when(joinWaitingRepository.findAllByApplicant_IdAndStatusIn(applicantId,
                targetStatuses, pageRequest)).thenReturn(joinWaitingPage);
            when(joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting1)).thenReturn(responseDto1);
            when(joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting2)).thenReturn(responseDto2);

            // when
            Page<JoinWaitingResponseDto> resultDtoPage = joinWaitingService.findAllByApplicant_IdAndStatusIn(
                applicantId, pageRequest);

            // then
            assertThat(resultDtoPage).hasSize(2);
            assertThat(resultDtoPage.getContent()).hasSize(2);
            assertThat(resultDtoPage.getTotalElements()).isEqualTo(2);
            assertThat(resultDtoPage.getContent().get(0).applicantId()).isEqualTo(applicantId);
            assertThat(resultDtoPage.getContent().get(1).applicantId()).isEqualTo(applicantId);
        }

        @Test
        @DisplayName("findAllByApplicant_IdAndStatusIn - 사용자 없음 예외")
        void findAllByApplicant_IdAndStatusIn_userNotFound_throws() {
            // given
            Long nonExistApplicantId = 100L;
            PageRequest pageRequest = PageRequest.of(PAGE, SIZE);

            when(profileRepository.findById(nonExistApplicantId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> joinWaitingService.findAllByApplicant_IdAndStatusIn(
                    nonExistApplicantId, pageRequest))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("findAllByApplicant_IdAndStatusIn - 빈 결과 반환")
        void findAllByApplicant_IdAndStatusIn_emptyResult() {
            // given
            Long applicantId = applicant.getId();
            PageRequest pageRequest = PageRequest.of(PAGE, SIZE);
            Page<JoinWaiting> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

            when(profileRepository.findById(applicantId)).thenReturn(Optional.of(applicant));
            when(joinWaitingRepository.findAllByApplicant_IdAndStatusIn(applicantId, targetStatuses,
                pageRequest)).thenReturn(
                emptyPage);

            // when
            Page<JoinWaitingResponseDto> resultDtoPage = joinWaitingService.findAllByApplicant_IdAndStatusIn(
                applicantId, pageRequest);

            // then
            assertThat(resultDtoPage).hasSize(0);
            assertThat(resultDtoPage.getContent()).isEmpty();
            assertThat(resultDtoPage.getTotalElements()).isEqualTo(0);
        }
    }
}
