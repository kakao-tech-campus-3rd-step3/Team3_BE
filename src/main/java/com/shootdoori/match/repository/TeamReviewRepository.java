package com.shootdoori.match.repository;

import com.shootdoori.match.entity.review.TeamReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamReviewRepository extends JpaRepository<TeamReview, Long> {

    List<TeamReview> findAllByReviewedTeamTeamId(Long reviewedTeamId);

    TeamReview findByReviewedTeamTeamIdAndId(Long teamId, Long reviewId);
}
