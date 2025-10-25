package com.shootdoori.match.value;

import com.shootdoori.match.config.PasswordEncoderProvider;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Password {

    private String password;

    protected Password() {}

    private Password(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 값입니다.");
        }
        this.password = value;
    }

    public static Password of(String encodedPassword) {
        return new Password(encodedPassword);
    }

    public boolean matches(String rawPassword) {
        return PasswordEncoderProvider.matches(rawPassword, this.password);
    }

    public void validate(String rawPassword) {
        if (!matches(rawPassword)) {
            throw new UnauthorizedException(ErrorCode.FAIL_LOGIN);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password)) return false;
        Password password = (Password) o;
        return Objects.equals(password, password.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password);
    }
}