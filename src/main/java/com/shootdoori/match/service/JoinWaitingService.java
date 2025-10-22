package com.shootdoori.match.service;

import com.shootdoori.match.dto.JoinWaitingApproveRequestDto;
import com.shootdoori.match.dto.JoinWaitingCancelRequestDto;
import com.shootdoori.match.dto.JoinWaitingMapper;
import com.shootdoori.match.dto.JoinWaitingRejectRequestDto;
import com.shootdoori.match.dto.JoinWaitingRequestDto;
import com.shootdoori.match.dto.JoinWaitingResponseDto;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.join.JoinWaiting;
import com.shootdoori.match.entity.team.join.JoinWaitingStatus;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.JoinWaitingRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JoinWaitingService {

    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JoinWaitingRepository joinWaitingRepository;
    private final JoinWaitingMapper joinWaitingMapper;
    private final EmailJoinWaitingNotificationService notificationService;

    public JoinWaitingService(ProfileRepository profileRepository, TeamRepository teamRepository,
        TeamMemberRepository teamMemberRepository,
        JoinWaitingRepository joinWaitingRepository, JoinWaitingMapper joinWaitingMapper,
        EmailJoinWaitingNotificationService notificationService) {
        this.profileRepository = profileRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.joinWaitingRepository = joinWaitingRepository;
        this.joinWaitingMapper = joinWaitingMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public JoinWaitingResponseDto create(Long teamId, Long applicantId,
        JoinWaitingRequestDto requestDto) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        User applicant = profileRepository.findById(applicantId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.USER_NOT_FOUND, String.valueOf(applicantId)));

        String message = requestDto.message();
        boolean isMercenary = requestDto.isMercenary();

        team.validateSameUniversityAs(applicant);

        if (teamMemberRepository.existsByUser_Id(applicantId)) {
            throw new DuplicatedException(ErrorCode.ALREADY_OTHER_TEAM_MEMBER);
        }

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, applicantId)) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }

        if (joinWaitingRepository.existsByTeam_TeamIdAndApplicant_IdAndStatus(teamId, applicantId,
            JoinWaitingStatus.PENDING)) {
            throw new DuplicatedException(ErrorCode.JOIN_WAITING_ALREADY_PENDING);
        }

        JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, message, isMercenary);

        JoinWaiting savedJoinWaiting = joinWaitingRepository.save(joinWaiting);

        notificationService.sendJoinCreateNotification(team, applicant, message,
            isMercenary);

        return joinWaitingMapper.toJoinWaitingResponseDto(savedJoinWaiting);
    }

    @Transactional
    public JoinWaitingResponseDto approve(Long teamId, Long joinWaitingId,
        Long loginUserId, JoinWaitingApproveRequestDto requestDto) {

        String approveReason = requestDto.decisionReason();

        TeamMember approver = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        JoinWaiting joinWaiting = joinWaitingRepository
            .findByIdAndTeam_TeamIdForUpdate(joinWaitingId, teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.JOIN_WAITING_NOT_FOUND));

        TeamMemberRole role = TeamMemberRole.determineRole(joinWaiting, requestDto.role());

        Team team = joinWaiting.getTeam();
        User applicant = joinWaiting.getApplicant();

        if (teamMemberRepository.existsByUser_Id(applicant.getId())) {
            throw new DuplicatedException(ErrorCode.ALREADY_OTHER_TEAM_MEMBER);
        }

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, applicant.getId())) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }

        team.validateSameUniversityAs(joinWaiting.getApplicant());
        team.ensureCapacityAvailable();

        joinWaiting.approve(approver, role, approveReason);

        notificationService.sendJoinApprovalNotification(team, applicant, approver,
            joinWaiting.getDecidedAt(), joinWaiting.isMercenary());

        return joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting);
    }

    @Transactional
    public JoinWaitingResponseDto reject(Long teamId, Long joinWaitingId,
        Long loginUserId, JoinWaitingRejectRequestDto requestDto) {
        String rejectReason = requestDto.decisionReason();

        TeamMember approver = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        JoinWaiting joinWaiting = joinWaitingRepository
            .findByIdAndTeam_TeamIdForUpdate(joinWaitingId, teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.JOIN_WAITING_NOT_FOUND));

        Team team = joinWaiting.getTeam();
        User applicant = joinWaiting.getApplicant();

        joinWaiting.reject(approver, rejectReason);

        notificationService.sendJoinRejectionNotification(team, applicant, approver,
            joinWaiting.getDecidedAt(), rejectReason, joinWaiting.isMercenary());

        return joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting);
    }

    @Transactional
    public JoinWaitingResponseDto cancel(Long teamId, Long joinWaitingId,
        Long requesterId, JoinWaitingCancelRequestDto requestDto) {

        String cancelReason = requestDto.decisionReason();

        User requester = profileRepository.findById(requesterId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.USER_NOT_FOUND, String.valueOf(requesterId)));

        JoinWaiting joinWaiting = joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(
                joinWaitingId,
                teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.JOIN_WAITING_NOT_FOUND));

        Team team = joinWaiting.getTeam();
        User applicant = joinWaiting.getApplicant();

        joinWaiting.cancel(requester, cancelReason);

        notificationService.sendJoinCancelNotification(team, applicant,
            joinWaiting.getDecidedAt(), cancelReason, joinWaiting.isMercenary());

        return joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting);
    }

    @Transactional(readOnly = true)
    public Page<JoinWaitingResponseDto> findPending(Long teamId, JoinWaitingStatus status,
        Pageable pageable) {

        teamRepository.findById(teamId).orElseThrow(() ->
            new NotFoundException(ErrorCode.TEAM_NOT_FOUND));

        return joinWaitingRepository.findAllByTeam_TeamIdAndStatus(teamId, status, pageable)
            .map(joinWaitingMapper::toJoinWaitingResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<JoinWaitingResponseDto> findAllByApplicantIdAndStatusIn(Long applicantId,
        Pageable pageable) {

        profileRepository.findById(applicantId).orElseThrow(() ->
            new NotFoundException(ErrorCode.USER_NOT_FOUND));

        List<JoinWaitingStatus> targetStatuses = List.of(JoinWaitingStatus.PENDING,
            JoinWaitingStatus.REJECTED);

        return joinWaitingRepository.findAllByApplicant_IdAndStatusIn(applicantId, targetStatuses,
                pageable)
            .map(joinWaitingMapper::toJoinWaitingResponseDto);
    }
}
