package com.shootdoori.match.repository;

import com.shootdoori.match.dto.TeamReviewResponseDto;
import com.shootdoori.match.entity.TeamReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamReviewRepository extends JpaRepository<TeamReview, Long> {
    List<TeamReview> findAllByReviewedTeamId(Long ReviewedTeamId);

    TeamReviewResponseDto findByReviewedTeamIdAndId(Long ReviewedTeamId, Long id);
}
