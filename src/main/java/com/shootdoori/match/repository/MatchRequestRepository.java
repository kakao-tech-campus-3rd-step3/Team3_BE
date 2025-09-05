package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Integer> {

  @Modifying
  @Query("UPDATE MatchRequest mr " +
      "SET mr.status = '거절' " +
      "WHERE mr.targetTeam.teamId = :targetTeamId " +
      "AND mr.status = '대기중' " +
      "AND mr.requestId <> :acceptedRequestId")
  int rejectOtherRequests(@Param("targetTeamId") Long targetTeamId,
      @Param("acceptedRequestId") Integer acceptedRequestId);

}
