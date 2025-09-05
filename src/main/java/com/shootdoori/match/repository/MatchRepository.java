package com.shootdoori.match.repository;

import com.shootdoori.match.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

  @Query("SELECT m FROM Match m " +
      "WHERE (m.team1.teamId = :teamId OR m.team2.teamId = :teamId) " +
      "AND m.status = '경기완료' " +
      "ORDER BY m.matchDate DESC, m.matchTime DESC")
  Page<Match> findCompletedMatchesByTeamId(@Param("teamId") Long teamId, Pageable pageable);
}
