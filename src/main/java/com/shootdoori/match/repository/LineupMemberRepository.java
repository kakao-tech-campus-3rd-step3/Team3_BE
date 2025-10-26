package com.shootdoori.match.repository;

import com.shootdoori.match.entity.lineup.Lineup;
import com.shootdoori.match.entity.lineup.LineupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LineupMemberRepository extends JpaRepository<LineupMember, Long> {
    List<LineupMember> findAllByLineupId(Long lineupId);

    Optional<LineupMember> findFirstByLineupId(Long id);

    void deleteAllByLineupId(Long id);
}