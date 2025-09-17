package com.shootdoori.match.repository;

import com.shootdoori.match.entity.TeamMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    public boolean existsByTeam_IdAndUser_Id(Long teamId, Long userId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
}
