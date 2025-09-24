package com.shootdoori.match.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class RefreshToken {
    @Id
    private String tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiryDate;

    private boolean revoked = false;

    private String deviceInfo;

    protected RefreshToken() {
    }

    public RefreshToken(String tokenId, User user, LocalDateTime expiryDate, String deviceInfo) {
        this.tokenId = tokenId;
        this.user = user;
        this.expiryDate = expiryDate;
        this.deviceInfo = deviceInfo;
    }

    public User getUser() { return user; }

    public boolean isRevoked() { return revoked; }

    public String getDeviceInfo() { return deviceInfo; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void revoke() {
        this.revoked = true;
    }
}
