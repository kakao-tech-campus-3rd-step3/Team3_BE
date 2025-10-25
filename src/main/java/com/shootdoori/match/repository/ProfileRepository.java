package com.shootdoori.match.repository;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM `user` WHERE id = :id", nativeQuery = true)
    Optional<User> findByIdIncludingDeleted(@Param("id") Long id);

    List<User> findBySoftDeleteStatusAndAuditUpdatedAtBefore(UserStatus status, LocalDateTime threshold);

    @Query(value = "SELECT * FROM `user`", nativeQuery = true)
    List<User> findAllIncludingDeleted();
}
