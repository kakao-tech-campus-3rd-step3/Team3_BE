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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final MailService mailService;

    public JoinWaitingService(ProfileRepository profileRepository, TeamRepository teamRepository,
        TeamMemberRepository teamMemberRepository,
        JoinWaitingRepository joinWaitingRepository, JoinWaitingMapper joinWaitingMapper,
        MailService mailService) {
        this.profileRepository = profileRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.joinWaitingRepository = joinWaitingRepository;
        this.joinWaitingMapper = joinWaitingMapper;
        this.mailService = mailService;
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

        sendJoinCreateNotification(team, applicant, message, isMercenary);

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

        TeamMemberRole role = joinWaiting.isMercenary()
            ? TeamMemberRole.MERCENARY
            : TeamMemberRole.fromDisplayName(requestDto.role());

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

        sendJoinApprovalNotification(team, applicant, approver, joinWaiting.getDecidedAt(),
            joinWaiting.isMercenary());

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

        sendJoinRejectionNotification(team, applicant, approver, joinWaiting.getDecidedAt(),
            rejectReason);

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

        sendJoinCancelNotification(team, applicant, joinWaiting.getDecidedAt(), cancelReason);

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

    private void sendJoinCreateNotification(Team team, User applicant, String message,
        boolean isMercenary) {

        String type = isMercenary ? "용병 신청" : "팀원 가입";
        String subject = "[슛두리] " + type + " 알림";

        String captainText = String.format(
            "안녕하세요!\n\n" +
                "[%s] %s님이 '%s' 팀에 신청했습니다.\n\n" +
                "신청 메시지: %s\n\n" +
                "슛두리 앱에서 가입 승인/거절을 처리해주세요.",
            type, applicant.getName(), team.getTeamName().name(), message
        );

        String applicantText = String.format(
            "안녕하세요!\n\n" +
                "'%s' 팀에 %s이(가) 완료되었습니다.\n\n" +
                "신청 메시지: %s\n\n" +
                "승인/거절 결과는 이메일로 안내드리겠습니다.",
            team.getTeamName().name(), isMercenary ? "용병 신청" : "가입 신청", message
        );

        mailService.sendEmail(team.getCaptain().getEmail(), subject, captainText);

        team.getTeamMembers().stream()
            .filter(TeamMember::isViceCaptain)
            .findFirst()
            .ifPresent(viceCaptain -> {
                String viceCaptainText = String.format(
                    "안녕하세요!\n\n" +
                        "[%s] %s님이 '%s' 팀에 신청했습니다.\n\n" +
                        "신청 메시지: %s\n\n" +
                        "슛두리 앱에서 가입 승인/거절을 처리해주세요.",
                    type, applicant.getName(), team.getTeamName().name(), message
                );

                mailService.sendEmail(viceCaptain.getUser().getEmail(), subject, viceCaptainText);
            });

        mailService.sendEmail(applicant.getEmail(), subject, applicantText);
    }

    private void sendJoinApprovalNotification(Team team, User applicant, TeamMember approver,
        LocalDateTime decidedAt, boolean isMercenary) {
        String type = isMercenary ? "용병" : "팀원";
        String subject = "[슛두리] " + type + " 신청 승인 알림";

        String text = String.format(
            "축하합니다!\n\n" +
                "'%s' 팀의 %s 신청이 승인되었습니다.\n\n" +
                "승인자: %s\n" +
                "승인 시간: %s\n\n" +
                "이제 팀 활동에 참여할 수 있습니다.",
            team.getTeamName().name(),
            type,
            approver.getUser().getName(),
            decidedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        mailService.sendEmail(applicant.getEmail(), subject, text);
    }

    private void sendJoinRejectionNotification(Team team, User applicant, TeamMember approver,
        LocalDateTime decidedAt, String rejectReason) {
        String subject = "[슛두리] 팀 가입 거절 안내";

        String text = String.format(
            "안녕하세요.\n\n" +
                "'%s' 팀의 가입 신청이 거절되었습니다.\n\n" +
                "거절 사유: %s\n" +
                "처리자: %s\n" +
                "처리 시간: %s\n\n" +
                "아쉽지만 현재 팀 사정상 함께하기 어려울 것 같습니다. 다른 팀에 지원을 검토해 주시면 감사하겠습니다.",
            team.getTeamName().name(),
            rejectReason,
            approver.getUser().getName(),
            decidedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        mailService.sendEmail(applicant.getEmail(), subject, text);
    }

    private void sendJoinCancelNotification(Team team, User applicant, LocalDateTime decidedAt,
        String cancelReason) {
        String subject = "[슛두리] 팀 가입 신청 취소 안내";

        String captainText = String.format(
            "안녕하세요!\n\n" +
                "%s님이 '%s' 팀에 대한 가입 신청을 취소했습니다.\n\n" +
                "취소 사유: %s\n" +
                "취소 시간: %s\n\n" +
                "더 이상 가입 승인/거절 처리가 필요하지 않습니다.",
            applicant.getName(),
            team.getTeamName().name(),
            cancelReason,
            decidedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        String applicantText = String.format(
            "안녕하세요!\n\n" +
                "'%s' 팀 가입 신청을 취소하셨습니다.\n\n" +
                "취소 사유: %s\n" +
                "취소 시간: %s\n\n",
            team.getTeamName().name(),
            cancelReason,
            decidedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        mailService.sendEmail(team.getCaptain().getEmail(), subject, captainText);

        team.getTeamMembers().stream()
            .filter(TeamMember::isViceCaptain)
            .findFirst()
            .ifPresent(viceCaptain -> {
                mailService.sendEmail(viceCaptain.getUser().getEmail(), subject, captainText);
            });

        mailService.sendEmail(applicant.getEmail(), subject, applicantText);
    }
}
