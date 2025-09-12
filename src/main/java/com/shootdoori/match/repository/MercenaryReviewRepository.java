package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MercenaryReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MercenaryReviewRepository extends JpaRepository<MercenaryReview, Long> {
}
