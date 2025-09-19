package com.shootdoori.match.repository;

import com.shootdoori.match.entity.TeamReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamReviewRepository extends JpaRepository<TeamReview, Long> {

    List<TeamReview> findAllByReviewedTeamTeamId(Long reviewedTeamId);

    TeamReview findByReviewedTeam_TeamIdAndId(@Param("reviewedTeamId") Long reviewedTeamId,
        @Param("id") Long id);
}
