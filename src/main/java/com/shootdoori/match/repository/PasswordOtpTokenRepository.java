package com.shootdoori.match.repository;

import com.shootdoori.match.entity.auth.PasswordOtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordOtpTokenRepository extends JpaRepository<PasswordOtpToken, Long> {
    Optional<PasswordOtpToken> findByUser_Email(String email);

    Optional<PasswordOtpToken> findByUser_Id(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PasswordOtpToken pot where pot.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("select count(pot) from PasswordOtpToken pot where pot.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
