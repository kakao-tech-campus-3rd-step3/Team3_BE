package com.shootdoori.match.repository;

import com.shootdoori.match.entity.auth.PasswordOtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordOtpTokenRepository extends JpaRepository<PasswordOtpToken, Long> {
    Optional<PasswordOtpToken> findByUser_Email(String email);

    Optional<PasswordOtpToken> findByUser_Id(Long userId);
}
