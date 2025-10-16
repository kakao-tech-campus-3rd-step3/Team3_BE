package com.shootdoori.match.repository;

import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    @Query("SELECT mr FROM MatchRequest mr " +
        "WHERE mr.targetTeam.teamId = :targetTeamId " +
        "AND mr.status = com.shootdoori.match.entity.match.request.MatchRequestStatus.PENDING " +
        "AND mr.requestId <> :acceptedRequestId " +
        "AND mr.matchWaiting.waitingId = :waitingId")
    List<MatchRequest> findRequestsToReject(@Param("targetTeamId") Long targetTeamId,
                                            @Param("acceptedRequestId") Long acceptedRequestId,
                                            @Param("waitingId") Long waitingId);

    @Query("SELECT mr FROM MatchRequest mr " +
        "WHERE mr.targetTeam.teamId = :targetTeamId " +
        "AND mr.status = com.shootdoori.match.entity.match.request.MatchRequestStatus.PENDING " +
        "ORDER BY mr.requestAt ASC ")
    Slice<MatchRequest> findPendingRequestsByTargetTeam(@Param("targetTeamId") Long targetTeamId, Pageable pageable);

    @Query("SELECT mr FROM MatchRequest mr " +
        "WHERE mr.requestTeam.teamId = :requestTeamId " +
        "ORDER BY mr.requestAt DESC")
    Slice<MatchRequest> findSentRequestsByTeam(@Param("requestTeamId") Long requestTeamId,
                                               Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(mr) > 0 THEN true ELSE false END " +
        "FROM MatchRequest mr " +
        "WHERE mr.matchWaiting.waitingId = :waitingId " +
        "AND mr.requestTeam.teamId = :requestTeamId " +
        "AND mr.status <> :excludedStatus")
    boolean existsActiveRequest(
        @Param("waitingId") Long waitingId,
        @Param("requestTeamId") Long requestTeamId,
        @Param("excludedStatus") MatchRequestStatus excludedStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from MatchRequest mr where mr.requestTeam.teamId = :teamId or mr.targetTeam.teamId = :teamId")
    void deleteAllByTeamId(@Param("teamId") Long teamId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MatchRequest mr SET mr.status = 'CANCELED' WHERE mr.requestTeam.teamId = :teamId OR mr.targetTeam.teamId = :teamId")
    void cancelAllByTeamId(@Param("teamId") Long teamId);

}
