package com.shootdoori.match.service;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.notification.JoinWaitingEmailComposer;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class EmailJoinWaitingNotificationService {

    private final MailService mailService;
    private final JoinWaitingEmailComposer emailComposer;

    public EmailJoinWaitingNotificationService(MailService mailService,
        JoinWaitingEmailComposer emailComposer) {
        this.mailService = mailService;
        this.emailComposer = emailComposer;
    }

    public void sendJoinCreateNotification(Team team, User applicant, String message,
        boolean isMercenary) {

        emailComposer.composeCreated(team, applicant, message, isMercenary)
            .forEach(mailService::sendEmail);
    }

    public void sendJoinApprovalNotification(Team team, User applicant, TeamMember approver,
        LocalDateTime decidedAt, boolean isMercenary) {

        emailComposer.composeApproved(team, applicant, approver, decidedAt, isMercenary)
            .forEach(mailService::sendEmail);
    }

    public void sendJoinRejectionNotification(Team team, User applicant, TeamMember approver,
        LocalDateTime decidedAt, String rejectReason, boolean isMercenary) {

        emailComposer.composeRejected(team, applicant, approver, decidedAt, rejectReason,
                isMercenary)
            .forEach(mailService::sendEmail);
    }

    public void sendJoinCancelNotification(Team team, User applicant, LocalDateTime decidedAt,
        String cancelReason, boolean isMercenary) {

        emailComposer.composeCanceled(team, applicant, decidedAt, cancelReason, isMercenary)
            .forEach(mailService::sendEmail);
    }
}
