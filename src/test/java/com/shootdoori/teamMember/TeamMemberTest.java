package com.shootdoori.teamMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.TeamSkillLevel;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DifferentException;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.NoPermissionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("TeamMember 도메인 모델 테스트")
public class TeamMemberTest {

    private Team team;
    private User captain;
    private User user;
    private User anotherUser;

    private TeamMember teamMember; // 테스트 대상 멤버 (기본: MEMBER)

    @BeforeEach
    void setUp() {
        captain = User.create(
            "김학생",
            "아마추어",
            "student@example.com",
            "student@kangwon.ac.kr",
            "Abcd1234!",
            "010-1234-5678",
            "골키퍼",
            "강원대학교",
            "컴퓨터공학과",
            "25",
            "축구를 좋아하는 대학생입니다. 골키퍼 포지션을 주로 맡고 있으며, 즐겁게 운동하고 싶습니다!"
        );

        user = User.create(
            "손응민",
            "세미프로",
            "member@example.com",
            "member@kangwon.ac.kr",
            "Abcd1234!",
            "010-0000-0000",
            "풀백",
            "강원대학교",
            "컴퓨터공학과",
            "23",
            "축구 좋아하는 회원입니다."
        );

        anotherUser = User.create(
            "박데사르",
            "프로",
            "bakinthekorea@example.com",
            "bakinthekorea@kangwon.ac.kr",
            "Abcd1234!",
            "010-0000-0000",
            "풀백",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "반데사르 팬입니다."
        );

        ReflectionTestUtils.setField(captain, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 2L);
        ReflectionTestUtils.setField(anotherUser, "id", 3L);

        team = new Team(
            "강원대 FC",
            captain,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            TeamSkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );

        team.recruitMember(captain, TeamMemberRole.LEADER);
        team.recruitMember(user, TeamMemberRole.MEMBER);
        teamMember = team.getMembers().get(1);
    }

    @Nested
    @DisplayName("생성 & 기본값 테스트")
    class CreationDefaultsTest {

        @Test
        @DisplayName("역할이 null로 생성되면 기본값은 MEMBER이다")
        void defaultRole_isMember_whenNullProvided() {
            // when
            TeamMember newMember = new TeamMember(team, user, null);

            // then
            assertThat(newMember.getRole()).isEqualTo(TeamMemberRole.MEMBER);
        }
    }

    @Nested
    @DisplayName("역할 확인 테스트")
    class RoleCheckTest {

        @Test
        @DisplayName("회장 역할 확인")
        void isCaptain_returnsTrue_whenLeader() {
            // given
            TeamMember leader = new TeamMember(team, captain, TeamMemberRole.LEADER);

            // when
            boolean isCaptain = leader.isCaptain();

            // then
            assertThat(isCaptain).isEqualTo(true);
        }

        @Test
        @DisplayName("부회장 역할 확인")
        void isViceCaptain_returnsTrue_whenViceLeader() {
            // given
            TeamMember viceLeader = new TeamMember(team, captain, TeamMemberRole.VICE_LEADER);

            // when
            boolean isViceCaptain = viceLeader.isViceCaptain();

            // then
            assertThat(isViceCaptain).isEqualTo(true);
        }

        @Test
        @DisplayName("일반 멤버는 회장이 아니다")
        void isCaptain_returnsFalse_whenMember() {
            // given
            TeamMember basicMember = new TeamMember(team, captain, TeamMemberRole.MEMBER);

            // when
            boolean isCaptain = basicMember.isCaptain();

            // then
            assertThat(isCaptain).isEqualTo(false);
        }

