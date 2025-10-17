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
            // given
            String unknown = "없는 이름";

            // when & then
            assertThatThrownBy(() -> TeamMemberRole.fromDisplayName(unknown))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열이면 IllegalArgumentException 발생")
        void fromDisplayName_empty_throws() {
            // given
            String empty = "";

            // when & then
            assertThatThrownBy(() -> TeamMemberRole.fromDisplayName(empty))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 이면 IllegalArgumentException 발생")
        void fromDisplayName_null_throws() {
            // given
            String value = null;

            // when & then
            assertThatThrownBy(() -> TeamMemberRole.fromDisplayName(value))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 회장")
        void fromDisplayName_valid_returnsEnum_leader() {
            // given
            String input = "회장";

            // when
            TeamMemberRole result = TeamMemberRole.fromDisplayName(input);

            // then
            assertThat(result).isEqualTo(TeamMemberRole.LEADER);
        }

        @Test
        @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 부회장")
        void fromDisplayName_valid_returnsEnum_vice_leader() {
            // given
            String input = "부회장";

            // when
            TeamMemberRole result = TeamMemberRole.fromDisplayName(input);

            // then
            assertThat(result).isEqualTo(TeamMemberRole.VICE_LEADER);
        }

        @Test
        @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 일반멤버")
        void fromDisplayName_valid_returnsEnum_member() {
            // given
            String input = "일반멤버";

            // when
            TeamMemberRole result = TeamMemberRole.fromDisplayName(input);

            // then
            assertThat(result).isEqualTo(TeamMemberRole.MEMBER);
        }
    }

    @Nested
    @DisplayName("canMakeJoinDecision 테스트")
    class CanMakeJoinDecisionTest {

        @Test
        @DisplayName("일반 멤버이면 false를 반환")
        void canMakeJoinDecision_valid_returnsFalse_for_member() {
            // given
            // when
            boolean result = MEMBER.canMakeJoinDecision();

            // then
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("회장이면 true를 반환")
        void canMakeJoinDecision_valid_returnsTrue_for_leader() {
            // given
            // when
            boolean result = LEADER.canMakeJoinDecision();

            // then
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("부회장이면 true를 반환")
        void canMakeJoinDecision_valid_returnsTrue_for_vice_leader() {
            // given
            // when
            boolean result = VICE_LEADER.canMakeJoinDecision();

            // then
            assertThat(result).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("canKick(targetRole) 테스트")
    class CanKickTest {

        @Test
        @DisplayName("회장은 부회장을 강퇴할 수 있다")
        void leader_canKick_vice_leader() {
            // given
            // when
            boolean result = LEADER.canKick(VICE_LEADER);

            // then
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("회장은 일반 멤버를 강퇴할 수 있다")
        void leader_canKick_member() {
            // given
            // when
            boolean result = LEADER.canKick(MEMBER);

            // then
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("부회장은 일반 멤버를 강퇴할 수 있다")
        void vice_leader_canKick_member() {
            // given
            // when
            boolean result = VICE_LEADER.canKick(MEMBER);

            // then
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("부회장은 회장을 강퇴할 수 없다")
        void vice_leader_canNotKick_leader() {
            // given
            // when
            boolean result = VICE_LEADER.canKick(LEADER);

            // then
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("일반 멤버는 회장을 강퇴할 수 없다")
        void member_canNotKick_leader() {
            // given
            // when
            boolean result = MEMBER.canKick(LEADER);

            // then
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("일반 멤버는 부회장을 강퇴할 수 없다")
        void member_canNotKick_vice_leader() {
            // given
            // when
            boolean result = MEMBER.canKick(VICE_LEADER);

            // then
            assertThat(result).isEqualTo(false);
        }

        @Test
        @DisplayName("일반 멤버는 일반 멤버를 강퇴할 수 없다")
        void member_canNotKick_member() {
            // given
            // when
            boolean result = MEMBER.canKick(MEMBER);

            // then
            assertThat(result).isEqualTo(false);
        }
    }
}
