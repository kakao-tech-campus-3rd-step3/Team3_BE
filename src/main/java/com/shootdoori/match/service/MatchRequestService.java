package com.shootdoori.match.service;


import com.shootdoori.match.dto.*;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.exception.NoPermissionException;
import com.shootdoori.match.exception.NotFoundException;
import com.shootdoori.match.exception.ErrorCode;
import com.shootdoori.match.exception.OneselfMatchException;
import com.shootdoori.match.repository.*;

import java.time.LocalDateTime;
import java.util.Objects;

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
  private final TeamMemberRepository teamMemberRepository;

  public MatchRequestService(MatchRequestRepository matchRequestRepository,
                             MatchWaitingRepository matchWaitingRepository,
                             MatchRepository matchRepository,
                             TeamRepository teamRepository,
                             TeamMemberRepository teamMemberRepository) {
    this.matchRequestRepository = matchRequestRepository;
    this.matchWaitingRepository = matchWaitingRepository;
    this.matchRepository = matchRepository;
    this.teamRepository = teamRepository;
    this.teamMemberRepository = teamMemberRepository;
  }

  @Transactional(readOnly = true)
  public Slice<MatchWaitingResponseDto> getWaitingMatches(Long loginUserId, MatchWaitingRequestDto matchWaitingRequestDto, Pageable pageable){
    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Team team = teamMember.getTeam();

    Long myTeamId = team.getTeamId();

    return matchWaitingRepository.findAvailableMatchesByDateCursor
        (myTeamId,
          matchWaitingRequestDto.selectDate(),
          matchWaitingRequestDto.startTime(),
          pageable)
      .map(mw -> new MatchWaitingResponseDto(
        mw.getWaitingId(),
        mw.getTeam().getTeamId(),
        mw.getTeam().getTeamName(),
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
  public MatchRequestResponseDto requestToMatch(Long loginUserId, Long waitingId, MatchRequestRequestDto requestDto) {
    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Team requestTeam = teamMember.getTeam();

    Long requestTeamId = requestTeam.getTeamId();

    MatchWaiting targetWaiting = matchWaitingRepository.findById(waitingId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_WAITING_NOT_FOUND, String.valueOf(waitingId)));

    Long targetTeamId = targetWaiting.getTeam().getTeamId();

    if(targetTeamId.equals(requestTeamId)){
      throw new OneselfMatchException();
    }

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
        saved.getRequestTeam().getTeamName(),
        saved.getTargetTeam().getTeamId(),
        saved.getTargetTeam().getTeamName(),
        saved.getRequestMessage(),
        saved.getStatus()
    );
  }

  @Transactional
  public MatchRequestResponseDto cancelMatchRequest(Long loginUserId, Long requestId) {
    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Team cancelRequestTeam = teamMember.getTeam();

    Long cancelRequestTeamId = cancelRequestTeam.getTeamId();

    MatchRequest matchRequest = matchRequestRepository.findById(requestId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_REQUEST_NOT_FOUND, String.valueOf(requestId)));

    Long existMatchRequestedTeamId = matchRequest.getRequestTeam().getTeamId();

    if(cancelRequestTeamId.longValue() != existMatchRequestedTeamId.longValue()){
      throw new NoPermissionException();
    }

    matchRequest.cancelRequest();

    return new MatchRequestResponseDto(
      matchRequest.getRequestId(),
      matchRequest.getRequestTeam().getTeamId(),
      matchRequest.getRequestTeam().getTeamName(),
      matchRequest.getTargetTeam().getTeamId(),
      matchRequest.getTargetTeam().getTeamName(),
      matchRequest.getRequestMessage(),
      matchRequest.getStatus()
    );
  }


  @Transactional(readOnly = true)
  public Slice<MatchRequestResponseDto> getReceivedPendingRequests(Long loginUserId, Pageable pageable) {
    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Team myTeam = teamMember.getTeam();

    Long myTeamId = myTeam.getTeamId();

    return matchRequestRepository.findPendingRequestsByTargetTeam(myTeamId, pageable)
      .map(mr -> new MatchRequestResponseDto(
        mr.getRequestId(),
        mr.getRequestTeam().getTeamId(),
        mr.getRequestTeam().getTeamName(),
        mr.getTargetTeam().getTeamId(),
        mr.getTargetTeam().getTeamName(),
        mr.getRequestMessage(),
        mr.getStatus()
      ));
  }

  @Transactional
  public MatchConfirmedResponseDto acceptRequest(Long loginUserId, Long requestId) {
    MatchRequest matchRequest = matchRequestRepository.findById(requestId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_REQUEST_NOT_FOUND, String.valueOf(requestId)));

    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Team determineTeam = teamMember.getTeam();

    Long determineTeamId = determineTeam.getTeamId();

    MatchWaiting matchWaiting = matchRequest.getMatchWaiting();
    Long waitingTeamId = matchWaiting.getTeam().getTeamId();

    if(determineTeamId.longValue() != waitingTeamId.longValue()){
      throw new NoPermissionException();
    }

    matchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, LocalDateTime.now());
    matchWaiting.updateWaitingStatus(MatchWaitingStatus.MATCHED);

    matchRequestRepository.rejectOtherRequests(matchRequest.getTargetTeam().getTeamId(), requestId, matchWaiting.getWaitingId());

    Match match = new Match(
      matchRequest.getTargetTeam(),
      matchRequest.getRequestTeam(),
      matchWaiting.getPreferredDate(),
      matchWaiting.getPreferredTimeStart(),
      matchWaiting.getPreferredVenue(),
      MatchStatus.FINISHED // FE 연동 위한 잠정 변경 MATCHED->FINISHED
    );
    matchRepository.save(match);

    return new MatchConfirmedResponseDto(
      match.getMatchId(),
      match.getTeam1().getTeamId(),
      match.getTeam1().getTeamName(),
      match.getTeam2().getTeamId(),
      match.getTeam2().getTeamName(),
      match.getMatchDate(),
      match.getMatchTime(),
      match.getVenue().getVenueId(),
      match.getStatus()
    );
  }

  @Transactional
  public MatchRequestResponseDto rejectRequest(Long loginUserId, Long requestId) {
    MatchRequest matchRequest = matchRequestRepository.findById(requestId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_REQUEST_NOT_FOUND, String.valueOf(requestId)));

    TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
      .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

    Team determineTeam = teamMember.getTeam();

    Long determineTeamId = determineTeam.getTeamId();

    MatchWaiting matchWaiting = matchRequest.getMatchWaiting();
    Long waitingTeamId = matchWaiting.getTeam().getTeamId();

    if(determineTeamId.longValue() != waitingTeamId.longValue()){
      throw new NoPermissionException();
    }

    matchRequest.updateRequestStatus(MatchRequestStatus.REJECTED, LocalDateTime.now());

    return new MatchRequestResponseDto(
      matchRequest.getRequestId(),
      matchRequest.getRequestTeam().getTeamId(),
      matchRequest.getRequestTeam().getTeamName(),
      matchRequest.getTargetTeam().getTeamId(),
      matchRequest.getTargetTeam().getTeamName(),
      matchRequest.getRequestMessage(),
      matchRequest.getStatus()
    );
  }

}
