package com.shootdoori.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.TeamSkillLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TeamSkillLevel.fromDisplayName 테스트")
class TeamSkillLevelTest {

    @Test
    @DisplayName("존재하지 않는 표시 이름이면 IllegalArgumentException 발생")
    void fromDisplayName_unknown_throws() {
        // given
        String unknown = "없는 이름";

        // when & then
        assertThatThrownBy(() -> TeamSkillLevel.fromDisplayName(unknown))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("빈 문자열이면 IllegalArgumentException 발생")
    void fromDisplayName_empty_throws() {
        // given
        String empty = "";

        // when & then
        assertThatThrownBy(() -> TeamSkillLevel.fromDisplayName(empty))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 이면 IllegalArgumentException 발생")
    void fromDisplayName_null_throws() {
        // given
        String value = null;

        // when & then
        assertThatThrownBy(() -> TeamSkillLevel.fromDisplayName(value))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 프로")
    void fromDisplayName_valid_returnsEnum_pro() {
        // given
        String input = "프로";

        // when
        TeamSkillLevel result = TeamSkillLevel.fromDisplayName(input);

        // then
        assertThat(result).isEqualTo(TeamSkillLevel.PRO);
    }

    @Test
    @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 세미프로")
    void fromDisplayName_valid_returnsEnum_semi_pro() {
        // given
        String input = "세미프로";

        // when
        TeamSkillLevel result = TeamSkillLevel.fromDisplayName(input);

        // then
        assertThat(result).isEqualTo(TeamSkillLevel.SEMI_PRO);
    }

    @Test
    @DisplayName("유효한 표시 이름이면 해당 Enum 반환 - 아마추어")
    void fromDisplayName_valid_returnsEnum_amateur() {
        // given
        String input = "아마추어";

        // when
        TeamSkillLevel result = TeamSkillLevel.fromDisplayName(input);

        // then
        assertThat(result).isEqualTo(TeamSkillLevel.AMATEUR);
    }
}