//package com.shootdoori.joinWaiting;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//import com.shootdoori.match.MatchApplication;
//import com.shootdoori.match.dto.JoinWaitingApproveRequestDto;
//import com.shootdoori.match.dto.JoinWaitingCancelRequestDto;
//import com.shootdoori.match.dto.JoinWaitingRejectRequestDto;
//import com.shootdoori.match.dto.JoinWaitingRequestDto;
//import com.shootdoori.match.dto.JoinWaitingResponseDto;
//import com.shootdoori.match.entity.team.join.JoinWaitingStatus;
//import com.shootdoori.match.entity.SkillLevel;
//import com.shootdoori.match.entity.team.Team;
//import com.shootdoori.match.entity.team.TeamMember;
//import com.shootdoori.match.entity.team.TeamMemberRole;
//import com.shootdoori.match.entity.team.TeamType;
//import com.shootdoori.match.entity.user.User;
//import com.shootdoori.match.exception.common.ErrorCode;
//import com.shootdoori.match.exception.common.NotFoundException;
//import com.shootdoori.match.repository.JoinWaitingRepository;
//import com.shootdoori.match.repository.ProfileRepository;
//import com.shootdoori.match.repository.TeamMemberRepository;
//import com.shootdoori.match.repository.TeamRepository;
//import java.util.UUID;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestClient;
//
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MatchApplication.class)
//public class JoinWaitingE2ETest {
//
//    @LocalServerPort
//    private int port;
//
//    private RestClient restClient;
//    private String baseUrl;
//
//    @Autowired
//    ProfileRepository profileRepository;
//
//    @Autowired
//    TeamRepository teamRepository;
//
//    @Autowired
//    TeamMemberRepository teamMemberRepository;
//
//    @Autowired
//    JoinWaitingRepository joinWaitingRepository;
//
//    private Team testTeam;
//    private User teamLeader;
//    private User applicant;
//    private Long teamId;
//    private Long applicantId;
//    private Long leaderUserId;
//    private Long leaderMemberId;
//
//    @BeforeEach
//    void setUp() {
//        baseUrl = "http://localhost:" + port;
//
//        restClient = RestClient.builder()
//            .baseUrl(baseUrl)
//            .build();
//
//        setupTestData();
//    }
//
//    private void setupTestData() {
//        joinWaitingRepository.deleteAll();
//        teamMemberRepository.deleteAll();
//        teamRepository.deleteAll();
//        profileRepository.deleteAll();
//
//        teamLeader = User.create(
//            "팀리더",
//            "세미프로",
//            uniqueEmail("leader", "example.com"),
//            uniqueEmail("leader", "kangwon.ac.kr"),
//            "Abcd1234!",
//            "010-1111-1111",
//            "미드필더",
//            "강원대학교",
//            "체육학과",
//            "25",
//            "팀을 이끌어가는 리더입니다."
//        );
//        profileRepository.save(teamLeader);
//        leaderUserId = teamLeader.getId();
//
//        testTeam = new Team(
//            "강원대 FC",
//            teamLeader,
//            "강원대학교",
//            TeamType.DEPARTMENT_CLUB,
//            SkillLevel.SEMI_PRO,
//            "주 3회 연습합니다."
//        );
//        teamRepository.save(testTeam);
//        teamId = testTeam.getTeamId();
//
//        testTeam.recruitMember(teamLeader, TeamMemberRole.LEADER);
//        teamRepository.saveAndFlush(testTeam);
//
//        TeamMember leaderMember = teamMemberRepository.findByTeam_TeamIdAndUser_Id(teamId,
//            leaderUserId).orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));
//        leaderMemberId = leaderMember.getId();
//
//        applicant = User.create(
//            "신청자",
//            "아마추어",
//            uniqueEmail("applicant", "example.com"),
//            uniqueEmail("applicant", "kangwon.ac.kr"),
//            "Abcd1234!",
//            "010-2222-2222",
//            "공격수",
//            "강원대학교",
//            "컴퓨터공학과",
//            "22",
//            "축구를 좋아하는 학생입니다."
//        );
//        profileRepository.save(applicant);
//        applicantId = applicant.getId();
//    }
//
//    private String uniqueEmail(String name, String domain) {
//        String token = UUID.randomUUID().toString().replace("-", "");
//        return name + "_" + token + "@" + domain;
//    }
//
//    @Nested
//    @DisplayName("팀 가입 신청 생성")
//    class CreateJoinWaitingTest {
//
//        @Test
//        @DisplayName("정상적인 팀 가입 신청을 생성한다.")
//        void createJoinWaiting_Success() {
//            // given
//            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(
//                "열심히 뛰겠습니다!"
//            );
//
//            // when
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting", teamId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            // then
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//            assertThat(response.getBody()).isNotNull();
//            assertThat(response.getBody().teamId()).isEqualTo(teamId);
//            assertThat(response.getBody().applicantId()).isEqualTo(applicantId);
//            assertThat(response.getBody().status()).isEqualTo("대기중");
//
//            assertThat(joinWaitingRepository.findAll()).hasSize(1);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 팀으로는 신청할 수 없다.")
//        void createJoinWaiting_TeamNotFound() {
//            // given
//            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(
//                "열심히 뛰겠습니다!"
//            );
//
//            Long noExistTeamId = 100L;
//
//            // when & then
//            assertThatThrownBy(() ->
//                restClient
//                    .post()
//                    .uri("/api/teams/{teamId}/join-waiting", noExistTeamId)
//                    .body(requestDto)
//                    .retrieve()
//                    .toEntity(JoinWaitingResponseDto.class)
//            ).isInstanceOf(HttpClientErrorException.class)
//                .satisfies(exception -> {
//                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
//                    assertThat(httpClientErrorException.getStatusCode()).isEqualTo(
//                        HttpStatus.NOT_FOUND);
//                });
//        }
//
//        @Test
//        @DisplayName("대기중인 신청은 중복으로 생성할 수 없다.")
//        void createJoinWaiting_DuplicateApplication() {
//            // given
//            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(
//                "열심히 뛰겠습니다!"
//            );
//
//            restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting", teamId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            // when & then
//            assertThatThrownBy(() ->
//                restClient
//                    .post()
//                    .uri("/api/teams/{teamId}/join-waiting", teamId)
//                    .body(requestDto)
//                    .retrieve()
//                    .toEntity(JoinWaitingResponseDto.class)
//            ).isInstanceOf(HttpClientErrorException.class)
//                .satisfies(exception -> {
//                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
//                    assertThat(httpClientErrorException.getStatusCode()).isEqualTo(
//                        HttpStatus.CONFLICT);
//                });
//        }
//    }
//
//    @Nested
//    @DisplayName("팀 가입 신청 승인")
//    class ApproveJoinWaitingTest {
//
//        private Long joinWaitingId;
//
//        @BeforeEach
//        void setUpApproveTest() {
//            // 가입 신청 생성
//            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(
//                "열심히 뛰겠습니다!"
//            );
//
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting", teamId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            joinWaitingId = response.getBody().id();
//        }
//
//        @Test
//        @DisplayName("정상적으로 승인한다.")
//        void approveJoinWaiting_Success() {
//            // given
//            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
//                "일반멤버",
//                "정상적으로 승인합니다."
//            );
//
//            // when
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/approve", teamId,
//                    joinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            // then
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(response.getBody()).isNotNull();
//            assertThat(response.getBody().status()).isEqualTo(
//                JoinWaitingStatus.APPROVED.getDisplayName());
//            assertThat(response.getBody().teamId()).isEqualTo(teamId);
//            assertThat(response.getBody().applicantId()).isEqualTo(applicantId);
//
//            assertThat(joinWaitingRepository.findAll()).hasSize(1);
//        }
//
//        @Test
//        @DisplayName("승인자 팀 멤버를 찾을 수 없다.")
//        void approveJoinWaiting_TeamMemberNotFound() {
//            // given
//            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
//                "일반멤버",
//                "승인자의 팀 멤버를 못 찾습니다."
//            );
//
//            Long nonExistTeamId = 100L;
//
//            // when & then
//            assertThatThrownBy(() -> restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/approve", nonExistTeamId,
//                    joinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class)
//            ).isInstanceOf(HttpClientErrorException.class)
//                .satisfies(exception -> {
//                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
//                    assertThat(httpClientErrorException.getStatusCode()).isEqualTo(
//                        HttpStatus.NOT_FOUND);
//                });
//        }
//
//        @Test
//        @DisplayName("대기 중인 가입 요청 신청을 찾을 수 없다.")
//        void approveJoinWaiting_JoinWaitingNotFound() {
//            // given
//            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
//                "일반멤버",
//                "대기 중인 가입 요청 신청을 찾을 수 없다."
//            );
//
//            Long nonExistJoinWaitingId = 100L;
//
//            // when & then
//            assertThatThrownBy(() -> restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/approve", teamId,
//                    nonExistJoinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class)
//            ).isInstanceOf(HttpClientErrorException.class)
//                .satisfies(exception -> {
//                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
//                    assertThat(httpClientErrorException.getStatusCode()).isEqualTo(
//                        HttpStatus.NOT_FOUND);
//                });
//        }
//
//        @Test
//        @DisplayName("이미 해당 팀의 멤버입니다.")
//        void approveJoinWaiting_AlreadyTeamMember() {
//            // given
//            JoinWaitingApproveRequestDto requestDto = new JoinWaitingApproveRequestDto(
//                "일반멤버",
//                "이미 해당 팀의 멤버입니다."
//            );
//
//            restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/approve", teamId,
//                    joinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            // when & then
//            assertThatThrownBy(() -> restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/approve", teamId,
//                    joinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class)
//            ).isInstanceOf(HttpClientErrorException.class)
//                .satisfies(exception -> {
//                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
//                    assertThat(httpClientErrorException.getStatusCode()).isEqualTo(
//                        HttpStatus.CONFLICT);
//                });
//        }
//    }
//
//    @Nested
//    @DisplayName("팀 가입 신청 거부")
//    class RejectJoinWaitingTest {
//
//        private Long joinWaitingId;
//
//        @BeforeEach
//        void setUpRejectTest() {
//            // 가입 신청 생성
//            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(
//                "열심히 뛰겠습니다!"
//            );
//
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting", teamId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            joinWaitingId = response.getBody().id();
//        }
//
//        @Test
//        @DisplayName("정상적으로 거부한다.")
//        void rejectJoinWaiting_Success() {
//            // given
//            JoinWaitingRejectRequestDto requestDto = new JoinWaitingRejectRequestDto(
//                "정상적으로 거부합니다."
//            );
//
//            // when
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/reject", teamId,
//                    joinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            // then
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(response.getBody()).isNotNull();
//            assertThat(response.getBody().status()).isEqualTo(
//                JoinWaitingStatus.REJECTED.getDisplayName());
//            assertThat(response.getBody().teamId()).isEqualTo(teamId);
//            assertThat(response.getBody().applicantId()).isEqualTo(applicantId);
//
//            assertThat(joinWaitingRepository.findAll()).hasSize(1);
//        }
//
//        @Test
//        @DisplayName("승인자 팀 멤버를 찾을 수 없다.")
//        void rejectJoinWaiting_TeamMemberNotFound() {
//            // given
//            Long nonExistLeaderMemberId = 100L;
//
//            JoinWaitingRejectRequestDto requestDto = new JoinWaitingRejectRequestDto(
//                "승인자 팀 멤버를 찾을 수 없다."
//            );
//
//            // when & then
//            assertThatThrownBy(() -> restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/reject", teamId,
//                    joinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class)
//            ).isInstanceOf(HttpClientErrorException.class)
//                .satisfies(exception -> {
//                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
//                    assertThat(httpClientErrorException.getStatusCode()).isEqualTo(
//                        HttpStatus.NOT_FOUND);
//                });
//        }
//
//        @Test
//        @DisplayName("대기 신청을 찾을 수 없다.")
//        void rejectJoinWaiting_JoinWaitingNotFound() {
//            // given
//            JoinWaitingRejectRequestDto requestDto = new JoinWaitingRejectRequestDto(
//                "대기 신청을 찾을 수 없다."
//            );
//
//            Long nonExistJoinWaitingId = 100L;
//
//            // when & then
//            assertThatThrownBy(() -> restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/reject", teamId,
//                    nonExistJoinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class)
//            ).isInstanceOf(HttpClientErrorException.class)
//                .satisfies(exception -> {
//                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
//                    assertThat(httpClientErrorException.getStatusCode()).isEqualTo(
//                        HttpStatus.NOT_FOUND);
//                });
//        }
//    }
//
//    @Nested
//    @DisplayName("팀 가입 신청 취소")
//    class CancelJoinWaitingTest {
//
//        private Long joinWaitingId;
//
//        @BeforeEach
//        void setUpCancelTest() {
//            // 가입 신청 생성
//            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(
//                "열심히 뛰겠습니다!"
//            );
//
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting", teamId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            joinWaitingId = response.getBody().id();
//        }
//
//        @Test
//        @DisplayName("정상적으로 신청을 취소한다.")
//        void cancelJoinWaiting_Success() {
//            // given
//            JoinWaitingCancelRequestDto requestDto = new JoinWaitingCancelRequestDto(
//                "개인 사정으로 취소합니다."
//            );
//
//            // when
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting/{joinWaitingId}/cancel", teamId,
//                    joinWaitingId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            // then
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(response.getBody()).isNotNull();
//            assertThat(response.getBody().status()).isEqualTo(
//                JoinWaitingStatus.CANCELED.getDisplayName());
//            assertThat(response.getBody().teamId()).isEqualTo(teamId);
//            assertThat(response.getBody().applicantId()).isEqualTo(applicantId);
//
//            assertThat(joinWaitingRepository.findAll()).hasSize(1);
//        }
//    }
//
//    @Nested
//    @DisplayName("대기중인 가입 신청 조회")
//    class FindPendingJoinWaitingTest {
//
//        private Long joinWaitingId;
//
//        @BeforeEach
//        void setUpApproveTest() {
//            // 가입 신청 생성
//            JoinWaitingRequestDto requestDto = new JoinWaitingRequestDto(
//                "열심히 뛰겠습니다!"
//            );
//
//            ResponseEntity<JoinWaitingResponseDto> response = restClient
//                .post()
//                .uri("/api/teams/{teamId}/join-waiting", teamId)
//                .body(requestDto)
//                .retrieve()
//                .toEntity(JoinWaitingResponseDto.class);
//
//            joinWaitingId = response.getBody().id();
//        }
//
//        @Test
//        @DisplayName("대기중인 신청 목록을 조회한다.")
//        void findPendingJoinWaiting_Success() {
//            // when
//            ResponseEntity<String> response = restClient
//                .get()
//                .uri("/api/teams/{teamId}/join-waiting", teamId)
//                .retrieve()
//                .toEntity(String.class);
//
//            // then
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//            String responseBody = response.getBody();
//
//            assertThat(responseBody).contains("\"id\":" + joinWaitingId);
//            assertThat(responseBody).contains("\"status\":\"대기중\"");
//            assertThat(responseBody).contains("\"applicantId\":" + applicantId);
//            assertThat(responseBody).contains("\"teamId\":" + teamId);
//
//            assertThat(responseBody).contains("\"totalElements\":1");
//            assertThat(responseBody).contains("\"size\":10");
//            assertThat(responseBody).contains("\"number\":0");
//        }
//
//        @Test
//        @DisplayName("사용자별로 신청 목록을 조회한다.")
//        void findByApplicantJoinWaiting_Success() {
//            // when
//            ResponseEntity<String> response = restClient
//                .get()
//                .uri("/api/users/{userId}/join-waiting", applicantId)
//                .retrieve()
//                .toEntity(String.class);
//
//            // then
//            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//            String responseBody = response.getBody();
//
//            assertThat(responseBody).contains("\"id\":" + joinWaitingId);
//            assertThat(responseBody).contains("\"status\":\"대기중\"");
//            assertThat(responseBody).contains("\"applicantId\":" + applicantId);
//            assertThat(responseBody).contains("\"teamId\":" + teamId);
//
//            assertThat(responseBody).contains("\"totalElements\":1");
//            assertThat(responseBody).contains("\"size\":10");
//            assertThat(responseBody).contains("\"number\":0");
//        }
//    }
//}