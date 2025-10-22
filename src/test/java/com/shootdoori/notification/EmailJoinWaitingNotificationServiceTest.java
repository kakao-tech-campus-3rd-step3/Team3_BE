package com.shootdoori.notification;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.notification.EmailMessage;
import com.shootdoori.match.notification.JoinWaitingEmailComposer;
import com.shootdoori.match.service.EmailJoinWaitingNotificationService;
import com.shootdoori.match.service.MailService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailJoinWaitingNotificationService 단위 테스트")
public class EmailJoinWaitingNotificationServiceTest {

    private EmailJoinWaitingNotificationService notificationService;

    @Mock
    private MailService mailService;

    @Mock
    private JoinWaitingEmailComposer emailComposer;

    private User captainUser;
    private Team team;
    private User applicantUser;
    private TeamMember approver;

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 10, 22, 14, 30);

    @BeforeEach
    void setUp() {
        notificationService = new EmailJoinWaitingNotificationService(mailService, emailComposer);

        captainUser = User.create(
            "팀장",
            "세미프로",
            "captain@kangwon.ac.kr",
            "Abcd1234!",
            "010-0000-0000",
            "MF",
            "강원대학교",
            "체육학과",
            "20",
            "팀장입니다."
        );

        applicantUser = User.create(
            "신청자",
            "아마추어",
            "applicant@kangwon.ac.kr",
            "Abcd1234!",
            "010-1111-1111",
            "FW",
            "강원대학교",
            "컴퓨터공학과",
            "22",
            "열심히 뛰겠습니다."
        );

        team = new Team(
            "강원대 FC",
            captainUser,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("세미프로"),
            "주 3회 연습합니다."
        );

        team.addMember(captainUser, TeamMemberRole.LEADER);
        approver = team.getTeamMembers().get(0);
    }

    @Nested
    @DisplayName("sendJoinCreateNotification 테스트")
    class SendJoinCreateNotificationTest {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("isMercenary 값에 따라 올바른 인자로 이메일 생성을 요청하고 발송한다")
        void sendJoinCreateNotification_Success(boolean isMercenary) {
            // given
            String message = "가입 신청합니다.";

            EmailMessage emailToApplicant = new EmailMessage("applicant@test.com", "Subject 1",
                "Body 1");
            EmailMessage emailToCaptain = new EmailMessage("captain@test.com", "Subject 1",
                "Body 2");
            List<EmailMessage> messages = List.of(emailToCaptain, emailToApplicant);

            when(emailComposer.composeCreated(team, applicantUser, message, isMercenary))
                .thenReturn(messages);

            // when
            notificationService.sendJoinCreateNotification(team, applicantUser, message,
                isMercenary);

            // then
            verify(emailComposer, times(1)).composeCreated(
                team,
                applicantUser,
                message,
                isMercenary
            );

            verify(mailService, times(1)).sendEmail(emailToApplicant);
            verify(mailService, times(1)).sendEmail(emailToCaptain);
        }
    }

    @Nested
    @DisplayName("sendJoinApprovalNotification 테스트")
    class SendJoinApprovalNotificationTest {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("isMercenary 값에 따라 승인 알림을 발송한다")
        void sendJoinApprovalNotification_Success(boolean isMercenary) {
            // given
            EmailMessage emailToApplicant = new EmailMessage("applicant@test.com", "Subject 1",
                "Body 1");
            List<EmailMessage> messages = List.of(emailToApplicant);

            when(emailComposer.composeApproved(team, applicantUser, approver, FIXED_TIME,
                isMercenary))
                .thenReturn(messages);

            // when
            notificationService.sendJoinApprovalNotification(team, applicantUser, approver,
                FIXED_TIME, isMercenary);

            // then
            verify(emailComposer, times(1)).composeApproved(team, applicantUser, approver,
                FIXED_TIME, isMercenary);

            verify(mailService, times(1)).sendEmail(emailToApplicant);
        }
    }

    @Nested
    @DisplayName("sendJoinRejectionNotification 테스트")
    class SendJoinRejectionNotificationTest {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("isMercenary 값에 따라 거절 알림을 발송한다")
        void sendJoinRejectionNotification_Success(boolean isMercenary) {
            // given
            String rejectReason = "죄송합니다";

            EmailMessage emailToApplicant = new EmailMessage("applicant@test.com", "Subject 1",
                "Body 1");
            List<EmailMessage> messages = List.of(emailToApplicant);

            when(emailComposer.composeRejected(team, applicantUser, approver, FIXED_TIME,
                rejectReason, isMercenary))
                .thenReturn(messages);

            // when
            notificationService.sendJoinRejectionNotification(team, applicantUser, approver,
                FIXED_TIME, rejectReason, isMercenary);

            // then
            verify(emailComposer, times(1)).composeRejected(team, applicantUser, approver,
                FIXED_TIME, rejectReason, isMercenary);

            verify(mailService, times(1)).sendEmail(emailToApplicant);
        }
    }

    @Nested
    @DisplayName("sendJoinCancelNotification 테스트")
    class SendJoinCancelNotificationTest {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("isMercenary 값에 따라 취소 알림을 발송한다")
        void sendJoinCancelNotification_Success(boolean isMercenary) {
            // given
            String cancelReason = "개인 사정으로 취소입니다.";

            EmailMessage emailToApplicant = new EmailMessage("applicant@test.com", "Subject 1",
                "Body 1");
            EmailMessage emailToCaptain = new EmailMessage("captain@test.com", "Subject 1",
                "Body 2");
            List<EmailMessage> messages = List.of(emailToApplicant, emailToCaptain);

            when(emailComposer.composeCanceled(team, applicantUser, FIXED_TIME, cancelReason,
                isMercenary))
                .thenReturn(messages);

            // when
            notificationService.sendJoinCancelNotification(team, applicantUser, FIXED_TIME,
                cancelReason, isMercenary);

            // then
            verify(emailComposer, times(1)).composeCanceled(team, applicantUser, FIXED_TIME,
                cancelReason, isMercenary);

            verify(mailService, times(1)).sendEmail(emailToApplicant);
            verify(mailService, times(1)).sendEmail(emailToCaptain);
        }
    }
}
