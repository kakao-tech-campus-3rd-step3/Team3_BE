package com.shootdoori.match.repository;

import com.shootdoori.match.entity.team.TeamMember;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeam_TeamIdAndUser_Id(Long teamId, Long userId);

    Page<TeamMember> findAllByTeam_TeamId(Long teamId, Pageable pageable);

    public boolean existsByTeam_TeamIdAndUser_Id(Long teamId, Long userId);

    Optional<TeamMember> findByIdAndTeam_TeamId(Long teamMemberId, Long teamId);

    Optional<TeamMember> findByUser_Id(Long userId);
}