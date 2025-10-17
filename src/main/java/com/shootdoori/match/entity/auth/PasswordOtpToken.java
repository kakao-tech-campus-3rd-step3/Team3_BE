package com.shootdoori.match.entity.auth;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.TooManyRequestsException;
import com.shootdoori.match.exception.common.UnauthorizedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Entity
public class PasswordOtpToken extends BasePasswordToken {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private int requestCount = 0;

    @Column(nullable = false)
    private LocalDate lastRequestedDate = LocalDate.now();

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

    private boolean canRequestToday() {
        resetTodayRequestLimit();
        return requestCount < 5;
    }

    private void resetTodayRequestLimit() {
        if (lastRequestedDate.isBefore(LocalDate.now())) {
            lastRequestedDate = LocalDate.now();
            requestCount = 0;
        }
    }

    public void incrementRequestCount() {
        if (!canRequestToday()) {
            throw new TooManyRequestsException(ErrorCode.LIMITED_OTP_REQUESTS);
        }

        requestCount++;
    }
}