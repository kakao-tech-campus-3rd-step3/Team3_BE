package com.shootdoori.match.entity.auth;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.value.Expiration;
import jakarta.persistence.*;

@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Embedded
    private Expiration expiration;

    protected PasswordResetToken() {}

    public PasswordResetToken(User user, String token, int expiryMinutes) {
        this.user = user;
        this.expiration = new Expiration(expiryMinutes);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public User getUser() { return user; }

    public void validateExpiryDate() {
        expiration.validateExpiryDate();
    }

    public void updateToken(String newToken, int expiryMinutes) {
        this.token = newToken;
        expiration.updateExpiryDate(expiryMinutes);
    }
}