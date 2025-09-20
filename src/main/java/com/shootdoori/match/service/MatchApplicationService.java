package com.shootdoori.match.service;


import com.shootdoori.match.dto.MatchApplicationRequestDto;
import com.shootdoori.match.dto.MatchApplicationResponseDto;
import com.shootdoori.match.dto.MatchConfirmedResponseDto;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.repository.MatchApplicationRepository;
import com.shootdoori.match.repository.MatchQueueRepository;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.TeamRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchApplicationService {

  private final MatchApplicationRepository matchApplicationRepository;
  private final MatchQueueRepository matchQueueRepository;
  private final MatchRepository matchRepository;
  private final TeamRepository teamRepository;

  public MatchApplicationService(MatchApplicationRepository matchApplicationRepository,
      MatchQueueRepository matchQueueRepository,
      MatchRepository matchRepository,
      TeamRepository teamRepository) {
    this.matchApplicationRepository = matchApplicationRepository;
    this.matchQueueRepository = matchQueueRepository;
    this.matchRepository = matchRepository;
    this.teamRepository = teamRepository;
  }

  @Transactional
  public MatchApplicationResponseDto applyToMatch(Long waitingId, MatchApplicationRequestDto requestDto) {

    MatchQueue targetQueue = matchQueueRepository.findById(waitingId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매치 대기열입니다."));

    Team applicantTeam = teamRepository.findById(requestDto.applicantTeamId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

    MatchApplication application = new MatchApplication(
        targetQueue,
        applicantTeam,
        targetQueue.getTeam(),
        requestDto.applicationMessage()
    );

    MatchApplication saved = matchApplicationRepository.save(application);

    return new MatchApplicationResponseDto(
        saved.getApplicationId(),
        saved.getApplicantTeam().getTeamId(),
        saved.getTargetTeam().getTeamId(),
        saved.getApplicationMessage(),
        saved.getStatus()
    );
  }

  @Transactional
  public MatchApplicationResponseDto cancelMatchApplication(Long applicationId) {
    MatchApplication application = matchApplicationRepository.findById(applicationId)
      .orElseThrow(() -> new IllegalArgumentException("해당 신청이 존재하지 않습니다. ID=" + applicationId));

    application.cancelApplication();

    return new MatchApplicationResponseDto(
      application.getApplicationId(),
      application.getApplicantTeam().getTeamId(),
      application.getTargetTeam().getTeamId(),
      application.getApplicationMessage(),
      application.getStatus()
    );
  }


  @Transactional(readOnly = true)
  public Slice<MatchApplicationResponseDto> getReceivedPendingApplications(Long teamId, Pageable pageable) {
    return matchApplicationRepository.findPendingApplicationsByTargetTeam(teamId, pageable)
      .map(ma -> new MatchApplicationResponseDto(
        ma.getApplicationId(),
        ma.getApplicantTeam().getTeamId(),
        ma.getTargetTeam().getTeamId(),
        ma.getApplicationMessage(),
        ma.getStatus()
      ));
  }

  @Transactional
  public MatchConfirmedResponseDto acceptApplication(Long applicationId) {
    MatchApplication application = matchApplicationRepository.findById(applicationId)
      .orElseThrow(() -> new IllegalArgumentException("해당 신청이 없습니다. id=" + applicationId));

    MatchQueue matchQueue = application.getMatchQueue();

    application.updateApplicationStatus(MatchApplicationStatus.ACCEPTED, LocalDateTime.now());
    matchQueue.updateQueueStatus(MatchQueueStatus.MATCHED, LocalDateTime.now());

    matchApplicationRepository.rejectOtherRequests(application.getTargetTeam().getTeamId(), applicationId);

    Match match = new Match(
      application.getTargetTeam(),
      application.getApplicantTeam(),
      matchQueue.getPreferredDate(),
      matchQueue.getPreferredTimeStart(),
      matchQueue.getPreferredVenue(),
      MatchStatus.MATCHED
    );
    matchRepository.save(match);

    return new MatchConfirmedResponseDto(
      match.getMatchId(),
      match.getTeam1().getTeamId(),
      match.getTeam2().getTeamId(),
      match.getMatchDate(),
      match.getMatchTime(),
      match.getVenue().getVenueId(),
      match.getStatus()
    );
  }

  @Transactional
  public MatchApplicationResponseDto rejectApplication(Long applicationId) {
    MatchApplication application = matchApplicationRepository.findById(applicationId)
      .orElseThrow(() -> new IllegalArgumentException("해당 신청이 없습니다. id=" + applicationId));

    application.updateApplicationStatus(MatchApplicationStatus.REJECTED, LocalDateTime.now());

    return new MatchApplicationResponseDto(
      application.getApplicationId(),
      application.getApplicantTeam().getTeamId(),
      application.getTargetTeam().getTeamId(),
      application.getApplicationMessage(),
      application.getStatus()
    );
  }

}
