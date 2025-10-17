package com.shootdoori.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.TeamType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TeamType.fromDisplayName 테스트")
public class TeamTypeTest {

    @Test
    @DisplayName("존재하지 않는 표시이름이면 IllegalArgumentException 발생")
    void fromDisplayName_unknown_throws() {
        assertThatThrownBy(() -> TeamType.fromDisplayName("없는 이름"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("빈 문자열이면 IllegalArgumentException 발생")
    void fromDisplayName_empty_throws() {
        assertThatThrownBy(() -> TeamType.fromDisplayName(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 이면 IllegalArgumentException 발생")
    void fromDisplayName_null_throws() {
        assertThatThrownBy(() -> TeamType.fromDisplayName(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("공백 포함 시 일치하지 않으면 IllegalArgumentException 발생")
    void fromDisplayName_withSpaces_throws() {
        assertThatThrownBy(() -> TeamType.fromDisplayName(" 중앙동아리 "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유효한 표시이름이면 해당 Enum 반환 - 중앙 아리")
    void fromDisplayName_valid_returnsEnum_central_club() {
        assertThat(TeamType.fromDisplayName("중앙동아리")).isEqualTo(TeamType.CENTRAL_CLUB);
    }

    @Test
    @DisplayName("유효한 표시이름이면 해당 Enum 반환 - 과동아리")
    void fromDisplayName_valid_returnsEnum_department_club() {
        assertThat(TeamType.fromDisplayName("과동아리")).isEqualTo(TeamType.DEPARTMENT_CLUB);
    }

    @Test
    @DisplayName("유효한 표시이름이면 해당 Enum 반환 - 기타")
    void fromDisplayName_valid_returnsEnum_other() {
        assertThat(TeamType.fromDisplayName("기타")).isEqualTo(TeamType.OTHER);
    }
}
