package com.shootdoori.match.repository;

import com.shootdoori.match.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    public boolean existsByTeam_TeamIdAndUser_Id(Long teamId, Long userId);
}
