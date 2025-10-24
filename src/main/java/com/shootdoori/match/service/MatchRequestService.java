package com.shootdoori.match.service;


import com.shootdoori.match.dto.*;
import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.request.MatchRequestStatus;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.match.waiting.MatchWaitingStatus;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.exception.domain.match.OneselfMatchException;
import com.shootdoori.match.repository.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final MatchWaitingRepository matchWaitingRepository;
    private final MatchRepository matchRepository;
    private final MatchCreateService matchCreateService;
    private final TeamMemberService teamMemberService;
    private final MatchEmailService matchEmailService;

    public MatchRequestService(MatchRequestRepository matchRequestRepository,
                               MatchWaitingRepository matchWaitingRepository,
                               MatchRepository matchRepository,
                               MatchCreateService matchCreateService,
                               TeamMemberService teamMemberService,
                               MatchEmailService matchEmailService) {
        this.matchRequestRepository = matchRequestRepository;
        this.matchWaitingRepository = matchWaitingRepository;
        this.matchRepository = matchRepository;
        this.matchCreateService = matchCreateService;
        this.teamMemberService = teamMemberService;
        this.matchEmailService = matchEmailService;
    }

    @Transactional(readOnly = true)
    public Slice<MatchWaitingResponseDto> getWaitingMatches(Long loginUserId,
                                                            MatchWaitingRequestDto matchWaitingRequestDto, Pageable pageable) {
        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        return matchWaitingRepository.findAvailableMatchesByDateCursor
                (teamMember.getTeamId(),
                    matchWaitingRequestDto.selectDate(),
                    matchWaitingRequestDto.startTime(),
                    pageable)
            .map(MatchWaitingResponseDto::from);
    }

    @Transactional
    public MatchRequestResponseDto requestToMatch(Long loginUserId, Long waitingId,
                                                  MatchRequestRequestDto requestDto) {
        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        Team requestTeam = teamMember.getTeam();

        MatchWaiting targetWaiting = matchCreateService.findByIdForEntity(waitingId);

        if (targetWaiting.belongTo(teamMember)) {
            throw new OneselfMatchException();
        }

        boolean alreadyRequested = matchRequestRepository.existsActiveRequest(
            waitingId,
            teamMember.getTeamId(),
            MatchRequestStatus.CANCELED
        );
        if (alreadyRequested) {
            throw new DuplicatedException(ErrorCode.ALREADY_MATCH_REQUEST);
        }

        MatchRequest matchRequest = new MatchRequest(
            targetWaiting,
            requestTeam,
            targetWaiting.getTeam(),
            requestDto.requestMessage()
        );

        MatchRequest saved = matchRequestRepository.save(matchRequest);

        matchEmailService.sendMatchRequestEmail(requestTeam, targetWaiting);

        return MatchRequestResponseDto.from(saved);
    }

    @Transactional
    public MatchRequestResponseDto cancelMatchRequest(Long loginUserId, Long requestId) {
        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        MatchRequest matchRequest = findByIdForEntity(requestId);

        if (!matchRequest.requestBelongTo(teamMember)) {
            throw new NoPermissionException(ErrorCode.MATCH_REQUEST_OWNERSHIP_VIOLATION);
        }

        matchRequest.cancelRequest();

        return MatchRequestResponseDto.from(matchRequest);
    }


    @Transactional(readOnly = true)
    public Slice<MatchRequestResponseDto> getReceivedPendingRequests(Long loginUserId,
                                                                     Pageable pageable) {
        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        return matchRequestRepository.findPendingRequestsByTargetTeam(teamMember.getTeamId(), pageable)
            .map(MatchRequestResponseDto::from);
    }

    @Transactional
    public MatchConfirmedResponseDto acceptRequest(Long loginUserId, Long requestId) {
        MatchRequest matchRequest = findByIdForEntity(requestId);

        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        MatchWaiting matchWaiting = matchRequest.getMatchWaiting();

        if (!matchWaiting.belongTo(teamMember)) {
            throw new NoPermissionException(ErrorCode.MATCH_WAITING_OWNERSHIP_VIOLATION);
        }

        matchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, LocalDateTime.now());
        matchWaiting.updateWaitingStatus(MatchWaitingStatus.MATCHED);

        List<MatchRequest> requestsToReject = matchRequestRepository.findRequestsToReject(
            matchRequest.getTargetTeamId(), requestId, matchWaiting.getWaitingId()
        );

        requestsToReject.forEach(r -> {
            matchEmailService.sendMatchRejectedEmail(r);
            r.updateRequestStatus(MatchRequestStatus.REJECTED, LocalDateTime.now());
        });
        matchRequestRepository.saveAll(requestsToReject);

        Match match = new Match(
            matchRequest.getTargetTeam(),
            matchRequest.getRequestTeam(),
            matchWaiting.getPreferredDate(),
            matchWaiting.getPreferredTimeStart(),
            matchWaiting.getPreferredVenue(),
            MatchStatus.MATCHED
        );
        matchRepository.save(match);

        matchEmailService.sendMatchAcceptEmail(match);

        return MatchConfirmedResponseDto.from(match);
    }

    @Transactional
    public MatchRequestResponseDto rejectRequest(Long loginUserId, Long requestId) {
        MatchRequest matchRequest = findByIdForEntity(requestId);

        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        MatchWaiting matchWaiting = matchRequest.getMatchWaiting();

        if (!matchWaiting.belongTo(teamMember)) {
            throw new NoPermissionException(ErrorCode.MATCH_WAITING_OWNERSHIP_VIOLATION);
        }

        matchRequest.updateRequestStatus(MatchRequestStatus.REJECTED, LocalDateTime.now());

        matchEmailService.sendMatchRejectedEmail(matchRequest);

        return MatchRequestResponseDto.from(matchRequest);
    }

    @Transactional(readOnly = true)
    public Slice<MatchRequestHistoryResponseDto> getSentRequestsByMyTeam(Long loginUserId,
                                                          Pageable pageable) {
        TeamMember teamMember = teamMemberService.findByUserIdForEntity(loginUserId);

        return matchRequestRepository.findSentRequestsByTeam(teamMember.getTeamId(), pageable)
            .map(MatchRequestHistoryResponseDto::from);
    }

    @Transactional(readOnly = true)
    public MatchRequest findByIdForEntity(Long matchRequestId) {
        return matchRequestRepository.findById(matchRequestId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_REQUEST_NOT_FOUND,
                String.valueOf(matchRequestId)));
    }

    @Transactional
    public void cancelAllMatchesByTeamId(Long teamId) {
        matchRequestRepository.cancelAllByTeamId(teamId);
    }
}
