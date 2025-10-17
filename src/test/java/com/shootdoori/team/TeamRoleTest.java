package com.shootdoori.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.TeamMemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TeamMemberRole.fromDisplayName 테스트")
class TeamMemberRoleTest {

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
    @DisplayName("유효한 표시이름이면 해당 Enum 반환 - 회장")
    void fromDisplayName_valid_returnsEnum_leader() {
        assertThat(TeamMemberRole.fromDisplayName("회장"))
            .isEqualTo(TeamMemberRole.LEADER);
    }

    @Test
    @DisplayName("유효한 표시이름이면 해당 Enum 반환 - 부회장")
    void fromDisplayName_valid_returnsEnum_vice_leader() {
        assertThat(TeamMemberRole.fromDisplayName("부회장"))
            .isEqualTo(TeamMemberRole.VICE_LEADER);
    }

    @Test
    @DisplayName("유효한 표시이름이면 해당 Enum 반환 - 일반멤버")
    void fromDisplayName_valid_returnsEnum_member() {
        assertThat(TeamMemberRole.fromDisplayName("일반멤버"))
            .isEqualTo(TeamMemberRole.MEMBER);
    }
}