        @Test
        @DisplayName("일반 멤버는 부회장이 아니다")
        void isViceCaptain_returnsFalse_whenMember() {
            // given
            TeamMember basicMember = new TeamMember(team, captain, TeamMemberRole.MEMBER);

            // when
            boolean isViceCaptain = basicMember.isViceCaptain();

            // then
            assertThat(isViceCaptain).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("역할 변경 테스트")
    class ChangeRoleTest {

        @Test
        @DisplayName("이미 회장이 있을 때 회장으로 승격 시 예외가 발생한다")
        void changeRole_toLeader_throws_whenLeaderExists() {
            // when & then
            assertThatThrownBy(() -> teamMember.changeRole(team, TeamMemberRole.LEADER))
                .isInstanceOf(DuplicatedException.class);
        }

        @Test
        @DisplayName("이미 부회장이 있을 때 부회장으로 승격 시 예외가 발생한다")
        void changeRole_toViceLeader_throws_whenViceLeaderExists() {
            // given
            team.recruitMember(anotherUser, TeamMemberRole.VICE_LEADER);

            // when & then
            assertThatThrownBy(() -> teamMember.changeRole(team, TeamMemberRole.VICE_LEADER))
                .isInstanceOf(DuplicatedException.class);
        }
    }

    @Nested
    @DisplayName("가입 승인/거절 권한 검증 테스트")
    class JoinDecisionPermissionTest {

        @Test
        @DisplayName("회장은 가입 결정 권한을 가진다")
        void canMakeJoinDecision_leader_returnsTrue() {
            // given
            TeamMember leader = new TeamMember(team, user, TeamMemberRole.LEADER);

            // when & then
            assertThat(leader.canMakeJoinDecisionFor(team)).isTrue();
        }

        @Test
        @DisplayName("부회장은 가입 결정 권한을 가진다")
        void canMakeJoinDecision_viceLeader_returnsTrue() {
            // given
            TeamMember vice = new TeamMember(team, user, TeamMemberRole.VICE_LEADER);

            // when & then
            assertThat(vice.canMakeJoinDecisionFor(team)).isTrue();
        }

        @Test
        @DisplayName("일반 멤버는 가입 결정 권한이 없어 예외가 발생한다")
        void canMakeJoinDecision_member_throws() {
            // when & then
            assertThatThrownBy(() -> teamMember.canMakeJoinDecisionFor(team))
                .isInstanceOf(NoPermissionException.class);
        }
    }

    @Nested
    @DisplayName("위임 테스트")
    class DelegationTest {

        @Test
        @DisplayName("회장이 다른 멤버에게 회장을 위임한다")
        void delegateLeadership_success() {
            // given
            TeamMember leader = new TeamMember(team, captain, TeamMemberRole.LEADER);
            TeamMember targetMember = new TeamMember(team, user, TeamMemberRole.MEMBER);

            // when
            leader.delegateLeadership(targetMember);

            // then
            assertThat(leader.getRole()).isEqualTo(TeamMemberRole.MEMBER);
            assertThat(targetMember.getRole()).isEqualTo(TeamMemberRole.LEADER);
        }

        @Test
        @DisplayName("부회장이 다른 멤버에게 부회장을 위임한다")
        void delegateViceLeadership_success() {
            // given
            TeamMember viceLeader = new TeamMember(team, user, TeamMemberRole.VICE_LEADER);
            TeamMember targetMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);

            // when
            viceLeader.delegateViceLeadership(targetMember);

            // then
            assertThat(viceLeader.getRole()).isEqualTo(TeamMemberRole.MEMBER);
            assertThat(targetMember.getRole()).isEqualTo(TeamMemberRole.VICE_LEADER);
        }

        @Test
        @DisplayName("일반 멤버가 회장 위임 시도 시 예외 발생")
        void delegateLeadership_throws_whenNotLeader() {
            // given
            TeamMember basicMember = new TeamMember(team, user, TeamMemberRole.MEMBER);
            TeamMember targetMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() ->
                basicMember.delegateLeadership(targetMember))
                .isInstanceOf(NoPermissionException.class);
        }

