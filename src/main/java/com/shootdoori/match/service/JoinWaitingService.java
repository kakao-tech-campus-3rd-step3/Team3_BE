package com.shootdoori.match.service;

import com.shootdoori.match.dto.JoinWaitingApproveRequestDto;
import com.shootdoori.match.dto.JoinWaitingCancelRequestDto;
import com.shootdoori.match.dto.JoinWaitingMapper;
import com.shootdoori.match.dto.JoinWaitingRejectRequestDto;
import com.shootdoori.match.dto.JoinWaitingRequestDto;
import com.shootdoori.match.dto.JoinWaitingResponseDto;
import com.shootdoori.match.entity.JoinWaiting;
import com.shootdoori.match.entity.JoinWaitingStatus;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedException;
import com.shootdoori.match.exception.ErrorCode;
import com.shootdoori.match.exception.NotFoundException;
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

    public JoinWaitingService(ProfileRepository profileRepository, TeamRepository teamRepository,
        TeamMemberRepository teamMemberRepository,
        JoinWaitingRepository joinWaitingRepository, JoinWaitingMapper joinWaitingMapper) {
        this.profileRepository = profileRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.joinWaitingRepository = joinWaitingRepository;
        this.joinWaitingMapper = joinWaitingMapper;
    }

    @Transactional
    public JoinWaitingResponseDto create(Long teamId, Long applicantId, JoinWaitingRequestDto requestDto) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND, String.valueOf(teamId)));

        User applicant = profileRepository.findById(applicantId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, String.valueOf(applicantId)));

        team.validateSameUniversity(applicant);

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, applicantId)) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }

        if (joinWaitingRepository.existsByTeam_TeamIdAndApplicant_IdAndStatus(teamId, applicantId,
            JoinWaitingStatus.PENDING)) {
            throw new DuplicatedException(ErrorCode.JOIN_WAITING_ALREADY_PENDING);
        }

        JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, requestDto.message());

        JoinWaiting savedJoinWaiting = joinWaitingRepository.save(joinWaiting);

        return joinWaitingMapper.toJoinWaitingResponseDto(savedJoinWaiting);
    }

    @Transactional
    public JoinWaitingResponseDto approve(Long teamId, Long joinWaitingId,
        Long loginUserId, JoinWaitingApproveRequestDto requestDto) {

        TeamMember approver = teamMemberRepository.findByUser_Id(loginUserId)
          .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Long approverId = approver.getId();

        TeamMemberRole role = TeamMemberRole.fromDisplayName(requestDto.role());

        JoinWaiting joinWaiting = joinWaitingRepository
            .findByIdAndTeam_TeamIdForUpdate(joinWaitingId, teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.JOIN_WAITING_NOT_FOUND));

        Team team = joinWaiting.getTeam();
        User applicant = joinWaiting.getApplicant();

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, applicant.getId())) {
            throw new DuplicatedException(ErrorCode.ALREADY_TEAM_MEMBER);
        }

        team.validateSameUniversity(joinWaiting.getApplicant());
        team.validateCanAcceptNewMember();

        joinWaiting.approve(approver, role, requestDto.decisionReason());

        return joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting);
    }

    @Transactional
    public JoinWaitingResponseDto reject(Long teamId, Long joinWaitingId,
        Long loginUserId, JoinWaitingRejectRequestDto requestDto) {

        TeamMember approver = teamMemberRepository.findByUser_Id(loginUserId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        JoinWaiting joinWaiting = joinWaitingRepository
            .findByIdAndTeam_TeamIdForUpdate(joinWaitingId, teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.JOIN_WAITING_NOT_FOUND));

        joinWaiting.reject(approver, requestDto.reason());

        return joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting);
    }

    @Transactional
    public JoinWaitingResponseDto cancel(Long teamId, Long joinWaitingId,
        Long requesterId, JoinWaitingCancelRequestDto requestDto) {

        User requester = profileRepository.findById(requesterId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, String.valueOf(requesterId)));

        JoinWaiting joinWaiting = joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(joinWaitingId,
                teamId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.JOIN_WAITING_NOT_FOUND));

        joinWaiting.cancel(requester, requestDto.decisionReason());

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
    public Page<JoinWaitingResponseDto> findAllByApplicant_IdAndStatusIn(Long applicantId,
        Pageable pageable) {

        profileRepository.findById(applicantId).orElseThrow(() ->
            new NotFoundException(ErrorCode.USER_NOT_FOUND));

        List<JoinWaitingStatus> targetStatuses = List.of(JoinWaitingStatus.PENDING, JoinWaitingStatus.REJECTED);
        
        return joinWaitingRepository.findAllByApplicant_IdAndStatusIn(applicantId, targetStatuses, pageable)
            .map(joinWaitingMapper::toJoinWaitingResponseDto);
    }
}
