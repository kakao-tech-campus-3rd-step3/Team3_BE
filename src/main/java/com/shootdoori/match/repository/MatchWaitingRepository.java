package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MatchWaiting;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchWaitingRepository extends JpaRepository<MatchWaiting, Long> {
  @Query("SELECT mw FROM MatchWaiting mw " +
      "WHERE mw.preferredDate = :date " +
      "AND mw.status = '대기중' " +
      "AND mw.team.id <> :teamId " + //우리 팀 제외
      "ORDER BY mw.preferredTimeStart ASC")
  Page<MatchWaiting> findAvailableMatchesByDate(
      @Param("date") LocalDate date,
      @Param("teamId") Long teamId,
      Pageable pageable);
}

