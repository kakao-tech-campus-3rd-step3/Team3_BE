package com.shootdoori.match.repository;

import com.shootdoori.match.entity.TeamReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamReviewRepository extends JpaRepository<TeamReview, Long> {
}
