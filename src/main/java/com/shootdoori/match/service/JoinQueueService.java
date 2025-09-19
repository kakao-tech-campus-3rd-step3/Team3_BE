package com.shootdoori.match.service;

import com.shootdoori.match.dto.JoinQueueMapper;
import com.shootdoori.match.dto.JoinQueueRequestDto;
import com.shootdoori.match.dto.JoinQueueResponseDto;
import com.shootdoori.match.entity.JoinQueue;
import com.shootdoori.match.entity.JoinQueueStatus;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.AlreadyTeamMemberException;
import com.shootdoori.match.exception.DuplicatePendingJoinQueueException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.exception.UserNotFoundException;
import com.shootdoori.match.repository.JoinQueueRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import java.util.Queue;
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
}
