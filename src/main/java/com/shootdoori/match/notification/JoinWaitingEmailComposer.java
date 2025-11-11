package com.shootdoori.match.notification;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.user.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class JoinWaitingEmailComposer {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm");

    private static final String SUBJECT_PREFIX = "[슛두리] ";

    private static final String STAFF_CREATED_BODY_TEMPLATE = """
        안녕하세요!
        
        [%s] %s님이 '%s' 팀에 신청했습니다.
        
        신청 메시지: %s
        
        슛두리 앱에서 가입 승인/거절을 처리해주세요.""";

    private static final String APPLICANT_CREATED_BODY_TEMPLATE = """
        안녕하세요!
        
        '%s' 팀에 %s이(가) 완료되었습니다.
        
        신청 메시지: %s
        
        승인/거절 결과는 이메일로 안내드리겠습니다.""";

    private static final String APPLICANT_APPROVED_BODY_TEMPLATE = """
        축하합니다!
        
        '%s' 팀의 %s이 승인되었습니다.
        
        승인자: %s
        승인 시간: %s
        
        이제 팀 활동에 참여할 수 있습니다.""";

    private static final String APPLICANT_REJECTED_BODY_TEMPLATE = """
        안녕하세요.
        
        '%s' 팀의 %s이 거절되었습니다.
        
        거절 사유: %s
        처리자: %s
        처리 시간: %s
        """;

    private static final String STAFF_CANCELED_BODY_TEMPLATE = """
        안녕하세요!
        
        %s님이 '%s' 팀에 대한 %s을 취소했습니다.
        
        취소 사유: %s
        취소 시간: %s
        
        더 이상 가입 승인/거절 처리가 필요하지 않습니다.""";

    private static final String APPLICANT_CANCELED_BODY_TEMPLATE = """
        안녕하세요!
        
        '%s' 팀 %s을 취소하셨습니다.
        
        취소 사유: %s
        취소 시간: %s
        """;

    public List<EmailMessage> composeCreated(Team team, User applicant, String message,
        boolean isMercenary) {
        String staffType = getStaffType(isMercenary);
        String applicantType = getApplicantType(isMercenary);
        String subject = SUBJECT_PREFIX + applicantType + " 알림";

        String staffBody = String.format(
            STAFF_CREATED_BODY_TEMPLATE,
            staffType, applicant.getName(), team.getTeamName().name(), message
        );

        String applicantBody = String.format(
            APPLICANT_CREATED_BODY_TEMPLATE,
            team.getTeamName().name(), applicantType, message
        );

        return composeApplicantAndStaffMessages(team, applicant, subject, applicantBody, staffBody);
    }

    public List<EmailMessage> composeApproved(Team team, User applicant, TeamMember approver,
        LocalDateTime decidedAt, boolean isMercenary) {
        String type = getApplicantType(isMercenary);
        String subject = SUBJECT_PREFIX + type + " 승인 알림";

        String applicantEmail = applicant.getEmail();
        String applicantBody = String.format(
            APPLICANT_APPROVED_BODY_TEMPLATE,
            team.getTeamName().name(),
            type,
            approver.getUser().getName(),
            decidedAt.format(dateTimeFormatter)
        );

        return List.of(createEmail(applicantEmail, subject, applicantBody));
    }

    public List<EmailMessage> composeRejected(Team team, User applicant, TeamMember approver,
        LocalDateTime decidedAt, String rejectReason, boolean isMercenary) {
        String type = getApplicantType(isMercenary);
        String subject = SUBJECT_PREFIX + type + " 거절 안내";

        String applicantEmail = applicant.getEmail();
        String applicantBody = String.format(
            APPLICANT_REJECTED_BODY_TEMPLATE,
            team.getTeamName().name(),
            type,
            rejectReason,
            approver.getUser().getName(),
            decidedAt.format(dateTimeFormatter)
        );

        return List.of(createEmail(applicantEmail, subject, applicantBody));
    }

    public List<EmailMessage> composeCanceled(Team team, User applicant, LocalDateTime decidedAt,
        String cancelReason, boolean isMercenary) {
        String staffType = getStaffType(isMercenary);
        String applicantType = getApplicantType(isMercenary);
        String subject = SUBJECT_PREFIX + applicantType + " 취소 안내";

        String staffBody = String.format(
            STAFF_CANCELED_BODY_TEMPLATE,
            applicant.getName(),
            team.getTeamName().name(),
            staffType,
            cancelReason,
            decidedAt.format(dateTimeFormatter)
        );

        String applicantBody = String.format(
            APPLICANT_CANCELED_BODY_TEMPLATE,
            team.getTeamName().name(),
            applicantType,
            cancelReason,
            decidedAt.format(dateTimeFormatter)
        );

        return composeApplicantAndStaffMessages(team, applicant, subject, applicantBody, staffBody);
    }

    private List<EmailMessage> composeApplicantAndStaffMessages(Team team, User applicant,
        String subject, String applicantBody, String staffBody) {
        List<EmailMessage> messages = new ArrayList<>();

        messages.add(createEmail(applicant.getEmail(), subject, applicantBody));

        messages.addAll(createStaffMessages(team, subject, staffBody));

        return messages;
    }

    private List<EmailMessage> createStaffMessages(Team team, String subject, String staffBody) {
        List<EmailMessage> staffMessages = new ArrayList<>();

        staffMessages.add(createEmail(team.getCaptain().getEmail(), subject, staffBody));

        viceCaptains(team)
            .map(viceCaptain -> createEmail(viceCaptain.getUser().getEmail(), subject, staffBody))
            .forEach(staffMessages::add);

        return staffMessages;
    }

    private EmailMessage createEmail(String to, String subject, String body) {
        return new EmailMessage(to, subject, body);
    }

    private static Stream<TeamMember> viceCaptains(Team team) {
        return team.getTeamMembers().stream()
            .filter(TeamMember::isViceCaptain);
    }

    private String getStaffType(boolean isMercenary) {
        return isMercenary ? "용병 신청" : "팀원 신청";
    }

    private String getApplicantType(boolean isMercenary) {
        return isMercenary ? "용병 신청" : "가입 신청";
    }
}
