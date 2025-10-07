package com.shootdoori.match.repository;

import com.shootdoori.match.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    void deleteAllByUserId(Long userId);

    long countByUserId(Long userId);
}
