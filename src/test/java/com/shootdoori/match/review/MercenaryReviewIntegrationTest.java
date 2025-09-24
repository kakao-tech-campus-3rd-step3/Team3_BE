package com.shootdoori.match.review;

import com.shootdoori.match.entity.*;
import com.shootdoori.match.repository.MercenaryRecruitmentRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.repository.ProfileRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MercenaryReviewIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MercenaryRecruitmentRepository mercenaryRecruitmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProfileRepository userRepository;

    private Team recruitingTeam;
    private User captain;

    @BeforeEach
    void setUp() {
        captain = createUser("이용병","test1@naver.com" ,"mercenary_captain@hallym.ac.kr", "010-1111-1111");
        userRepository.save(captain);

        recruitingTeam = createTeam("슛돌이 FC", captain);
        teamRepository.save(recruitingTeam);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("용병 모집 게시글 생성 성공 테스트")
    void createMercenaryRecruitment_Success() {
        // Given
        LocalDate matchDate = LocalDate.now().plusDays(5);
        LocalTime matchTime = LocalTime.of(18, 0);
        String message = "함께 승리할 공격수 한 분 급히 모십니다!";
        Position position = Position.CF;
        SkillLevel skillLevel = SkillLevel.SEMI_PRO;

        // When
        MercenaryRecruitment recruitment = MercenaryRecruitment.create(
                recruitingTeam,
                matchDate,
                matchTime,
                message,
                position,
                skillLevel
        );
        MercenaryRecruitment savedRecruitment = mercenaryRecruitmentRepository.save(recruitment);
        em.flush();
        em.clear();

        // Then
        MercenaryRecruitment foundRecruitment = mercenaryRecruitmentRepository.findById(savedRecruitment.getId()).orElse(null);
        assertThat(foundRecruitment).isNotNull();
        assertThat(foundRecruitment.getTeam().getTeamId()).isEqualTo(recruitingTeam.getTeamId());
        assertThat(foundRecruitment.getMatchDate()).isEqualTo(matchDate);
        assertThat(foundRecruitment.getPosition()).isEqualTo(position);
        assertThat(foundRecruitment.getSkillLevel()).isEqualTo(skillLevel);
        assertThat(foundRecruitment.getRecruitmentStatus()).isEqualTo(RecruitmentStatus.RECRUITING);
    }

    @Test
    @DisplayName("경기 시간이 과거일 경우 용병 모집 게시글 생성 실패")
    void createMercenaryRecruitment_Fail_WithPastDateTime() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1); // 과거 날짜
        LocalTime matchTime = LocalTime.of(10, 0);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            MercenaryRecruitment.create(
                    recruitingTeam,
                    pastDate,
                    matchTime,
                    "과거 경기에 대한 모집",
                    Position.GK,
                    SkillLevel.AMATEUR
            );
        });
        assertThat(exception.getMessage()).isEqualTo("경기 시간은 현재 시간 이후여야 합니다.");
    }

    @Test
    @DisplayName("용병 모집 정보 수정 성공 테스트")
    void updateRecruitmentInfo_Success() {
        // Given: 기존 용병 모집 게시글 생성
        MercenaryRecruitment recruitment = MercenaryRecruitment.create(
                recruitingTeam,
                LocalDate.now().plusDays(7),
                LocalTime.of(20, 0),
                "원래 메시지",
                Position.DF,
                SkillLevel.AMATEUR
        );
        mercenaryRecruitmentRepository.saveAndFlush(recruitment);

        // When: 정보 수정
        LocalDate newMatchDate = LocalDate.now().plusDays(8);
        LocalTime newMatchTime = LocalTime.of(21, 0);
        String newMessage = "수정된 메시지: 미드필더 구합니다!";
        Position newPosition = Position.CM;
        SkillLevel newSkillLevel = SkillLevel.SEMI_PRO;

        MercenaryRecruitment foundRecruitment = mercenaryRecruitmentRepository.findById(recruitment.getId()).get();
        foundRecruitment.updateRecruitmentInfo(newMatchDate, newMatchTime, newMessage, newPosition, newSkillLevel);
        em.flush();
        em.clear();

        // Then: 수정된 정보 검증
        MercenaryRecruitment updatedRecruitment = mercenaryRecruitmentRepository.findById(recruitment.getId()).get();
        assertThat(updatedRecruitment.getMatchDate()).isEqualTo(newMatchDate);
        assertThat(updatedRecruitment.getMessage()).isEqualTo(newMessage);
        assertThat(updatedRecruitment.getPosition()).isEqualTo(newPosition);
        assertThat(updatedRecruitment.getSkillLevel()).isEqualTo(newSkillLevel);
    }


    // 테스트 데이터 생성용 메서드

    private User createUser(String name, String email, String universityEmail, String phoneNumber) {
        return User.create(name, "아마추어", email, universityEmail, "encodedPassword", phoneNumber,
                "미드필더", email.contains("hallym") ? "한림대학교" : "강원대학교", "컴퓨터공학과", "23", "테스트용 유저입니다.");
    }

    private Team createTeam(String name, User captain) {
        return new Team(name, captain, captain.getUniversity().name(), TeamType.OTHER, SkillLevel.AMATEUR, "용병 모집 테스트용 팀입니다.");
    }
}
