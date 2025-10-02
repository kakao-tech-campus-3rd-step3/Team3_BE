package com.shootdoori.match.entity;

import com.shootdoori.match.exception.ErrorCode;
import com.shootdoori.match.exception.UnauthorizedException;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
public class EmailVerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String code;

    protected EmailVerificationCode() {}

    public EmailVerificationCode(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return this.email;
    }

    public String getCode() {
        return this.code;
    }

    public void updateCode(String newCode) {
        this.code = newCode;
    }

    public boolean matches(String rawCode, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawCode, this.code);
    }

    public void validateCode(String rawCode, PasswordEncoder passwordEncoder) {
        if (!matches(rawCode, passwordEncoder)) {
            throw new UnauthorizedException(ErrorCode.INVALID_OTP);
        }
    }
}
