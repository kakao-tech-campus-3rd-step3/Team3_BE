package com.shootdoori.match.repository;

import com.shootdoori.match.entity.Match;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

  @Query("SELECT m FROM Match m " +
      "WHERE (m.team1.teamId = :teamId OR m.team2.teamId = :teamId) " +
      "AND m.status = com.shootdoori.match.entity.MatchStatus.FINISHED  " +
      "AND (m.matchDate < :cursorDate OR (m.matchDate = :cursorDate AND m.matchTime < :cursorTime)) " +
      "ORDER BY m.matchDate DESC, m.matchTime DESC")
  Slice<Match> findCompletedMatchesByTeamId(
      @Param("teamId") Long teamId,
      @Param("cursorDate") LocalDate cursorDate,
      @Param("cursorTime") LocalTime cursorTime,
      Pageable pageable
  );

  Match findByMatchId(int i);
}
