package com.shootdoori.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DifferentUniversityException;
import com.shootdoori.match.exception.DuplicatedMemberException;
import com.shootdoori.match.exception.LastTeamMemberRemovalNotAllowedException;
import com.shootdoori.match.exception.TeamCapacityExceededException;
import com.shootdoori.match.value.MemberCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("Team 도메인 모델 테스트")
public class TeamTest {

    private Team team;
    private User captain;
    private User newMember;
    private User differentUniversityMember;

    @BeforeEach
    void setUp() {
        captain = User.create(
            "김학생",
            "아마추어",
            "student@example.com",
            "student@kangwon.ac.kr",
            "Abcd1234!",
            "imkim251",
            "골키퍼",
            "강원대학교",
            "컴퓨터공학과",
            "25",
            "축구를 좋아하는 대학생입니다. 골키퍼 포지션을 주로 맡고 있으며, 즐겁게 운동하고 싶습니다!"
        );

        newMember = User.create(
            "손응민",
            "세미프로",
            "student999@gmail.com",
            "student35@kangwon.ac.kr",
            "Abcd1234!",
            "imkim252",
            "풀백",
            "강원대학교",
            "컴퓨터공학과",
            "35",
            "축구 좋아하는 아빠입니다."
        );

        differentUniversityMember = User.create(
            "박서울",
            "프로",
            "seoul@example.com",
            "seoul@seoul.ac.kr",
            "Abcd1234!",
            "imkim253",
            "공격수",
            "서울대학교",
            "체육학과",
            "22",
            "서울대 축구부입니다."
        );

        ReflectionTestUtils.setField(captain, "id", 1L);
        ReflectionTestUtils.setField(newMember, "id", 2L);
        ReflectionTestUtils.setField(differentUniversityMember, "id", 3L);

        team = new Team(
            "강원대 FC",
            captain,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );
        team.recruitMember(captain, TeamMemberRole.LEADER);
    }

    @Nested
    @DisplayName("팀 생성 테스트")
    class TeamCreationTest {

        @Test
        @DisplayName("null 팀명으로 생성 시 예외 발생")
        void createTeam_nullName_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                new Team(
                    null,
                    captain,
                    "강원대학교",
                    TeamType.fromDisplayName("과동아리"),
                    SkillLevel.fromDisplayName("아마추어"),
                    "주 2회 연습합니다."
                ))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("100자 초과 팀명으로 생성 시 예외 발생")
        void createTeam_tooLongName_throwsException() {
            // given
            String longName = "가".repeat(101);

            // when & then
            assertThatThrownBy(() ->
                new Team(
                    longName,
                    captain,
                    "강원대학교",
                    TeamType.fromDisplayName("과동아리"),
                    SkillLevel.fromDisplayName("아마추어"),
                    "주 2회 연습합니다."
                ))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("팀 멤버 모집 테스트")
    class RecruitMemberTest {

        @Test
        @DisplayName("정상적으로 멤버를 모집한다")
        void recruitMember_success() {
            // when
            team.recruitMember(newMember, TeamMemberRole.MEMBER);

            // then
            assertThat(team.getMembers()).hasSize(2);
            assertThat(team.getMemberCount().count()).isEqualTo(2);
            assertThat(team.getMembers().get(1).getUser()).isEqualTo(newMember);
            assertThat(team.getMembers().get(1).getRole()).isEqualTo(TeamMemberRole.MEMBER);
        }

        @Test
        @DisplayName("중복 멤버 예외 테스트")
        void recruitMember_duplicate_throwsException() {
            // given
            team.recruitMember(newMember, TeamMemberRole.MEMBER);

            // when & then
            assertThatThrownBy(
                () ->
                    team.recruitMember(newMember, TeamMemberRole.MEMBER))
                .isInstanceOf(DuplicatedMemberException.class);
        }
    }

    @Nested
    @DisplayName("팀 멤버 제거 테스트")
    class RemoveMemberTest {

        @Test
        @DisplayName("멤버가 2명 이상일 때 정상적으로 제거한다")
        void removeMember_success() {
            // given
            team.recruitMember(newMember, TeamMemberRole.MEMBER);

            // when
            team.removeMember(team.getMembers().get(1));

            // then
            assertThat(team.getMembers()).hasSize(1);
            assertThat(team.getMemberCount().count()).isEqualTo(1);
        }

        @Test
        @DisplayName("마지막 멤버 제거 시 예외 발생")
        void removeMember_lastMember_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                team.removeMember(team.getMembers().get(0)))
                .isInstanceOf(LastTeamMemberRemovalNotAllowedException.class);
        }
    }

    @Nested
    @DisplayName("대학교 검증 테스트")
    class ValidateUniversityTest {

        @Test
        @DisplayName("같은 대학교 사용자는 검증을 통과한다")
        void validateSameUniversity_success() {
            // when & then
            team.validateSameUniversity(newMember);
        }

        @Test
        @DisplayName("다른 대학교 사용자 예외 테스트")
        void validateSameUniversity_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                team.validateSameUniversity(differentUniversityMember))
                .isInstanceOf(DifferentUniversityException.class);
        }
    }

    @Nested
    @DisplayName("팀원 정원 검증 테스트")
    class ValidateCapacityTest {

        @Test
        @DisplayName("정원 여유가 있으면 검증을 통과한다")
        void validateCanAcceptNewMember_success() {

            // when & then
            team.validateCanAcceptNewMember();
        }

        @Test
        @DisplayName("정원 여유가 없을 때 예외 테스트")
        void validateCanAcceptNewMember_throwsException() {

            // given
            ReflectionTestUtils.setField(team, "memberCount", MemberCount.of(100));

            // when & then
            assertThatThrownBy(() ->
                team.validateCanAcceptNewMember())
                .isInstanceOf(TeamCapacityExceededException.class);
        }
    }

    @Nested
    @DisplayName("팀 정보 변경 테스트")
    class ChangeTeamInfoTest {

        @Test
        @DisplayName("유효한 정보로 팀 정보를 변경하면 검증을 통과한다")
        void changeTeamInfo_success() {

            // when
            team.changeTeamInfo(
                "감자 FC",
                "한림대학교",
                "프로",
                "저희가 짱입니다."
            );

            // then
            assertThat(team.getTeamName().name()).isEqualTo("감자 FC");
            assertThat(team.getUniversity().name()).isEqualTo("한림대학교");
            assertThat(team.getSkillLevel()).isEqualTo(SkillLevel.PRO);
        }

        @Test
        @DisplayName("잘못된 SkillLevel로 변경 시 예외 발생")
        void changeTeamInfo_throwsException() {

            // when & then
            assertThatThrownBy(() ->
                team.changeTeamInfo(
                    "감자 FC",
                    "한림대학교",
                    "잘못된 SkillLevel",
                    "저희가 짱입니다."
                ))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
