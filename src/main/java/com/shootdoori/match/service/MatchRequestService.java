package com.shootdoori.match.service;


import com.shootdoori.match.dto.*;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.exception.MatchRequestNotFoundException;
import com.shootdoori.match.exception.MatchWaitingNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
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
public class MatchRequestService {

  private final MatchRequestRepository matchRequestRepository;
  private final MatchWaitingRepository matchWaitingRepository;
  private final MatchRepository matchRepository;
  private final TeamRepository teamRepository;

  public MatchRequestService(MatchRequestRepository matchRequestRepository,
                             MatchWaitingRepository matchWaitingRepository,
                             MatchRepository matchRepository,
                             TeamRepository teamRepository) {
    this.matchRequestRepository = matchRequestRepository;
    this.matchWaitingRepository = matchWaitingRepository;
    this.matchRepository = matchRepository;
    this.teamRepository = teamRepository;
  }

  @Transactional(readOnly = true)
  public Slice<MatchWaitingResponseDto> getWaitingMatches(MatchWaitingRequestDto matchWaitingRequestDto, Pageable pageable){
    Team searchRequestTeam = teamRepository.findById(matchWaitingRequestDto.teamId())
      .orElseThrow(() -> new TeamNotFoundException(matchWaitingRequestDto.teamId()));

    return matchWaitingRepository.findAvailableMatchesByDateCursor
        (matchWaitingRequestDto.teamId(),
          matchWaitingRequestDto.selectDate(),
          matchWaitingRequestDto.startTime(),
          pageable)
      .map(mw -> new MatchWaitingResponseDto(
        mw.getWaitingId(),
        mw.getTeam().getTeamId(),
        mw.getPreferredDate(),
        mw.getPreferredTimeStart(),
        mw.getPreferredTimeEnd(),
        mw.getPreferredVenue().getVenueId(),
        mw.getSkillLevelMin(),
        mw.getSkillLevelMax(),
        mw.getUniversityOnly(),
        mw.getMessage(),
        mw.getMatchRequestStatus(),
        mw.getExpiresAt()
      ));
  }

  @Transactional
  public MatchRequestResponseDto requestToMatch(Long waitingId, MatchRequestRequestDto requestDto) {

    MatchWaiting targetWaiting = matchWaitingRepository.findById(waitingId)
        .orElseThrow(() -> new MatchWaitingNotFoundException(waitingId));

    Team requestTeam = teamRepository.findById(requestDto.requestTeamId())
        .orElseThrow(() -> new TeamNotFoundException(requestDto.requestTeamId()));

    MatchRequest matchRequest = new MatchRequest(
      targetWaiting,
      requestTeam,
      targetWaiting.getTeam(),
      requestDto.requestMessage()
    );

    MatchRequest saved = matchRequestRepository.save(matchRequest);

    return new MatchRequestResponseDto(
        saved.getRequestId(),
        saved.getRequestTeam().getTeamId(),
        saved.getTargetTeam().getTeamId(),
        saved.getRequestMessage(),
        saved.getStatus()
    );
  }

  @Transactional
  public MatchRequestResponseDto cancelMatchRequest(Long requestId) {
    MatchRequest matchRequest = matchRequestRepository.findById(requestId)
      .orElseThrow(() -> new MatchRequestNotFoundException(requestId));

    matchRequest.cancelRequest();

    return new MatchRequestResponseDto(
      matchRequest.getRequestId(),
      matchRequest.getRequestTeam().getTeamId(),
      matchRequest.getTargetTeam().getTeamId(),
      matchRequest.getRequestMessage(),
      matchRequest.getStatus()
    );
  }


  @Transactional(readOnly = true)
  public Slice<MatchRequestResponseDto> getReceivedPendingRequests(Long teamId, Pageable pageable) {
    Team targetTeam = teamRepository.findById(teamId)
      .orElseThrow(() -> new TeamNotFoundException(teamId));

    return matchRequestRepository.findPendingRequestsByTargetTeam(teamId, pageable)
      .map(mr -> new MatchRequestResponseDto(
        mr.getRequestId(),
        mr.getRequestTeam().getTeamId(),
        mr.getTargetTeam().getTeamId(),
        mr.getRequestMessage(),
        mr.getStatus()
      ));
  }

  @Transactional
  public MatchConfirmedResponseDto acceptRequest(Long requestId) {
    MatchRequest matchRequest = matchRequestRepository.findById(requestId)
      .orElseThrow(() -> new MatchRequestNotFoundException(requestId));

    MatchWaiting matchWaiting = matchRequest.getMatchWaiting();

    matchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, LocalDateTime.now());
    matchWaiting.updateWaitingStatus(MatchWaitingStatus.MATCHED);

    matchRequestRepository.rejectOtherRequests(matchRequest.getTargetTeam().getTeamId(), requestId, matchWaiting.getWaitingId());

    Match match = new Match(
      matchRequest.getTargetTeam(),
      matchRequest.getRequestTeam(),
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
  public MatchRequestResponseDto rejectRequest(Long requestId) {
    MatchRequest matchRequest = matchRequestRepository.findById(requestId)
      .orElseThrow(() -> new MatchRequestNotFoundException(requestId));

    matchRequest.updateRequestStatus(MatchRequestStatus.REJECTED, LocalDateTime.now());

    return new MatchRequestResponseDto(
      matchRequest.getRequestId(),
      matchRequest.getRequestTeam().getTeamId(),
      matchRequest.getTargetTeam().getTeamId(),
      matchRequest.getRequestMessage(),
      matchRequest.getStatus()
    );
  }

}
