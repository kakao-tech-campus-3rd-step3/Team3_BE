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
import com.shootdoori.match.exception.JoinWaitingNotFoundException;
import com.shootdoori.match.exception.TeamMemberNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.exception.UserNotFoundException;
import com.shootdoori.match.repository.JoinWaitingRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
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
    public JoinWaitingResponseDto create(Long teamId, JoinWaitingRequestDto requestDto) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new TeamNotFoundException(teamId));

        Long applicantId = requestDto.applicantId();
        User applicant = profileRepository.findById(applicantId)
            .orElseThrow(() -> new UserNotFoundException(applicantId));

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
        JoinWaitingApproveRequestDto requestDto) {

        TeamMemberRole role = TeamMemberRole.fromDisplayName(requestDto.role());

        Long approverId = requestDto.approverId();
        TeamMember approver = teamMemberRepository.findByIdAndTeam_TeamId(approverId, teamId)
            .orElseThrow(() -> new TeamMemberNotFoundException());

        JoinWaiting joinWaiting = joinWaitingRepository
            .findByIdAndTeam_TeamIdForUpdate(joinWaitingId, teamId)
            .orElseThrow(() -> new JoinWaitingNotFoundException());

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
        JoinWaitingRejectRequestDto requestDto) {

        Long approverId = requestDto.approverId();
        TeamMember approver = teamMemberRepository.findByIdAndTeam_TeamId(approverId, teamId)
            .orElseThrow(() -> new TeamMemberNotFoundException());

        JoinWaiting joinWaiting = joinWaitingRepository
            .findByIdAndTeam_TeamIdForUpdate(joinWaitingId, teamId)
            .orElseThrow(() -> new JoinWaitingNotFoundException());

        Team team = joinWaiting.getTeam();
        User applicant = joinWaiting.getApplicant();

        joinWaiting.reject(approver, requestDto.reason());

        return joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting);
    }

    @Transactional
    public JoinWaitingResponseDto cancel(Long teamId, Long joinWaitingId,
        JoinWaitingCancelRequestDto requestDto) {

        Long requesterId = requestDto.requesterId();
        User requester = profileRepository.findById(requesterId)
            .orElseThrow(() -> new UserNotFoundException(requesterId));

        JoinWaiting joinWaiting = joinWaitingRepository.findByIdAndTeam_TeamIdForUpdate(joinWaitingId,
                teamId)
            .orElseThrow(() -> new JoinWaitingNotFoundException());

        joinWaiting.cancel(requester, requestDto.decisionReason());

        return joinWaitingMapper.toJoinWaitingResponseDto(joinWaiting);
    }

    @Transactional(readOnly = true)
    public Page<JoinWaitingResponseDto> findPending(Long teamId, JoinWaitingStatus status,
        Pageable pageable) {

        return joinWaitingRepository.findAllByTeam_TeamIdAndStatus(teamId, status, pageable)
            .map(joinWaitingMapper::toJoinWaitingResponseDto);
    }
}
