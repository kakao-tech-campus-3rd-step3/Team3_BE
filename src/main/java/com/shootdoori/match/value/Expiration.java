package com.shootdoori.match.value;

import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class Expiration {

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    protected Expiration() {}

    public Expiration(int minutes) {
        this.expiryDate = LocalDateTime.now().plusMinutes(minutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void validateExpiryDate() {
        if (isExpired()) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN);
        }
    }

    public void updateExpiryDate(int expiryMinutes) {
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
    }
}
