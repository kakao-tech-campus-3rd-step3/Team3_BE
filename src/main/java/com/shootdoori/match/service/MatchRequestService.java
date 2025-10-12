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
import com.shootdoori.match.value.TeamName;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    public Slice<MatchWaitingResponseDto> getWaitingMatches(Long loginUserId,
                                                            MatchWaitingRequestDto matchWaitingRequestDto, Pageable pageable) {
        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team team = teamMember.getTeam();

        Long myTeamId = team.getTeamId();

        return matchWaitingRepository.findAvailableMatchesByDateCursor
                (myTeamId,
                    matchWaitingRequestDto.selectDate(),
                    matchWaitingRequestDto.startTime(),
                    pageable)
            .map(MatchWaitingResponseDto::from);
    }

    @Transactional
    public MatchRequestResponseDto requestToMatch(Long loginUserId, Long waitingId,
                                                  MatchRequestRequestDto requestDto) {
        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team requestTeam = teamMember.getTeam();

        Long requestTeamId = requestTeam.getTeamId();

        MatchWaiting targetWaiting = matchWaitingRepository.findById(waitingId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_WAITING_NOT_FOUND,
                String.valueOf(waitingId)));

        Long targetTeamId = targetWaiting.getTeam().getTeamId();

        if (targetTeamId.equals(requestTeamId)) {
            throw new OneselfMatchException();
        }

        boolean alreadyRequested = matchRequestRepository.existsActiveRequest(
            waitingId,
            requestTeamId,
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

        return MatchRequestResponseDto.from(saved);
    }

    @Transactional
    public MatchRequestResponseDto cancelMatchRequest(Long loginUserId, Long requestId) {
        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team cancelRequestTeam = teamMember.getTeam();

        Long cancelRequestTeamId = cancelRequestTeam.getTeamId();

        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_REQUEST_NOT_FOUND,
                String.valueOf(requestId)));

        Long existMatchRequestedTeamId = matchRequest.getRequestTeam().getTeamId();

        if (cancelRequestTeamId.longValue() != existMatchRequestedTeamId.longValue()) {
            throw new NoPermissionException(ErrorCode.MATCH_REQUEST_OWNERSHIP_VIOLATION);
        }

        matchRequest.cancelRequest();

        return MatchRequestResponseDto.from(matchRequest);
    }


    @Transactional(readOnly = true)
    public Slice<MatchRequestResponseDto> getReceivedPendingRequests(Long loginUserId,
                                                                     Pageable pageable) {
        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team myTeam = teamMember.getTeam();

        Long myTeamId = myTeam.getTeamId();

        return matchRequestRepository.findPendingRequestsByTargetTeam(myTeamId, pageable)
            .map(MatchRequestResponseDto::from);
    }

    @Transactional
    public MatchConfirmedResponseDto acceptRequest(Long loginUserId, Long requestId) {
        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_REQUEST_NOT_FOUND,
                String.valueOf(requestId)));

        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team determineTeam = teamMember.getTeam();
        Long determineTeamId = determineTeam.getTeamId();

        MatchWaiting matchWaiting = matchRequest.getMatchWaiting();
        Long waitingTeamId = matchWaiting.getTeam().getTeamId();

        if (!determineTeamId.equals(waitingTeamId)) {
            throw new NoPermissionException(ErrorCode.MATCH_WAITING_OWNERSHIP_VIOLATION);
        }

        Team targetTeam = matchRequest.getTargetTeam();
        Team requestTeam = matchRequest.getRequestTeam();
        TeamName targetTeamName = targetTeam.getTeamName();
        TeamName requestTeamName = requestTeam.getTeamName();

        matchRequest.updateRequestStatus(MatchRequestStatus.ACCEPTED, LocalDateTime.now());
        matchWaiting.updateWaitingStatus(MatchWaitingStatus.MATCHED);

        matchRequestRepository.rejectOtherRequests(targetTeam.getTeamId(), requestId,
            matchWaiting.getWaitingId());

        Match match = new Match(
            targetTeam,
            requestTeam,
            matchWaiting.getPreferredDate(),
            matchWaiting.getPreferredTimeStart(),
            matchWaiting.getPreferredVenue(),
            MatchStatus.FINISHED // 임시 변경
        );
        matchRepository.save(match);

        return MatchConfirmedResponseDto.from(match);
    }

    @Transactional
    public MatchRequestResponseDto rejectRequest(Long loginUserId, Long requestId) {
        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATCH_REQUEST_NOT_FOUND,
                String.valueOf(requestId)));

        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team determineTeam = teamMember.getTeam();

        Long determineTeamId = determineTeam.getTeamId();

        MatchWaiting matchWaiting = matchRequest.getMatchWaiting();
        Long waitingTeamId = matchWaiting.getTeam().getTeamId();

        if (determineTeamId.longValue() != waitingTeamId.longValue()) {
            throw new NoPermissionException(ErrorCode.MATCH_WAITING_OWNERSHIP_VIOLATION);
        }

        matchRequest.updateRequestStatus(MatchRequestStatus.REJECTED, LocalDateTime.now());

        return MatchRequestResponseDto.from(matchRequest);
    }

    @Transactional(readOnly = true)
    public Slice<MatchRequestHistoryResponseDto> getSentRequestsByMyTeam(Long loginUserId,
                                                          Pageable pageable) {
        TeamMember teamMember = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Long myTeamId = teamMember.getTeam().getTeamId();

        return matchRequestRepository.findSentRequestsByTeam(myTeamId, pageable)
            .map(MatchRequestHistoryResponseDto::from);
    }

    @Transactional
    public void deleteAllByTeamId(Long teamId) {
        matchRequestRepository.deleteAllByTeamId(teamId);
    }
}
