package com.shootdoori.match.repository;

import com.shootdoori.match.entity.TeamMember;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    Page<TeamMember> findAllByTeamId(Long teamId, Pageable pageable);

    public boolean existsByTeamTeamIdAndUserId(Long teamId, Long userId);
}
