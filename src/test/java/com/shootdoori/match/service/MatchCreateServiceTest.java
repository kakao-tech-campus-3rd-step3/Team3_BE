package com.shootdoori.match.service;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.dto.MatchWaitingCancelResponseDto;
import com.shootdoori.match.dto.MatchWaitingResponseDto;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.entity.team.*;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.venue.Venue;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@SpringBootTest
@Transactional
class MatchCreateServiceTest {

    @Autowired
    private MatchCreateService matchCreateService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private MatchWaitingRepository matchWaitingRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private User savedCaptain;
    private User savedMember;
    private Team savedTeam;
    private Venue savedVenue;

    private final Long NON_EXIST_VENUE_ID = 1000000007L;
    private final Long NON_EXIST_USER_ID = 1000000007L;
    private final String MESSAGE = "메세지";

    public static final LocalTime TEN_OCLOCK = LocalTime.of(10, 0);
    public static final LocalTime TWELVE_OCLOCK = LocalTime.of(12, 0);


    @BeforeEach
    void setUp() {
        User captain = User.create(
            "선원준",
            "프로",
            "swj@kangwon.ac.kr",
            "12345678",
            "010-1234-5678",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "테스트 캡틴입니다."
        );
        savedCaptain = profileRepository.save(captain);

        User member = User.create(
            "선원준 팀 멤버 1",
            "프로",
            "swj1@kangwon.ac.kr",
            "12345678",
            "010-1234-9999",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "테스트 멤버입니다."
        );
        savedMember = profileRepository.save(member);

        Team team = new Team(
            "팀 선원준",
            savedCaptain,
            "강원대학교",
            TeamType.OTHER,
            SkillLevel.AMATEUR,
            "팀 선원준 설명"
        );
        savedTeam = teamRepository.save(team);

        TeamMember captainMember = new TeamMember(savedTeam, savedCaptain, TeamMemberRole.LEADER);
        teamMemberRepository.save(captainMember);

        TeamMember normalMember = new TeamMember(savedTeam, savedMember, TeamMemberRole.MEMBER);
        teamMemberRepository.save(normalMember);

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
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 요청 시 NotFoundException 발생")
    void createMatch_teamNotFound() {
        // given
        MatchCreateRequestDto dto = new MatchCreateRequestDto(
            LocalDate.now(),
            TEN_OCLOCK,
            TWELVE_OCLOCK,
            savedVenue.getVenueId(),
            SkillLevel.AMATEUR,
            SkillLevel.PRO,
            false,
            MESSAGE
        );

        // when
        Throwable thrown = catchThrowable(
            () -> matchCreateService.createMatch(NON_EXIST_USER_ID, dto));

        // then
        assertThat(thrown)
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 경기장 ID로 요청 시 NotFoundException 발생")
    void createMatch_venueNotFound() {
        // given
        MatchCreateRequestDto dto = new MatchCreateRequestDto(
            LocalDate.now(),
            TEN_OCLOCK,
            TWELVE_OCLOCK,
            NON_EXIST_VENUE_ID, // 존재하지 않는 venue ID
            SkillLevel.AMATEUR,
            SkillLevel.PRO,
            false,
            MESSAGE
        );

        // when
        Throwable thrown = catchThrowable(
            () -> matchCreateService.createMatch(savedCaptain.getId(), dto));

        // then
        assertThat(thrown)
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(NON_EXIST_VENUE_ID.toString());
    }

    @Test
    @DisplayName("정상적으로 매치 생성 (MatchWaiting)")
    void createMatch_success() {
        // given
        MatchCreateRequestDto dto = new MatchCreateRequestDto(
            LocalDate.now(),
            TEN_OCLOCK,
            TWELVE_OCLOCK,
            savedVenue.getVenueId(),
            SkillLevel.AMATEUR,
            SkillLevel.PRO,
            false,
            MESSAGE
        );

        // when
        MatchCreateResponseDto response = matchCreateService.createMatch(savedCaptain.getId(), dto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.teamId()).isEqualTo(savedTeam.getTeamId());

        Optional<MatchWaiting> found = matchWaitingRepository.findById(response.waitingId());
        assertThat(found).isPresent();

        MatchWaiting match = found.get();
        assertThat(match.getTeam().getTeamId()).isEqualTo(savedTeam.getTeamId());
        assertThat(match.getPreferredVenue().getVenueId()).isEqualTo(savedVenue.getVenueId());
        assertThat(match.getSkillLevelMin()).isEqualTo(SkillLevel.AMATEUR);
        assertThat(match.getSkillLevelMax()).isEqualTo(SkillLevel.PRO);
        assertThat(match.getUniversityOnly()).isFalse();
        assertThat(match.getMessage()).isEqualTo(MESSAGE);
        assertThat(match.getMatchWaitingStatus()).isEqualTo(MatchWaitingStatus.WAITING);
    }

    @Test
    @DisplayName("리더가 매치 대기 취소 시 상태가 CANCELED로 변경됨")
    void cancelMatchWaiting_asLeader_success() {
        // given - 먼저 매치 생성
        MatchCreateRequestDto dto = new MatchCreateRequestDto(
            LocalDate.now(),
            TEN_OCLOCK,
            TWELVE_OCLOCK,
            savedVenue.getVenueId(),
            SkillLevel.AMATEUR,
            SkillLevel.PRO,
            false,
            MESSAGE
        );
        MatchCreateResponseDto created = matchCreateService.createMatch(savedCaptain.getId(), dto);

        // when - 리더가 취소 요청
        MatchWaitingCancelResponseDto canceled = matchCreateService.cancelMatchWaiting(
            savedCaptain.getId(), created.waitingId());

        // then - 상태 변경 확인
        assertThat(canceled).isNotNull();
        assertThat(canceled.teamId()).isEqualTo(savedTeam.getTeamId());
        assertThat(canceled.status()).isEqualTo(MatchWaitingStatus.CANCELED);

        // DB에 실제로 반영되었는지 확인
        MatchWaiting found = matchWaitingRepository.findById(created.waitingId()).orElseThrow();
        assertThat(found.getMatchWaitingStatus()).isEqualTo(MatchWaitingStatus.CANCELED);
    }

    @Test
    @DisplayName("일반 멤버가 매치 대기 취소 시 NoPermissionException 발생")
    void cancelMatchWaiting_asMember_fail() {
        // given - 먼저 매치 생성
        MatchCreateRequestDto dto = new MatchCreateRequestDto(
            LocalDate.now(),
            TEN_OCLOCK,
            TWELVE_OCLOCK,
            savedVenue.getVenueId(),
            SkillLevel.AMATEUR,
            SkillLevel.PRO,
            false,
            MESSAGE
        );
        MatchCreateResponseDto created = matchCreateService.createMatch(savedCaptain.getId(), dto);

        // when / then - 멤버가 취소 시 예외 발생
        assertThatThrownBy(
            () -> matchCreateService.cancelMatchWaiting(savedMember.getId(), created.waitingId()))
            .isInstanceOf(NoPermissionException.class);

        // DB에 상태가 여전히 WAITING인지 확인
        MatchWaiting found = matchWaitingRepository.findById(created.waitingId()).orElseThrow();
        assertThat(found.getMatchWaitingStatus()).isEqualTo(MatchWaitingStatus.WAITING);
    }

    @Test
    @DisplayName("내 팀의 매치 대기 목록 조회 - 최신순 정렬 확인")
    void getMyWaitingMatches_orderByCreatedAtDesc() throws InterruptedException {
        // given - 먼저 생성된 매치 생성
        MatchCreateRequestDto dto1 = new MatchCreateRequestDto(
            LocalDate.now(),
            TEN_OCLOCK,
            TWELVE_OCLOCK,
            savedVenue.getVenueId(),
            SkillLevel.AMATEUR,
            SkillLevel.PRO,
            false,
            "먼저 생성된 매치"
        );
        MatchCreateResponseDto match1 = matchCreateService.createMatch(savedCaptain.getId(), dto1);

        // 잠깐 기다려서 createdAt이 달라지도록 함
        Thread.sleep(100);

        // 나중에 생성된 매치
        MatchCreateRequestDto dto2 = new MatchCreateRequestDto(
            LocalDate.now(),
            TEN_OCLOCK,
            TWELVE_OCLOCK,
            savedVenue.getVenueId(),
            SkillLevel.AMATEUR,
            SkillLevel.PRO,
            false,
            "나중에 생성된 매치"
        );
        MatchCreateResponseDto match2 = matchCreateService.createMatch(savedCaptain.getId(), dto2);

        // when - 내 팀 매치 대기 조회
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Slice<MatchWaitingResponseDto> response = matchCreateService.getMyWaitingMatches(
            savedCaptain.getId(), pageable);

        assertThat(response).isNotEmpty();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).waitingId()).isEqualTo(
            match2.waitingId()); // 최신이 첫 번째
        assertThat(response.getContent().get(1).waitingId()).isEqualTo(
            match1.waitingId()); // 오래된 것이 두 번째
    }
}
