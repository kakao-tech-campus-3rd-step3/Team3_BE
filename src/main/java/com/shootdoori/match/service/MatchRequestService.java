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
    private final MailService mailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public MatchRequestService(MatchRequestRepository matchRequestRepository,
                               MatchWaitingRepository matchWaitingRepository,
                               MatchRepository matchRepository,
                               MatchCreateService matchCreateService,
                               TeamMemberService teamMemberService,
                               MailService mailService) {
        this.matchRequestRepository = matchRequestRepository;
        this.matchWaitingRepository = matchWaitingRepository;
        this.matchRepository = matchRepository;
        this.matchCreateService = matchCreateService;
        this.teamMemberService = teamMemberService;
        this.mailService = mailService;
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

        sendMatchRequestEmail(requestTeam, targetWaiting);

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
            sendMatchRejectedEmail(r);
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

        sendMatchAcceptEmail(match);

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

        sendMatchRejectedEmail(matchRequest);

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
    public void deleteAllByTeamId(Long teamId) {
        matchRequestRepository.deleteAllByTeamId(teamId);
    }

    private void sendMatchRequestEmail(Team requestTeam, MatchWaiting targetWaiting) {
        String receiverEmail = targetWaiting.getTeam().getCaptain().getEmail();

        String formattedDate = targetWaiting.getPreferredDate().format(DATE_FORMATTER);
        String formattedStart = targetWaiting.getPreferredTimeStart().format(TIME_FORMATTER);
        String formattedEnd = targetWaiting.getPreferredTimeEnd().format(TIME_FORMATTER);

        String subject = String.format("[슛두리 매치 신청] %s 팀이 매치를 신청했습니다!", requestTeam.getTeamName());
        String content = String.format(
            "%s 팀이 %s %s~%s에 매치를 신청했습니다.\n\n" +
                "경기 장소: %s\n\n" ,
            requestTeam.getTeamName(),
            formattedDate,
            formattedStart,
            formattedEnd,
            targetWaiting.getPreferredVenue()
        );

        mailService.sendEmail(receiverEmail, subject, content);
    }

    private void sendMatchAcceptEmail(Match match) {
        String receiverEmail = match.getMatchRequestTeam().getCaptain().getEmail();

        String formattedDate = match.getMatchDate().format(DATE_FORMATTER);
        String formattedStart = match.getMatchTime().format(TIME_FORMATTER);

        String subject = String.format("[슛두리 매치 수락] %s 팀이 매치를 수락했습니다!", match.getMatchCreateTeam().getTeamName());
        String content = String.format(
            "%s 팀이 매치를 수락했습니다!\n\n" +
                "경기 일정: %s %s\n" +
                "경기 장소: %s\n\n" +
                "매치가 확정되었습니다. 즐거운 경기 되세요!",
            match.getMatchCreateTeam().getTeamName(),
            formattedDate,
            formattedStart,
            match.getVenue()
        );

        mailService.sendEmail(receiverEmail, subject, content);
    }

    private void sendMatchRejectedEmail(MatchRequest rejectedRequest) {
        String receiverEmail = rejectedRequest.getRequestTeam().getCaptain().getEmail();

        MatchWaiting waiting = rejectedRequest.getMatchWaiting();
        String formattedDate = waiting.getPreferredDate().format(DATE_FORMATTER);
        String formattedStart = waiting.getPreferredTimeStart().format(TIME_FORMATTER);
        String formattedEnd = waiting.getPreferredTimeEnd().format(TIME_FORMATTER);

        String subject = String.format("[슛두리 매치 거절] %s 팀에 대한 매치 요청이 거절되었습니다.",
            rejectedRequest.getMatchWaiting().getTeam().getTeamName());

        String content = String.format(
            "안녕하세요, 슛두리입니다.\n\n" +
                "아쉽게도 %s %s~%s에 신청하신 매치는 다른 팀과 확정되어 거절되었습니다.\n\n" +
                "다른 매치를 다시 신청해보세요!\n\n" +
                "감사합니다.",
            formattedDate,
            formattedStart,
            formattedEnd
        );

        mailService.sendEmail(receiverEmail, subject, content);
    }

    @Transactional
    public void cancelAllMatchesByTeamId(Long teamId) {
        matchRequestRepository.cancelAllByTeamId(teamId);
    }
}
