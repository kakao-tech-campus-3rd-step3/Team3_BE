package com.shootdoori.match.repository;

import com.shootdoori.match.entity.MercenaryReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MercenaryReviewRepository extends JpaRepository<MercenaryReview, Long> {
    MercenaryReview findByMercenaryUserIdAndId(Long userId, Long reviewId);

    List<MercenaryReview> findAllByMercenaryUserId(Long userId);
}
