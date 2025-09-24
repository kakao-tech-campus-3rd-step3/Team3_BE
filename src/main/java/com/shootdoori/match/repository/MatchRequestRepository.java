package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MatchRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

  @Modifying
  @Query("UPDATE MatchRequest mr " +
      "SET mr.status = com.shootdoori.match.entity.MatchRequestStatus.REJECTED " +
      "WHERE mr.targetTeam.teamId = :targetTeamId " +
      "AND mr.status = com.shootdoori.match.entity.MatchRequestStatus.PENDING " +
      "AND mr.requestId <> :acceptedRequestId " +
      "AND mr.matchWaiting.waitingId = :waitingId ")
  int rejectOtherRequests(@Param("targetTeamId") Long targetTeamId,
      @Param("acceptedRequestId") Long acceptedRequestId);

  @Query("SELECT mr FROM MatchRequest mr " +
    "WHERE mr.targetTeam.teamId = :targetTeamId " +
    "AND mr.status = com.shootdoori.match.entity.MatchRequestStatus.PENDING")
  Slice<MatchRequest> findPendingRequestsByTargetTeam(@Param("teamId") Long teamId, Pageable pageable);
}
