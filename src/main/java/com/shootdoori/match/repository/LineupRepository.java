package com.shootdoori.match.repository;

import com.shootdoori.match.entity.lineup.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, Long> {
    List<Lineup> findByTeamMemberTeamTeamId(Long teamId);
}