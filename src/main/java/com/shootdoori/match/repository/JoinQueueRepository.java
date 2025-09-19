package com.shootdoori.match.repository;

import com.shootdoori.match.entity.JoinQueue;
import com.shootdoori.match.entity.JoinQueueStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface JoinQueueRepository extends JpaRepository<JoinQueue, Long> {

    boolean existsByTeam_TeamIdAndApplicant_IdAndStatus(Long teamId, Long applicantId,
        JoinQueueStatus joinQueueStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
          select j from JoinQueue j
          join fetch j.team t
          join fetch j.applicant a
          where j.id = :id and t.teamId = :teamId
        """)
    Optional<JoinQueue> findByIdAndTeam_TeamIdForUpdate(Long id, Long teamId);
}
