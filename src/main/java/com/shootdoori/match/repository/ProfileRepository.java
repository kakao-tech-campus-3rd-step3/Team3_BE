package com.shootdoori.match.repository;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByStatusAndStatusChangedAtBefore(UserStatus status, LocalDateTime before);
}
