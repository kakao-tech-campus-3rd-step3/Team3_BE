package com.shootdoori.match.service;

import com.shootdoori.match.dto.*;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.match.waiting.MatchWaitingSkillLevel;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.entity.team.*;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Transactional
class MatchRequestServiceTest {

    @Autowired
    private MatchRequestService matchRequestService;

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @Autowired
    private MatchWaitingRepository matchWaitingRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private User requestTeamCaptain1;
    private User requestTeamCaptain2;
    private User targetTeamCaptain;
    private Team requestTeam1;
    private Team requestTeam2;
    private Team targetTeam;
    private Venue savedVenue;
    private MatchWaiting savedWaiting;

    private final Long NON_EXIST_WAITING_ID = 1000000007L;
    private final Long NON_EXIST_REQUEST_ID = 1000000007L;
    private final Long NON_EXIST_USER_ID = 1000000007L;
    private final String REQUEST_MESSAGE = "요청 메세지";
    private final String REQUEST_MESSAGE_1 = "요청 메세지 1";
    private final String REQUEST_MESSAGE_2 = "요청 메세지 2";

    public static final LocalTime TEN_OCLOCK = LocalTime.of(10, 0);

    @BeforeEach
    void setUp() {
        User matchCreateTeamCaptain = User.create(
            "매치 생성 역할을 맡는 팀의 리더",
            "프로",
            "swj@naver.com",
            "swj@kangwon.ac.kr",
            "12345678",
            "010-1234-5678",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "매치 생성 역할을 맡는 팀의 리더"
        );
        targetTeamCaptain = profileRepository.save(matchCreateTeamCaptain);

        User matchRequestTeamCaptain1 = User.create(
            "매치 신청 역할을 맡는 팀의 리더 1",
            "프로",
            "swj2@naver.com",
            "swj2@kangwon.ac.kr",
            "12345678",
            "010-1234-5679",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "매치 신청 역할을 맡는 팀의 리더 1"
        );
        requestTeamCaptain1 = profileRepository.save(matchRequestTeamCaptain1);

        User matchRequestTeamCaptain2 = User.create(
            "매치 신청 역할을 맡는 팀의 리더 2",
            "프로",
            "swj3@naver.com",
            "swj3@kangwon.ac.kr",
            "12345678",
            "010-1234-5680",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "매치 신청 역할을 맡는 팀의 리더 2"
        );
        requestTeamCaptain2 = profileRepository.save(matchRequestTeamCaptain2);

        Team matchCreateTeam = new Team(
            "매치 생성후 Waiting 중인 팀",
            targetTeamCaptain,
            "강원대학교",
            TeamType.OTHER,
            TeamSkillLevel.AMATEUR,
            "매치 생성후 Waiting 중인 팀"
        );
        targetTeam = teamRepository.save(matchCreateTeam);

        Team matchRequestTeam1 = new Team(
            "매치에 신청하는 1번째 팀",
            requestTeamCaptain1,
            "강원대학교",
            TeamType.OTHER,
            TeamSkillLevel.AMATEUR,
            "매치에 신청하는 1번째 팀"
        );
        requestTeam1 = teamRepository.save(matchRequestTeam1);

        Team matchRequestTeam2 = new Team(
            "매치에 신청하는 2번째 팀",
            requestTeamCaptain2,
            "강원대학교",
            TeamType.OTHER,
            TeamSkillLevel.AMATEUR,
            "매치에 신청하는 2번째 팀"
        );
        requestTeam2 = teamRepository.save(matchRequestTeam2);

        TeamMember requestTeamCaptain1Member = new TeamMember(requestTeam1, requestTeamCaptain1, TeamMemberRole.LEADER);
        teamMemberRepository.save(requestTeamCaptain1Member);

        TeamMember requestTeamCaptain2Member = new TeamMember(requestTeam2, requestTeamCaptain2, TeamMemberRole.LEADER);
        teamMemberRepository.save(requestTeamCaptain2Member);

        TeamMember targetTeamCaptainMember = new TeamMember(targetTeam, targetTeamCaptain, TeamMemberRole.LEADER);
        teamMemberRepository.save(targetTeamCaptainMember);

        Venue venue = new Venue(
            "강원대 대운동장",
            "춘천",
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780),
            "033-123-4567",
            "강원대",
            0L
        );
        savedVenue = venueRepository.save(venue);

