package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MatchApplication;
import com.shootdoori.match.entity.MatchApplicationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchApplicationRepository extends JpaRepository<MatchApplication, Long> {

  @Modifying
  @Query("UPDATE MatchApplication ma " +
      "SET ma.status = com.shootdoori.match.entity.MatchApplicationStatus.REJECTED " +
      "WHERE ma.targetTeam.teamId = :targetTeamId " +
      "AND ma.status = com.shootdoori.match.entity.MatchApplicationStatus.PENDING " +
      "AND ma.applicationId <> :acceptedRequestId")
  int rejectOtherRequests(@Param("targetTeamId") Long targetTeamId,
      @Param("acceptedRequestId") Long acceptedRequestId);

  @Query("SELECT ma FROM MatchApplication ma " +
    "WHERE ma.targetTeam.teamId = :targetTeamId " +
    "AND ma.status = com.shootdoori.match.entity.MatchApplicationStatus.PENDING")
  Slice<MatchApplication> findPendingApplicationsByTargetTeam(@Param("teamId") Long teamId, Pageable pageable);
}
