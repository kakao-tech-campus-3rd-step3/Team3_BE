package com.shootdoori.match.repository;

import com.shootdoori.match.entity.JoinQueue;
import com.shootdoori.match.entity.JoinQueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinQueueRepository extends JpaRepository<JoinQueue, Long> {

    boolean existsByTeam_TeamIdAndApplicant_IdAndStatus(Long teamId, Long applicantId,
        JoinQueueStatus joinQueueStatus);
}
