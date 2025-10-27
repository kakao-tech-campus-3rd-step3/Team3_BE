package com.shootdoori.match.repository;

import com.shootdoori.match.entity.mercenary.MercenaryRecruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MercenaryRecruitmentRepository extends JpaRepository<MercenaryRecruitment, Long> {
    Page<MercenaryRecruitment> findByTeam_Captain_Id(Long userId, Pageable pageable);
}
