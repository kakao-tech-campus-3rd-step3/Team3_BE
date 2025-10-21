package com.shootdoori.match.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.shootdoori.match.entity.auth.PasswordOtpToken;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.value.Expiration;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PasswordOtpTokenTest {

    private User user;
    private PasswordOtpToken otpToken;

    @BeforeEach
    void setUp() {
        user = createUser();
        otpToken = new PasswordOtpToken(user, "encodedCode", 3);
    }

    @Test
    @DisplayName("만료 시간 검증 - 3분이 지나면 토큰이 만료되어 사용할 수 없다")
    void token_Expired_After3Minutes() throws Exception {
        // given
        Expiration expiration = otpToken.getExpiration();
        Field expiresAtField = expiration.getClass().getDeclaredField("expiryDate");
        expiresAtField.setAccessible(true);
        expiresAtField.set(expiration, LocalDateTime.now().minusMinutes(3));

        // when & then
        assertThatThrownBy(expiration::validateExpiryDate)
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("인증코드 발송 횟수 업데이트 - 하루가 지나면 인증번호 요청 횟수 자동 초기화된다")
    void resetTodayRequestLimit_NextDay_ResetsCount()
        throws NoSuchFieldException, IllegalAccessException {

        for (int i = 0; i < 5; i++) {
            otpToken.incrementRequestCount();
        }
        assertThat(otpToken.getRequestCount()).isEqualTo(5);

        Field field = PasswordOtpToken.class.getDeclaredField("lastRequestedDate");
        field.setAccessible(true);
        field.set(otpToken, LocalDate.now().minusDays(1));

        // when
        assertThatCode(otpToken::incrementRequestCount)
            .doesNotThrowAnyException();

        // then
        assertThat(otpToken.getRequestCount()).isEqualTo(1);
        assertThat(otpToken.getLastRequestedDate()).isEqualTo(LocalDate.now());
    }

    private User createUser() {
        return User.create(
            "kim",
            "아마추어",
            "test@email.ac.kr",
            "asdf02~!",
            "mykakao12",
            "FW",
            "강원대학교",
            "컴퓨터공학과",
            "25",
            "즐겜해요~"
        );
    }
}