        savedWaiting = matchWaitingRepository.save(new MatchWaiting(
            targetTeam, LocalDate.now(),
            LocalTime.of(10, 0), LocalTime.of(12, 0),
            savedVenue,
            MatchWaitingSkillLevel.AMATEUR, MatchWaitingSkillLevel.PRO,
            false, "연습 경기",
            MatchWaitingStatus.WAITING,
            LocalDateTime.now().plusDays(1)
        ));
    }

    // ------------------- getWaitingMatches 테스트 -------------------

    @Nested
    @DisplayName("오늘 10~12시 진행 예정인 매치 대기가 있다고 했을 때, 신청자 입장에서 이를 검색시 각 날짜/시간/속한 팀에 따른 검색 결과의 개수 확인")
    class GetWaitingMatchesTest {

        @ParameterizedTest(name = "[{index}] 로그인 팀: {0}, 요청 날짜: {1}, 요청 시간: {2} → 예상 결과 개수: {3}")
        @CsvSource({
            "requestTeamCaptain1, today, 12:00, 0",  // 오늘, 12시 이후 조회 → 결과 없음
            "requestTeamCaptain1, today, 10:00, 1",  // 오늘, 10시 이후 조회 → 결과 있음
            "targetTeamCaptain, today, 10:00, 0",    // 매치 생성 주체가 나 자신인 경우로 조회 → 결과 없음
            "requestTeamCaptain1, tomorrow, 10:00, 0" // 다른 날짜(내일) 조회 → 결과 없음
        })
        @Transactional
        void boundaryDateTimeTest(String loginTeam, String dateStr, String timeStr, int expectedSize) {
            LocalDate date = switch (dateStr) {
                case "today" -> LocalDate.now();
                case "tomorrow" -> LocalDate.now().plusDays(1);
                default -> throw new IllegalArgumentException("Unknown date: " + dateStr);
            };

            LocalTime time = LocalTime.parse(timeStr);

            Long loginUserId = switch (loginTeam) {
                case "requestTeamCaptain1" -> requestTeamCaptain1.getId();
                case "targetTeamCaptain" -> targetTeamCaptain.getId();
                default -> throw new IllegalArgumentException("Unknown team: " + loginTeam);
            };

            MatchWaitingRequestDto request = new MatchWaitingRequestDto(date, time);
            Slice<MatchWaitingResponseDto> result = matchRequestService.getWaitingMatches(loginUserId, request, PageRequest.of(0, 10));

            assertThat(result).hasSize(expectedSize);

            if (expectedSize > 0) {
                assertThat(result.getContent().get(0).waitingId()).isEqualTo(savedWaiting.getWaitingId());
            }
        }
    }

    // ------------------- requestToMatch 테스트 -------------------

    @Test
    @DisplayName("매치 요청 생성 성공(PENDING 상태)")
    void requestToMatch_success() {

        MatchRequestRequestDto dto = new MatchRequestRequestDto(
            REQUEST_MESSAGE
        );

        MatchRequestResponseDto response = matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(), dto);

        assertThat(response).isNotNull();
        assertThat(response.requestTeamId()).isEqualTo(requestTeam1.getTeamId());
        assertThat(response.targetTeamId()).isEqualTo(targetTeam.getTeamId());
        assertThat(response.requestMessage()).isEqualTo(REQUEST_MESSAGE);
        assertThat(response.status()).isEqualTo(MatchRequestStatus.PENDING);

        Optional<MatchRequest> found = matchRequestRepository.findById(response.requestId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(MatchRequestStatus.PENDING);
    }

    @Test
    @DisplayName("존재하지 않는 MatchWaiting으로 요청 시 NotFoundException")
    void requestToMatch_waitingNotFound() {
        MatchRequestRequestDto dto = new MatchRequestRequestDto(REQUEST_MESSAGE);
        Throwable thrown = catchThrowable(() ->
            matchRequestService.requestToMatch(targetTeamCaptain.getId(), NON_EXIST_WAITING_ID, dto)
        );

        assertThat(thrown).isInstanceOf(NotFoundException.class)
            .hasMessageContaining(NON_EXIST_WAITING_ID.toString());
    }

    @Test
    @DisplayName("이미 신청한 매치에 다시 요청 시 DuplicatedException 발생")
    void requestToMatch_duplicateRequest() {
        MatchRequestRequestDto firstRequest = new MatchRequestRequestDto(REQUEST_MESSAGE);
        matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(), firstRequest);

        MatchRequestRequestDto againRequest = new MatchRequestRequestDto(REQUEST_MESSAGE);
        Throwable thrown = catchThrowable(() ->
            matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(), againRequest)
        );

        assertThat(thrown).isInstanceOf(DuplicatedException.class)
            .hasMessageContaining(ErrorCode.ALREADY_MATCH_REQUEST.getMessage());
    }

    @Test
    @DisplayName("이미 신청한 매치에 취소 후 다시 요청 시 문제 발생하지 않음")
    void requestToMatch_cancel_and_re_request_ok() {
        MatchRequestRequestDto firstRequest = new MatchRequestRequestDto(REQUEST_MESSAGE);
        MatchRequestResponseDto savedFirstRequest = matchRequestService.requestToMatch(
            requestTeamCaptain1.getId(),
            savedWaiting.getWaitingId(),
            firstRequest
        );

        MatchRequestResponseDto canceled = matchRequestService.cancelMatchRequest(
            requestTeamCaptain1.getId(),
            savedFirstRequest.requestId()
        );
        assertThat(canceled.status()).isEqualTo(MatchRequestStatus.CANCELED);

        MatchRequestRequestDto againRequest = new MatchRequestRequestDto(REQUEST_MESSAGE);
        MatchRequestResponseDto savedAgainRequest = matchRequestService.requestToMatch(
            requestTeamCaptain1.getId(),
            savedWaiting.getWaitingId(),
            againRequest
        );

        assertThat(savedAgainRequest).isNotNull();
        assertThat(savedAgainRequest.requestTeamId()).isEqualTo(requestTeam1.getTeamId());
        assertThat(savedAgainRequest.targetTeamId()).isEqualTo(targetTeam.getTeamId());
        assertThat(savedAgainRequest.requestMessage()).isEqualTo(REQUEST_MESSAGE);
        assertThat(savedAgainRequest.status()).isEqualTo(MatchRequestStatus.PENDING);

        Optional<MatchRequest> found = matchRequestRepository.findById(savedAgainRequest.requestId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(MatchRequestStatus.PENDING);
    }

    // ------------------- cancelMatchRequest 테스트 -------------------

    @Test
    @DisplayName("존재하지 않는 MatchRequest ID로 취소 시 NotFoundException 발생")
    void cancelMatchRequest_notFound() {
        Throwable thrown = catchThrowable(() ->
            matchRequestService.cancelMatchRequest(requestTeamCaptain1.getId(), NON_EXIST_REQUEST_ID)
        );

        assertThat(thrown)
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(NON_EXIST_REQUEST_ID.toString());
    }

    @Test
    @DisplayName("정상적으로 MatchRequest 취소 시 상태가 CANCELED로 변경")
    void cancelMatchRequest_success() {
        // given
        MatchRequestRequestDto dto = new MatchRequestRequestDto(REQUEST_MESSAGE);
        MatchRequestResponseDto savedRequest = matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(), dto);

        // when
        MatchRequestResponseDto canceled = matchRequestService.cancelMatchRequest(requestTeamCaptain1.getId(), savedRequest.requestId());

        // then
        assertThat(canceled).isNotNull();
        assertThat(canceled.requestId()).isEqualTo(savedRequest.requestId());
        assertThat(canceled.status()).isEqualTo(MatchRequestStatus.CANCELED);

        // DB 상태 확인
        Optional<MatchRequest> found = matchRequestRepository.findById(savedRequest.requestId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(MatchRequestStatus.CANCELED);
    }

    // ------------------- getReceivedPendingRequests 테스트 -------------------

    @Test
    @DisplayName("존재하지 않는 사용자 ID (즉, 해당 소속 팀이 없는 경우) 로 요청 시 NotFoundException")
    void getReceivedPendingRequests_teamNotFound() {
        Throwable thrown = catchThrowable(() ->
            matchRequestService.getReceivedPendingRequests(NON_EXIST_USER_ID, Pageable.unpaged())
        );

        assertThat(thrown)
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("정상적으로 받은 요청 조회")
    void getReceivedPendingRequests_success() {
        // given: 매치 요청 생성
        MatchRequestRequestDto dto1 = new MatchRequestRequestDto(REQUEST_MESSAGE_1);
        MatchRequestRequestDto dto2 = new MatchRequestRequestDto(REQUEST_MESSAGE_2);

        matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(), dto1);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        matchRequestService.requestToMatch(requestTeamCaptain2.getId(), savedWaiting.getWaitingId(), dto2);

        Pageable pageable = Pageable.ofSize(10);

        // when
        Slice<MatchRequestResponseDto> slice = matchRequestService.getReceivedPendingRequests(targetTeamCaptain.getId(), pageable);

        // then
        assertThat(slice.getContent()).hasSize(2);
        assertThat(slice.getContent())
            .extracting("requestMessage")
            .containsExactly(REQUEST_MESSAGE_1, REQUEST_MESSAGE_2);
        assertThat(slice.getContent())
            .extracting("status")
            .allMatch(status -> status.equals(MatchRequestStatus.PENDING));
    }

    // ------------------- acceptRequest 테스트 -------------------

    @Test
    @DisplayName("acceptRequest 정상 동작 테스트 - team1 수락 시 ACCEPTED, team2 자동 REJECTED, Match 생성 및 상태 MATCHED, MatchWaiting의 경우도 MATCHED")
    void acceptRequest_success() {
        // given: 두 팀이 waiting에 요청
        MatchRequestResponseDto savedRequest1 = matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(),
            new MatchRequestRequestDto(REQUEST_MESSAGE_1));

        MatchRequestResponseDto savedRequest2 = matchRequestService.requestToMatch(requestTeamCaptain2.getId(), savedWaiting.getWaitingId(),
            new MatchRequestRequestDto(REQUEST_MESSAGE_2));

        // when: team1 요청 수락
        MatchConfirmedResponseDto confirmed = matchRequestService.acceptRequest(targetTeamCaptain.getId(), savedRequest1.requestId());

        // then: DB에서 상태 확인
        MatchRequest updatedRequest1 = matchRequestRepository.findById(savedRequest1.requestId()).orElseThrow();
        MatchRequest updatedRequest2 = matchRequestRepository.findById(savedRequest2.requestId()).orElseThrow();
        MatchWaiting updatedWaiting = matchWaitingRepository.findById(savedWaiting.getWaitingId()).orElseThrow();
        Match match = matchRepository.findById(confirmed.matchId()).orElseThrow();

        // 1) team1 요청 ACCEPTED
        assertThat(updatedRequest1.getStatus()).isEqualTo(MatchRequestStatus.ACCEPTED);

        // 2) team2 요청 자동 REJECTED
        assertThat(updatedRequest2.getStatus()).isEqualTo(MatchRequestStatus.REJECTED);

        // 3) waiting 상태 MATCHED
        assertThat(updatedWaiting.getMatchWaitingStatus()).isEqualTo(MatchWaitingStatus.MATCHED);

        // 4) Match 생성 확인
        assertThat(match.getTeam1().getTeamId()).isEqualTo(targetTeam.getTeamId());
        assertThat(match.getTeam2().getTeamId()).isEqualTo(requestTeam1.getTeamId());
        assertThat(match.getStatus()).isEqualTo(MatchStatus.FINISHED); // FE 연동 위한 잠정 변경 MATCHED->FINISHED
        assertThat(match.getMatchDate()).isEqualTo(savedWaiting.getPreferredDate());
        assertThat(match.getMatchTime()).isEqualTo(savedWaiting.getPreferredTimeStart());
        assertThat(match.getVenue().getVenueId()).isEqualTo(savedWaiting.getPreferredVenue().getVenueId());
    }

    @Test
    @DisplayName("MatchRequest 거절 시 상태가 REJECTED로 변경")
    void rejectRequest_success() {
        MatchRequestRequestDto dto = new MatchRequestRequestDto(REQUEST_MESSAGE);
        MatchRequestResponseDto savedRequest = matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(), dto);

        MatchRequestResponseDto rejected = matchRequestService.rejectRequest(targetTeamCaptain.getId(), savedRequest.requestId());

        assertThat(rejected).isNotNull();
        assertThat(rejected.requestId()).isEqualTo(savedRequest.requestId());
        assertThat(rejected.status()).isEqualTo(MatchRequestStatus.REJECTED);

        Optional<MatchRequest> found = matchRequestRepository.findById(savedRequest.requestId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(MatchRequestStatus.REJECTED);
    }

    // ------------------- getSentRequestsByMyTeam 테스트 -------------------

    @Test
    @DisplayName("내가 속한 팀이 보낸 매치 요청이 있으면 정상 조회")
    void getSentRequestsByMyTeam_hasRequests() {
        matchRequestService.requestToMatch(requestTeamCaptain1.getId(), savedWaiting.getWaitingId(), new MatchRequestRequestDto(REQUEST_MESSAGE_1));

        // when
        Slice<MatchRequestHistoryResponseDto> result =
            matchRequestService.getSentRequestsByMyTeam(requestTeamCaptain1.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent())
            .extracting("requestMessage")
            .containsExactly(REQUEST_MESSAGE_1);
        assertThat(result.getContent())
            .extracting("status")
            .allMatch(status -> status.equals(MatchRequestStatus.PENDING));
    }
}
