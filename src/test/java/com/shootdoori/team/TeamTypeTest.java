package com.shootdoori.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.team.TeamType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TeamType.fromDisplayName 테스트")
public class TeamTypeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"없는 이름", " 중앙동아리 "})
    @DisplayName("유효하지 않은 표시 이름이면 IllegalArgumentException 발생")
    void fromDisplayName_invalid_throws(String displayName) {
        // when & then
        assertThatThrownBy(() -> TeamType.fromDisplayName(displayName))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "중앙동아리, CENTRAL_CLUB",
        "과동아리, DEPARTMENT_CLUB",
        "기타, OTHER"
    })
    @DisplayName("유효한 표시 이름이면 해당 Enum 반환")
    void fromDisplayName_valid_returnsEnum(String input, TeamType expected) {
        // when
        TeamType result = TeamType.fromDisplayName(input);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
