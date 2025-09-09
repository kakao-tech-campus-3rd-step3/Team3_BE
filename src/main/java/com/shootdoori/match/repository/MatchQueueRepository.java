package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MatchQueue;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchQueueRepository extends JpaRepository<MatchQueue, Long> {

    @Query("SELECT mq FROM MatchQueue mq " +
        "WHERE mq.preferredDate = :date " +
        "AND mq.status = com.shootdoori.match.entity.MatchQueueStatus.WAITING " +
        "AND mq.team.id <> :teamId " +
        "AND mq.expiresAt > CURRENT_TIMESTAMP " +
        "AND (:lastTime IS NULL OR mq.preferredTimeStart > :lastTime) " +
        "ORDER BY mq.preferredTimeStart ASC")
    Slice<MatchQueue> findAvailableMatchesByDateCursor(
        @Param("date") LocalDate date,
        @Param("teamId") Long teamId,
        @Param("lastTime") LocalTime lastTime,
        Pageable pageable
    );
}

