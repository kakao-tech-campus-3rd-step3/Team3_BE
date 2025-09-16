package com.shootdoori.match.service;


import com.shootdoori.match.dto.MatchApplicationRequestDto;
import com.shootdoori.match.dto.MatchApplicationResponseDto;
import com.shootdoori.match.entity.MatchApplication;
import com.shootdoori.match.entity.MatchApplicationStatus;
import com.shootdoori.match.entity.MatchQueue;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.repository.MatchApplicationRepository;
import com.shootdoori.match.repository.MatchQueueRepository;
import com.shootdoori.match.repository.TeamRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class MatchApplicationService {

  private final MatchApplicationRepository matchApplicationRepository;
  private final MatchQueueRepository matchQueueRepository;
  private final TeamRepository teamRepository;

  public MatchApplicationService(MatchApplicationRepository matchApplicationRepository,
      MatchQueueRepository matchQueueRepository,
      TeamRepository teamRepository) {
    this.matchApplicationRepository = matchApplicationRepository;
    this.matchQueueRepository = matchQueueRepository;
    this.teamRepository = teamRepository;
  }

  @Transactional
  public MatchApplicationResponseDto applyToMatch(Long waitingId, MatchApplicationRequestDto requestDto) {

    MatchQueue targetQueue = matchQueueRepository.findById(waitingId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매치 대기열입니다."));

    Team applicantTeam = teamRepository.findById(requestDto.applicantTeamId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

    MatchApplication application = new MatchApplication(
        applicantTeam,
        targetQueue.getTeam(),
        requestDto.applicationMessage()
    );

    MatchApplication saved = matchApplicationRepository.save(application);

    return new MatchApplicationResponseDto(
        saved.getApplicationId(),
        saved.getApplicantTeam().getTeamId(),
        saved.getTargetTeam().getTeamId(),
        saved.getStatus()
    );
  }
}
