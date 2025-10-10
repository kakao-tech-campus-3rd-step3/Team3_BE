package com.shootdoori.teamMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.TeamSkillLevel;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
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
    private User member;
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

        member = User.create(
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

        ReflectionTestUtils.setField(captain, "id", 1L);
        ReflectionTestUtils.setField(member, "id", 2L);

        team = new Team(
            "강원대 FC",
            captain,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            TeamSkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );

        team.recruitMember(captain, TeamMemberRole.LEADER);
        team.recruitMember(member, TeamMemberRole.MEMBER);
        teamMember = team.getMembers().get(1);
    }

    @Nested
    @DisplayName("상태 멱등성 테스트")
    class StatusIdempotencyTest {

        @Test
        @DisplayName("ACTIVE 상태에서 restore() 호출 시 예외 발생")
        void restore_whenAlreadyActive_throws() {
            // when & then
            assertThatThrownBy(() -> teamMember.restore())
                .isInstanceOf(DuplicatedException.class);
        }

        @Test
        @DisplayName("DELETED 상태에서 delete() 호출 시 예외 발생")
        void delete_whenAlreadyDeleted_throws() {
            // given
            teamMember.delete();

            // when & then
            assertThatThrownBy(() -> teamMember.delete())
                .isInstanceOf(DuplicatedException.class);
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
            User viceLeader = User.create(
                "부회장",
                "아마추어",
                "vice@example.com",
                "vice@kangwon.ac.kr",
                "Abcd1234!",
                "010-1111-1111",
                "공격수",
                "강원대학교",
                "체육학과",
                "22",
                "부회장입니다."
            );
            ReflectionTestUtils.setField(viceLeader, "id", 3L);
            team.recruitMember(viceLeader, TeamMemberRole.VICE_LEADER);

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
            TeamMember leader = new TeamMember(team, member, TeamMemberRole.LEADER);

            // when & then
            assertThat(leader.canMakeJoinDecisionFor(team)).isTrue();
        }

        @Test
        @DisplayName("부회장은 가입 결정 권한을 가진다")
        void canMakeJoinDecision_viceLeader_returnsTrue() {
            // given
            TeamMember vice = new TeamMember(team, member, TeamMemberRole.VICE_LEADER);

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
    @DisplayName("생성 & 기본값 테스트")
    class CreationDefaultsTest {

        @Test
        @DisplayName("역할이 null로 생성되면 기본값은 MEMBER이다")
        void defaultRole_isMember_whenNullProvided() {
            // when
            TeamMember newMember = new TeamMember(team, member, null);

            // then
            assertThat(newMember.getRole()).isEqualTo(TeamMemberRole.MEMBER);
        }
    }
}