        @Test
        @DisplayName("일반 멤버가 부회장 위임 시도 시 예외 발생")
        void delegateViceLeadership_throws_whenNotViceLeader() {
            // given
            TeamMember basicMember = new TeamMember(team, user, TeamMemberRole.MEMBER);
            TeamMember targetMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() ->
                basicMember.delegateViceLeadership(targetMember))
                .isInstanceOf(NoPermissionException.class);
        }

        @Test
        @DisplayName("자기 자신에게 회장 위임 시도 시 예외 발생")
        void delegateLeadership_throws_whenSelfDelegation() {
            // given
            TeamMember leader = new TeamMember(team, captain, TeamMemberRole.LEADER);

            // when & then
            assertThatThrownBy(() ->
                leader.delegateLeadership(leader))
                .isInstanceOf(DuplicatedException.class);
        }

        @Test
        @DisplayName("자기 자신에게 부회장 위임 시도 시 예외 발생")
        void delegateViceLeadership_throws_whenSelfDelegation() {
            // given
            TeamMember viceLeader = new TeamMember(team, user, TeamMemberRole.VICE_LEADER);

            // when & then
            assertThatThrownBy(() ->
                viceLeader.delegateViceLeadership(viceLeader))
                .isInstanceOf(DuplicatedException.class);
        }

        @Test
        @DisplayName("다른 팀 멤버에게 회장 위임 시도 시 예외 발생")
        void delegateLeadership_throws_whenDifferentTeam() {
            // given
            Team anotherTeam = createAnotherTeam();
            setDifferentTeamIds(team, anotherTeam);

            TeamMember leader = new TeamMember(team, captain, TeamMemberRole.LEADER);
            TeamMember anotherTeamMember = new TeamMember(anotherTeam, anotherUser,
                TeamMemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() ->
                leader.delegateLeadership(anotherTeamMember))
                .isInstanceOf(DifferentException.class);
        }

        @Test
        @DisplayName("다른 팀 멤버에게 부회장 위임 시도 시 예외 발생")
        void delegateViceLeadership_throws_whenDifferentTeam() {
            // given
            Team anotherTeam = createAnotherTeam();
            setDifferentTeamIds(team, anotherTeam);

            TeamMember viceLeader = new TeamMember(team, user, TeamMemberRole.VICE_LEADER);
            TeamMember anotherTeamMember = new TeamMember(anotherTeam, anotherUser,
                TeamMemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() ->
                viceLeader.delegateViceLeadership(anotherTeamMember))
                .isInstanceOf(DifferentException.class);
        }
    }

    @Nested
    @DisplayName("다른 팀 권한 검증 테스트")
    class DifferentTeamPermissionTest {
        @Test
        @DisplayName("같은 팀에 대한 가입 결정 권한 검증 성공")
        void canMakeJoinDecisionFor_success() {
            // given
            ReflectionTestUtils.setField(team, "teamId", 1L);

            TeamMember leader = new TeamMember(team, captain, TeamMemberRole.LEADER);

            // when
            boolean canMakeJoinDecision = leader.canMakeJoinDecisionFor(team);

            // then
            assertThat(canMakeJoinDecision).isEqualTo(true);
        }

        @Test
        @DisplayName("다른 팀에 대한 가입 결정 권한 검증 시 예외 발생")
        void canMakeJoinDecisionFor_throws_whenDifferentTeam() {
            // given
            Team anotherTeam = createAnotherTeam();
            setDifferentTeamIds(team, anotherTeam);

            TeamMember leader = new TeamMember(team, captain, TeamMemberRole.LEADER);

            // when & then
            assertThatThrownBy(() ->
                leader.canMakeJoinDecisionFor(anotherTeam))
                .isInstanceOf(NoPermissionException.class);
        }
    }

    private Team createAnotherTeam() {
        return new Team(
            "감자빵 FC",
            user,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            TeamSkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );
    }

    private void setDifferentTeamIds(Team team1, Team team2) {
        ReflectionTestUtils.setField(team1, "teamId", 1L);
        ReflectionTestUtils.setField(team2, "teamId", 2L);
    }
}

