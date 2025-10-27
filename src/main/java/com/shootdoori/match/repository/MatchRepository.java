package com.shootdoori.match.repository;

import com.shootdoori.match.dto.MatchSummaryProjection;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
        SELECT new com.shootdoori.match.dto.MatchSummaryProjection(
            m.matchId, m.matchDate, m.matchTime, m.status,
            createTeam.teamId, requestTeam.teamId,
            createTeam.teamName.name, requestTeam.teamName.name,
            v.venueName, m.audit.createdAt, m.audit.updatedAt
        )
        FROM Match m
        JOIN m.matchCreateTeam createTeam
        JOIN m.matchRequestTeam requestTeam
        JOIN m.venue v
        WHERE (createTeam.teamId = :teamId OR requestTeam.teamId = :teamId)
        AND m.status = :status
        AND (:cursorDate IS NULL 
             OR m.matchDate < :cursorDate
             OR (m.matchDate = :cursorDate AND m.matchTime < :cursorTime))
        ORDER BY m.matchDate DESC, m.matchTime DESC
        """)
    Slice<MatchSummaryProjection> findMatchSummariesByTeamIdAndStatus(
        @Param("teamId") Long teamId,
        @Param("status") MatchStatus status,
        @Param("cursorDate") LocalDate cursorDate,
        @Param("cursorTime") LocalTime cursorTime,
        Pageable pageable
    );

    @Query("SELECT m FROM Match m " +
        "WHERE m.status = 'MATCHED' " +
        "AND (m.matchDate < :cutoffDate " +
        "     OR (m.matchDate = :cutoffDate AND m.matchTime <= :cutoffTime))")
    List<Match> findMatchesToFinish(@Param("cutoffDate") LocalDate cutoffDate,
                                    @Param("cutoffTime") LocalTime cutoffTime);
}
