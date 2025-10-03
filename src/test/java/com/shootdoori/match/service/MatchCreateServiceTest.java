package com.shootdoori.match.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import com.shootdoori.match.dto.MatchCreateRequestDto;
import com.shootdoori.match.dto.MatchCreateResponseDto;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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
  private Team savedTeam;
  private Venue savedVenue;

  private final Long NON_EXIST_VENUE_ID = 1000000007L;
  private final Long NON_EXIST_USER_ID = 1000000007L;
  private final String MESSAGE = "메세지";

  @BeforeEach
  void setUp() {
    User captain = User.create(
      "선원준",
      "프로",
      "swj@naver.com",
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
      LocalTime.of(10, 0),
      LocalTime.of(12, 0),
      savedVenue.getVenueId(),
      SkillLevel.AMATEUR,
      SkillLevel.PRO,
      false,
      MESSAGE
    );

    // when
    Throwable thrown = catchThrowable(() -> matchCreateService.createMatch(NON_EXIST_USER_ID, dto));

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
      LocalTime.of(10, 0),
      LocalTime.of(12, 0),
      NON_EXIST_VENUE_ID, // 존재하지 않는 venue ID
      SkillLevel.AMATEUR,
      SkillLevel.PRO,
      false,
      MESSAGE
    );

    // when
    Throwable thrown = catchThrowable(() -> matchCreateService.createMatch(savedCaptain.getId(),dto));

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
      LocalTime.of(10, 0),
      LocalTime.of(12, 0),
      savedVenue.getVenueId(),
      SkillLevel.AMATEUR,
      SkillLevel.PRO,
      false,
      MESSAGE
    );

    // when
    MatchCreateResponseDto response = matchCreateService.createMatch(savedCaptain.getId(),dto);

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
    assertThat(match.getMatchRequestStatus()).isEqualTo(MatchWaitingStatus.WAITING);
  }
}
