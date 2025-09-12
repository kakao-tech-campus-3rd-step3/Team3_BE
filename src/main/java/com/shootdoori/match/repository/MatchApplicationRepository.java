package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MatchApplication;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
