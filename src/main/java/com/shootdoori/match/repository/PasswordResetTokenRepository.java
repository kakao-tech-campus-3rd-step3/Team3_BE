package com.shootdoori.match.repository;

import com.shootdoori.match.entity.auth.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser_Id(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PasswordResetToken prt where prt.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("select count(prt) from PasswordResetToken prt where prt.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
