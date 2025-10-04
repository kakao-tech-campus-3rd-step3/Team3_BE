package com.shootdoori.match.entity.auth;

import com.shootdoori.match.entity.common.DeviceType;
import com.shootdoori.match.entity.user.User;
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

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    private String userAgent;

    protected RefreshToken() {
    }

    public RefreshToken(String tokenId, User user, LocalDateTime expiryDate, DeviceType deviceType, String userAgent) {
        this.tokenId = tokenId;
        this.user = user;
        this.expiryDate = expiryDate;
        this.deviceType = deviceType;
        this.userAgent = userAgent;
    }

    public User getUser() { return user; }

    public boolean isRevoked() { return revoked; }

    public DeviceType getDeviceType() { return deviceType; }

    public String getUserAgent() { return userAgent; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void revoke() {
        this.revoked = true;
    }
}
