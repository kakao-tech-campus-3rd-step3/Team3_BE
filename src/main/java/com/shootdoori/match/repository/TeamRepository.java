package com.shootdoori.match.repository;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamStatus;
import com.shootdoori.match.value.UniversityName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Page<Team> findAllByUniversity(UniversityName university, Pageable pageable);

    @Query(value = "SELECT t FROM Team t WHERE t.university = :university", nativeQuery = true)
    Page<Team> findAllByUniversityIncludingDeleted(@Param("university") UniversityName university, Pageable pageable);

    @Query(value = "SELECT * FROM team WHERE team_id = :teamId", nativeQuery = true)
    Optional<Team> findByTeamIdIncludingDeleted(@Param("teamId") Long teamId);

    @Query(value = "SELECT t FROM Team t", nativeQuery = true)
    Page<Team> findAllIncludingDeleted(Pageable pageable);
}
