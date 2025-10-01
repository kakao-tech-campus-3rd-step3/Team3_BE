package com.shootdoori.match.entity;

import com.shootdoori.match.exception.UnauthorizedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
public class PasswordOtpToken extends BasePasswordToken {

    @Column(nullable = false)
    private String code;

    protected PasswordOtpToken() {}

    public PasswordOtpToken(User user, String code, int expiryMinutes) {
        super(user, expiryMinutes);
        this.code = code;
    }

    public boolean matches(String rawCode, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawCode, this.code);
    }

    public void validateCode(String rawCode, PasswordEncoder passwordEncoder) {
        validateExpiryDate();
        if (!matches(rawCode, passwordEncoder)) {
            throw new UnauthorizedException("인증번호가 일치하지 않습니다.");
        }
    }

    public void updateCode(String newCode, int expiryMinutes) {
        this.code = newCode;
        updateExpiryDate(expiryMinutes);
    }
}