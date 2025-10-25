package com.shootdoori.match.repository;

import com.shootdoori.match.entity.lineup.LineupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LineupRepository extends JpaRepository<LineupMember, Long> {
    List<LineupMember> findByTeamMemberTeamTeamId(Long teamId);
}