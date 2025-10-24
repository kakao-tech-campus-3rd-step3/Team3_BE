package com.shootdoori.match.entity.auth;

import com.shootdoori.match.config.PasswordEncoderService;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
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

    public boolean matches(String rawCode) {
        return PasswordEncoderService.matches(rawCode, this.code);
    }

    public void validateCode(String rawCode) {
        if (!matches(rawCode)) {
            throw new UnauthorizedException(ErrorCode.INVALID_OTP);
        }
    }
}
