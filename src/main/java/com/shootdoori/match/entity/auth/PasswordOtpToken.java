package com.shootdoori.match.entity.auth;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.springframework.security.crypto.password.PasswordEncoder;

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
            throw new UnauthorizedException(ErrorCode.INVALID_OTP);
        }
    }

    public void updateCode(String newCode, int expiryMinutes) {
        this.code = newCode;
        updateExpiryDate(expiryMinutes);
    }
}