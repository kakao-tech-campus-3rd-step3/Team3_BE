package com.shootdoori.match.repository;

import com.shootdoori.match.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<User, Long> {
    boolean existsByEmailOrUniversityEmail(String email, String universityEmail);
}
