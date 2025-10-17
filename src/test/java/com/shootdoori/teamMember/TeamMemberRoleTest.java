package com.shootdoori.teamMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.TeamMemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TeamMemberRole 테스트")
class TeamMemberRoleTest {

    private TeamMemberRole LEADER = TeamMemberRole.LEADER;
    private TeamMemberRole VICE_LEADER = TeamMemberRole.VICE_LEADER;
    private TeamMemberRole MEMBER = TeamMemberRole.MEMBER;

    @Nested
    @DisplayName("fromDisplayName 테스트")
    class FromDisplayNameTest {

        @Test
        @DisplayName("존재하지 않는 표시 이름이면 IllegalArgumentException 발생")
        void fromDisplayName_unknown_throws() {
            assertThatThrownBy(() -> TeamMemberRole.fromDisplayName("없는 이름"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열이면 IllegalArgumentException 발생")
        void fromDisplayName_empty_throws() {
            assertThatThrownBy(() -> TeamMemberRole.fromDisplayName(""))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 이면 IllegalArgumentException 발생")
        void fromDisplayName_null_throws() {
            assertThatThrownBy(() -> TeamMemberRole.fromDisplayName(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 회장")
        void fromDisplayName_valid_returnsEnum_leader() {
            assertThat(TeamMemberRole.fromDisplayName("회장"))
                .isEqualTo(TeamMemberRole.LEADER);
        }

        @Test
        @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 부회장")
        void fromDisplayName_valid_returnsEnum_vice_leader() {
            assertThat(TeamMemberRole.fromDisplayName("부회장"))
                .isEqualTo(TeamMemberRole.VICE_LEADER);
        }

        @Test
        @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 일반멤버")
        void fromDisplayName_valid_returnsEnum_member() {
            assertThat(TeamMemberRole.fromDisplayName("일반멤버"))
                .isEqualTo(TeamMemberRole.MEMBER);
        }
    }

    @Nested
    @DisplayName("canMakeJoinDecision 테스트")
    class CanMakeJoinDecisionTest {

        @Test
        @DisplayName("일반 멤버이면 false를 반환")
        void canMakeJoinDecision_valid_returnsFalse_for_member() {
            assertThat(MEMBER.canMakeJoinDecision())
                .isEqualTo(false);
        }

        @Test
        @DisplayName("회장이면 true를 반환")
        void canMakeJoinDecision_valid_returnsTrue_for_leader() {
            assertThat(LEADER.canMakeJoinDecision())
                .isEqualTo(true);
        }

        @Test
        @DisplayName("부회장이면 true를 반환")
        void canMakeJoinDecision_valid_returnsTrue_for_vice_leader() {
            assertThat(VICE_LEADER.canMakeJoinDecision())
                .isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("canKick(targetRole) 테스트")
    class CanKickTest {

        @Test
        @DisplayName("회장은 부회장을 강퇴할 수 있다")
        void leader_canKick_vice_leader() {
            assertThat(LEADER.canKick(VICE_LEADER))
                .isEqualTo(true);
        }

        @Test
        @DisplayName("회장은 일반 멤버를 강퇴할 수 있다")
        void leader_canKick_member() {
            assertThat(LEADER.canKick(MEMBER))
                .isEqualTo(true);
        }

        @Test
        @DisplayName("부회장은 일반 멤버를 강퇴할 수 있다")
        void vice_leader_canKick_member() {
            assertThat(VICE_LEADER.canKick(MEMBER))
                .isEqualTo(true);
        }

        @Test
        @DisplayName("부회장은 회장을 강퇴할 수 없다")
        void vice_leader_canNotKick_leader() {
            assertThat(VICE_LEADER.canKick(LEADER))
                .isEqualTo(false);
        }

        @Test
        @DisplayName("일반 멤버는 회장을 강퇴할 수 없다")
        void member_canNotKick_leader() {
            assertThat(MEMBER.canKick(LEADER))
                .isEqualTo(false);
        }

        @Test
        @DisplayName("일반 멤버는 부회장을 강퇴할 수 없다")
        void member_canNotKick_vice_leader() {
            assertThat(MEMBER.canKick(VICE_LEADER))
                .isEqualTo(false);
        }

        @Test
        @DisplayName("일반 멤버는 일반 멤버를 강퇴할 수 없다")
        void member_canNotKick_member() {
            assertThat(MEMBER.canKick(MEMBER))
                .isEqualTo(false);
        }
    }
}
