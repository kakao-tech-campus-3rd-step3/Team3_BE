package com.shootdoori.teamMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.TeamMemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TeamMemberRole 테스트")
class TeamMemberRoleTest {

    @Nested
    @DisplayName("fromDisplayName 테스트")
    class FromDisplayNameTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"없는 이름", " 회장 "})
        @DisplayName("유효하지 않은 표시 이름이면 IllegalArgumentException 발생")
        void fromDisplayName_invalid_throws(String displayName) {
            // when & then
            assertThatThrownBy(() -> TeamMemberRole.fromDisplayName(displayName))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("canMakeJoinDecision 테스트")
    class CanMakeJoinDecisionTest {

        @ParameterizedTest
        @CsvSource({
            "LEADER, true",
            "VICE_LEADER, true",
            "MEMBER, false"
        })
        @DisplayName("역할에 따라 가입 결정 가능 여부를 올바르게 반환한다")
        void canMakeJoinDecision(TeamMemberRole role, boolean expected) {
            // when
            boolean result = role.canMakeJoinDecision();

            // then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("canKick(targetRole) 테스트")
    class CanKickTest {

        @ParameterizedTest
        @CsvSource({
            "LEADER, VICE_LEADER, true",
            "LEADER, MEMBER, true",
            "VICE_LEADER, MEMBER, true",
            "LEADER, LEADER, false",
            "VICE_LEADER, LEADER, false",
            "VICE_LEADER, VICE_LEADER, false",
            "MEMBER, LEADER, false",
            "MEMBER, VICE_LEADER, false",
            "MEMBER, MEMBER, false"
        })
        @DisplayName("역할에 따라 강퇴 가능 여부를 올바르게 반환한다")
        void canKick(TeamMemberRole kicker, TeamMemberRole target, boolean expected) {
            // when
            boolean result = kicker.canKick(target);

            // then
            assertThat(result).isEqualTo(expected);
        }
    }
}
