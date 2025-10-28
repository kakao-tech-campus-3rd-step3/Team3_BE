package com.shootdoori.match.repository;

import com.shootdoori.match.entity.lineup.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, Long> {
}
