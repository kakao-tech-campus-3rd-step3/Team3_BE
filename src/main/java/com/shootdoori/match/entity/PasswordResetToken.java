package com.shootdoori.match.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class PasswordResetToken extends BasePasswordToken {
    @Column(nullable = false, unique = true)
    private String token;

    protected PasswordResetToken() {}

    public PasswordResetToken(User user, String token, int expiryMinutes) {
        super(user, expiryMinutes);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void updateToken(String newToken, int expiryMinutes) {
        this.token = newToken;
        updateExpiryDate(expiryMinutes);
    }
}