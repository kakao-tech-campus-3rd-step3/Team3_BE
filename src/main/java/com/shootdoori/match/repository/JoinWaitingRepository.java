package com.shootdoori.match.repository;

import com.shootdoori.match.entity.JoinWaiting;
import com.shootdoori.match.entity.JoinWaitingStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface JoinWaitingRepository extends JpaRepository<JoinWaiting, Long> {

    boolean existsByTeam_TeamIdAndApplicant_IdAndStatus(Long teamId, Long applicantId,
        JoinWaitingStatus joinWaitingStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
          select j from JoinWaiting j
          join fetch j.team t
          join fetch j.applicant a
          where j.id = :id and t.teamId = :teamId
        """)
    Optional<JoinWaiting> findByIdAndTeam_TeamIdForUpdate(Long id, Long teamId);

    Page<JoinWaiting> findAllByTeam_TeamIdAndStatus(Long teamId, JoinWaitingStatus status, Pageable pageable);
}
