package com.shootdoori.match.service;


import com.shootdoori.match.dto.MatchApplicationRequestDto;
import com.shootdoori.match.dto.MatchApplicationResponseDto;
import com.shootdoori.match.dto.MatchConfirmedResponseDto;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.repository.MatchRequestRepository;
import com.shootdoori.match.repository.MatchWaitingRepository;
import com.shootdoori.match.repository.MatchRepository;
import com.shootdoori.match.repository.TeamRepository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchApplicationService {

  private final MatchRequestRepository matchRequestRepository;
  private final MatchWaitingRepository matchWaitingRepository;
  private final MatchRepository matchRepository;
  private final TeamRepository teamRepository;

  public MatchApplicationService(MatchRequestRepository matchRequestRepository,
                                 MatchWaitingRepository matchWaitingRepository,
                                 MatchRepository matchRepository,
                                 TeamRepository teamRepository) {
    this.matchRequestRepository = matchRequestRepository;
    this.matchWaitingRepository = matchWaitingRepository;
    this.matchRepository = matchRepository;
    this.teamRepository = teamRepository;
  }

  @Transactional
  public MatchApplicationResponseDto applyToMatch(Long waitingId, MatchApplicationRequestDto requestDto) {

    MatchWaiting targetQueue = matchWaitingRepository.findById(waitingId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매치 대기열입니다."));

    Team applicantTeam = teamRepository.findById(requestDto.applicantTeamId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

    MatchRequest application = new MatchRequest(
        targetQueue,
        applicantTeam,
        targetQueue.getTeam(),
        requestDto.applicationMessage()
    );

    MatchRequest saved = matchRequestRepository.save(application);

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
    MatchRequest application = matchRequestRepository.findById(applicationId)
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
    return matchRequestRepository.findPendingApplicationsByTargetTeam(teamId, pageable)
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
    MatchRequest application = matchRequestRepository.findById(applicationId)
      .orElseThrow(() -> new IllegalArgumentException("해당 신청이 없습니다. id=" + applicationId));

    MatchWaiting matchWaiting = application.getMatchWaiting();

    application.updateRequestStatus(MatchRequestStatus.ACCEPTED, LocalDateTime.now());
    matchWaiting.updateQueueStatus(MatchWaitingStatus.MATCHED, LocalDateTime.now());

    matchRequestRepository.rejectOtherRequests(application.getTargetTeam().getTeamId(), applicationId);

    Match match = new Match(
      application.getTargetTeam(),
      application.getRequestTeam(),
      matchWaiting.getPreferredDate(),
      matchWaiting.getPreferredTimeStart(),
      matchWaiting.getPreferredVenue(),
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
    MatchRequest application = matchRequestRepository.findById(applicationId)
      .orElseThrow(() -> new IllegalArgumentException("해당 신청이 없습니다. id=" + applicationId));

    application.updateRequestStatus(MatchRequestStatus.REJECTED, LocalDateTime.now());

    return new MatchApplicationResponseDto(
      application.getApplicationId(),
      application.getApplicantTeam().getTeamId(),
      application.getTargetTeam().getTeamId(),
      application.getApplicationMessage(),
      application.getStatus()
    );
  }

}
