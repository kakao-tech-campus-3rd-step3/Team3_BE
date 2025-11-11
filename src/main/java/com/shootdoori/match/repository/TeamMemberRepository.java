package com.shootdoori.match.repository;

import com.shootdoori.match.entity.team.TeamMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeam_TeamIdAndUser_Id(Long teamId, Long userId);

    Page<TeamMember> findAllByTeam_TeamId(Long teamId, Pageable pageable);

    public boolean existsByTeam_TeamIdAndUser_Id(Long teamId, Long userId);

    Optional<TeamMember> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    List<TeamMember> findAllByUserId(Long userId);

    @Query("SELECT tm FROM TeamMember tm " +
        "WHERE tm.team.teamId = :teamId " +
        "AND (:cursorId IS NULL OR tm.id > :cursorId) " +
        "ORDER BY tm.id ASC")
    Slice<TeamMember> findSliceByTeam_TeamId(
        @Param("teamId") Long teamId,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );
}