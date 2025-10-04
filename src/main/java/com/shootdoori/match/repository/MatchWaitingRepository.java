package com.shootdoori.match.repository;

import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchWaitingRepository extends JpaRepository<MatchWaiting, Long> {

    @Query("SELECT mw FROM MatchWaiting mw " +
        "WHERE mw.preferredDate = :date " +
        "AND mw.status = com.shootdoori.match.entity.match.waiting.MatchWaitingStatus.WAITING " +
        "AND mw.team.id <> :teamId " +
        "AND mw.expiresAt > CURRENT_TIMESTAMP " +
        "AND (:lastTime IS NULL OR mw.preferredTimeStart >= :lastTime) " +
        "ORDER BY mw.preferredTimeStart ASC")
    Slice<MatchWaiting> findAvailableMatchesByDateCursor(
        @Param("teamId") Long teamId,
        @Param("date") LocalDate date,
        @Param("lastTime") LocalTime lastTime,
        Pageable pageable
    );

    @Query("SELECT mw FROM MatchWaiting mw " +
        "WHERE mw.team.id = :teamId " +
        "ORDER BY mw.createdAt DESC")
    Slice<MatchWaiting> findMyTeamMatchWaitingHistory(
        @Param("teamId") Long teamId,
        Pageable pageable
    );
}

