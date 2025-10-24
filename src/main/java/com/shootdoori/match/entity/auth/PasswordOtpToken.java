package com.shootdoori.match.entity.auth;

import com.shootdoori.match.config.PasswordEncoderService;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.TooManyRequestsException;
import com.shootdoori.match.exception.common.UnauthorizedException;
import com.shootdoori.match.value.Expiration;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Entity
public class PasswordOtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private int requestCount = 0;

    @Column(nullable = false)
    private LocalDate lastRequestedDate = LocalDate.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Embedded
    private Expiration expiration;

    protected PasswordOtpToken() {}

    public PasswordOtpToken(User user, String code, int expiryMinutes) {
        this.user = user;
        this.expiration = new Expiration(expiryMinutes);
        this.code = code;
    }

    public Expiration getExpiration() {
        return expiration;
    }

    public int getRequestCount() {
        return this.requestCount;
    }

    public LocalDate getLastRequestedDate() {
        return this.lastRequestedDate;
    }

    public User getUser() { return user; }

    public boolean matches(String rawCode) {
        return PasswordEncoderService.matches(rawCode, this.code);
    }

    public void validateCode(String rawCode) {
        expiration.validateExpiryDate();
        if (!matches(rawCode)) {
            throw new UnauthorizedException(ErrorCode.INVALID_OTP);
        }
    }

    public void updateCode(String newCode, int expiryMinutes) {
        this.code = newCode;
        expiration.updateExpiryDate(expiryMinutes);
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