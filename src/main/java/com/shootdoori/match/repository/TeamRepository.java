package com.shootdoori.match.repository;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.value.UniversityName;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"members"})
    @Query("SELECT t FROM Team t WHERE t.teamId = :teamId")
    Optional<Team> findByIdWithMembers(@Param("teamId") Long teamId);

    Page<Team> findAllByUniversity(UniversityName university, Pageable pageable);

    @Query(value = "SELECT * FROM team WHERE university = :university", nativeQuery = true)
    Page<Team> findAllByUniversityIncludingDeleted(@Param("university") String university, Pageable pageable);

    @Query(value = "SELECT * FROM team WHERE team_id = :teamId", nativeQuery = true)
    Optional<Team> findByTeamIdIncludingDeleted(@Param("teamId") Long teamId);
}
