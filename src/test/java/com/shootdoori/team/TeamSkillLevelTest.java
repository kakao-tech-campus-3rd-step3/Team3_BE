package com.shootdoori.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.SkillLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TeamSkillLevel.fromDisplayName 테스트")
class TeamSkillLevelTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"없는 이름", " 프로 "})
    @DisplayName("유효하지 않은 표시 이름이면 IllegalArgumentException 발생")
    void fromDisplayName_invalid_throws(String displayName) {
        // when & then
        assertThatThrownBy(() -> SkillLevel.fromDisplayName(displayName))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "프로, PRO",
        "세미프로, SEMI_PRO",
        "아마추어, AMATEUR"
    })
    @DisplayName("유효한 표시 이름이면 해당 Enum 반환")
    void fromDisplayName_valid_returnsEnum(String input, SkillLevel expected) {
        // when
        SkillLevel result = SkillLevel.fromDisplayName(input);

        // then
        assertThat(result).isEqualTo(expected);
    }
}