package com.shootdoori.match.service;

import com.shootdoori.match.dto.JoinQueueApproveRequestDto;
import com.shootdoori.match.dto.JoinQueueCancelRequestDto;
import com.shootdoori.match.dto.JoinQueueMapper;
import com.shootdoori.match.dto.JoinQueueRejectRequestDto;
import com.shootdoori.match.dto.JoinQueueRequestDto;
import com.shootdoori.match.dto.JoinQueueResponseDto;
import com.shootdoori.match.entity.JoinQueue;
import com.shootdoori.match.entity.JoinQueueStatus;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.AlreadyTeamMemberException;
import com.shootdoori.match.exception.DuplicatePendingJoinQueueException;
import com.shootdoori.match.exception.JoinQueueNotFoundException;
import com.shootdoori.match.exception.TeamMemberNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.exception.UserNotFoundException;
import com.shootdoori.match.repository.JoinQueueRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JoinQueueService {

    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JoinQueueRepository joinQueueRepository;
    private final JoinQueueMapper joinQueueMapper;

    public JoinQueueService(ProfileRepository profileRepository, TeamRepository teamRepository,
        TeamMemberRepository teamMemberRepository,
        JoinQueueRepository joinQueueRepository, JoinQueueMapper joinQueueMapper) {
        this.profileRepository = profileRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.joinQueueRepository = joinQueueRepository;
        this.joinQueueMapper = joinQueueMapper;
    }

    @Transactional
    public JoinQueueResponseDto create(Long teamId, JoinQueueRequestDto requestDto) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new TeamNotFoundException(teamId));

        Long applicantId = requestDto.applicantId();
        User applicant = profileRepository.findById(applicantId)
            .orElseThrow(() -> new UserNotFoundException(applicantId));

        team.validateSameUniversity(applicant);

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, applicantId)) {
            throw new AlreadyTeamMemberException();
        }

        if (joinQueueRepository.existsByTeam_TeamIdAndApplicant_IdAndStatus(teamId, applicantId,
            JoinQueueStatus.PENDING)) {
            throw new DuplicatePendingJoinQueueException();
        }

        JoinQueue joinQueue = JoinQueue.create(team, applicant, requestDto.message());

        JoinQueue savedJoinQueue = joinQueueRepository.save(joinQueue);

        return joinQueueMapper.toJoinQueueResponseDto(savedJoinQueue);
    }

    @Transactional
    public JoinQueueResponseDto approve(Long teamId, Long joinQueueId,
        JoinQueueApproveRequestDto requestDto) {

        TeamMemberRole role = TeamMemberRole.fromDisplayName(requestDto.role());

        Long approverId = requestDto.approverId();
        TeamMember approver = teamMemberRepository.findByIdAndTeam_TeamId(approverId, teamId)
            .orElseThrow(() -> new TeamMemberNotFoundException());

        JoinQueue joinQueue = joinQueueRepository
            .findByIdAndTeam_TeamIdForUpdate(joinQueueId, teamId)
            .orElseThrow(() -> new JoinQueueNotFoundException());

        Team team = joinQueue.getTeam();
        User applicant = joinQueue.getApplicant();

        if (teamMemberRepository.existsByTeam_TeamIdAndUser_Id(teamId, applicant.getId())) {
            throw new AlreadyTeamMemberException();
        }

        team.validateSameUniversity(joinQueue.getApplicant());
        team.validateCanAcceptNewMember();

        joinQueue.approve(approver, role, requestDto.decisionReason());

        return joinQueueMapper.toJoinQueueResponseDto(joinQueue);
    }

    @Transactional
    public JoinQueueResponseDto reject(Long teamId, Long joinQueueId,
        JoinQueueRejectRequestDto requestDto) {

        Long approverId = requestDto.approverId();
        TeamMember approver = teamMemberRepository.findByIdAndTeam_TeamId(approverId, teamId)
            .orElseThrow(() -> new TeamMemberNotFoundException());

        JoinQueue joinQueue = joinQueueRepository
            .findByIdAndTeam_TeamIdForUpdate(joinQueueId, teamId)
            .orElseThrow(() -> new JoinQueueNotFoundException());

        Team team = joinQueue.getTeam();
        User applicant = joinQueue.getApplicant();

        joinQueue.reject(approver, requestDto.reason());

        return joinQueueMapper.toJoinQueueResponseDto(joinQueue);
    }

    @Transactional
    public JoinQueueResponseDto cancel(Long teamId, Long joinQueueId,
        JoinQueueCancelRequestDto requestDto) {

        Long requesterId = requestDto.requesterId();
        User requester = profileRepository.findById(requesterId)
            .orElseThrow(() -> new UserNotFoundException(requesterId));

        JoinQueue joinQueue = joinQueueRepository.findByIdAndTeam_TeamIdForUpdate(joinQueueId,
                teamId)
            .orElseThrow(() -> new JoinQueueNotFoundException());

        joinQueue.cancel(requester, requestDto.decisionReason());

        return joinQueueMapper.toJoinQueueResponseDto(joinQueue);
    }

    @Transactional(readOnly = true)
    public Page<JoinQueueResponseDto> findPending(Long teamId, JoinQueueStatus status,
        Pageable pageable) {

        return joinQueueRepository.findAllByTeam_TeamIdAndStatus(teamId, status, pageable)
            .map(joinQueueMapper::toJoinQueueResponseDto);
    }
}
