package com.shootdoori.match.entity;

import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BasePasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    protected LocalDateTime expiryDate;

    protected BasePasswordToken() {}

    protected BasePasswordToken(User user, int expiryMinutes) {
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    public User getUser() { return user; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void validateExpiryDate() {
        if (isExpired()) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN);
        }
    }

    protected void updateExpiryDate(int expiryMinutes) {
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
    }
}