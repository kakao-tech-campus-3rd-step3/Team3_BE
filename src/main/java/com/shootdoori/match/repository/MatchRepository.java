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

    @Query("SELECT new com.shootdoori.match.dto.MatchSummaryProjection(" +
        "m.matchId, m.matchDate, m.matchTime, m.status, " +
        "t1.teamName.name, t2.teamName.name, " +
        "v.venueName, m.createdAt, m.updatedAt) " +
        "FROM Match m " +
        "JOIN m.team1 t1 " +
        "JOIN m.team2 t2 " +
        "JOIN m.venue v " +
        "WHERE (t1.teamId = :teamId OR t2.teamId = :teamId) " +
        "AND m.status = :status " +
        "AND (m.matchDate < :cursorDate " +
        "     OR (m.matchDate = :cursorDate AND m.matchTime < :cursorTime)) " +
        "ORDER BY m.matchDate DESC, m.matchTime DESC")
    Slice<MatchSummaryProjection> findMatchSummariesByTeamIdAndStatus(
        @Param("teamId") Long teamId,
        @Param("status") MatchStatus status,
        @Param("cursorDate") LocalDate cursorDate,
        @Param("cursorTime") LocalTime cursorTime,
        Pageable pageable
    );

    @Query("SELECT new com.shootdoori.match.dto.MatchSummaryProjection(" +
        "m.matchId, m.matchDate, m.matchTime, m.status, " +
        "t1.teamName.name, t2.teamName.name, " +
        "v.venueName, m.createdAt, m.updatedAt) " +
        "FROM Match m " +
        "JOIN m.team1 t1 " +
        "JOIN m.team2 t2 " +
        "JOIN m.venue v " +
        "WHERE (t1.teamId = :teamId OR t2.teamId = :teamId) " +
        "AND m.status = :status " +
        "ORDER BY m.matchDate DESC, m.matchTime DESC")
    Slice<MatchSummaryProjection> findFirstPageMatchSummariesByTeamIdAndStatus(
        @Param("teamId") Long teamId,
        @Param("status") MatchStatus status,
        Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Match m where m.team1.teamId = :teamId or m.team2.teamId = :teamId")
    void deleteAllByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT m FROM Match m " +
        "WHERE m.status = 'MATCHED' " +
        "AND (m.matchDate < :cutoffDate " +
        "     OR (m.matchDate = :cutoffDate AND m.matchTime <= :cutoffTime))")
    List<Match> findMatchesToFinish(@Param("cutoffDate") LocalDate cutoffDate,
                                    @Param("cutoffTime") LocalTime cutoffTime);
}
