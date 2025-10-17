package com.shootdoori.teamMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TeamMemberRole 테스트")
class TeamMemberRoleTest {

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
        private TeamMemberRole LEADER = TeamMemberRole.LEADER;
        private TeamMemberRole VICE_LEADER = TeamMemberRole.VICE_LEADER;
        private TeamMemberRole MEMBER = TeamMemberRole.MEMBER;

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
    @DisplayName("canKick 테스트")
    class CanKickTest {

    }
}
