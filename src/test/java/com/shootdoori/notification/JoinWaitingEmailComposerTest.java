package com.shootdoori.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.common.SkillLevel;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.notification.EmailMessage;
import com.shootdoori.match.notification.JoinWaitingEmailComposer;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JoinWaitingEmailComposer 테스트")
public class JoinWaitingEmailComposerTest {

    private JoinWaitingEmailComposer emailComposer;

    private User captainUser;
    private User applicantUser;
    private User viceCaptainUser;
    private Team team;
    private TeamMember captainMember;
    private TeamMember viceCaptainMember;
    private TeamMember approverMember;

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 10, 22, 14, 30);
    private static final String FORMATTED_TIME = "2025-10-22 14:30";

    @BeforeEach
    void setUp() {
        emailComposer = new JoinWaitingEmailComposer();

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

        viceCaptainUser = User.create(
            "부팀장",
            "아마추어",
            "vice@kangwon.ac.kr",
            "Abcd1234!",
            "010-2222-2222",
            "DF",
            "강원대학교",
            "경영학과",
            "21",
            "부팀장입니다."
        );

        ReflectionTestUtils.setField(captainUser, "id", 1L);
        ReflectionTestUtils.setField(applicantUser, "id", 2L);
        ReflectionTestUtils.setField(viceCaptainUser, "id", 3L);

        team = new Team(
            "강원대 FC",
            captainUser,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("세미프로"),
            "주 3회 연습합니다."
        );

        team.addMember(captainUser, TeamMemberRole.LEADER);
        captainMember = team.getTeamMembers().get(0);
        ReflectionTestUtils.setField(captainMember, "id", 1L);

        team.addMember(viceCaptainUser, TeamMemberRole.VICE_LEADER);
        viceCaptainMember = team.getTeamMembers().get(1);
        ReflectionTestUtils.setField(viceCaptainMember, "id", 2L);

        approverMember = captainMember;
    }

    @Nested
    @DisplayName("composeCreated 테스트")
    class ComposeCreatedTest {

        @Test
        @DisplayName("팀원 신청(부회장 1명) 시 - 총 3명에게 메일이 발송된다")
        void create_TeamMember_WithViceCaptain() {
            // given
            String message = "마루는 강쥐 팝업 행사 중";
            boolean isMercenary = false;

            // when
            List<EmailMessage> messages = emailComposer.composeCreated(team, applicantUser, message, isMercenary);

            // then
            assertThat(messages).hasSize(3);

            EmailMessage applicantEmail = findEmailByTo(messages, "applicant@kangwon.ac.kr");
            assertThat(applicantEmail.subject()).isEqualTo("[슛두리] 가입 신청 알림");
            assertThat(applicantEmail.body()).contains(
                "'강원대 FC' 팀에 가입 신청이(가) 완료되었습니다.",
                "신청 메시지: " + message
            );

            EmailMessage captainEmail = findEmailByTo(messages, "captain@kangwon.ac.kr");
            assertThat(captainEmail.subject()).isEqualTo("[슛두리] 가입 신청 알림");
            assertThat(captainEmail.body()).contains(
                "[팀원 신청] 신청자님이 '강원대 FC' 팀에 신청했습니다.",
                "신청 메시지: " + message
            );

            EmailMessage viceCaptainEmail = findEmailByTo(messages, "vice@kangwon.ac.kr");
            assertThat(viceCaptainEmail.subject()).isEqualTo(captainEmail.subject());
            assertThat(viceCaptainEmail.body()).isEqualTo(captainEmail.body());
        }
    }

    @Test
    @DisplayName("용병 신청(부회장 0명) 시 - 총 2명에게 메일이 발송된다")
    void create_Mercenary_NoViceCaptain() {
        // given
        String message = "마루는 강쥐 팝업 행사 중";
        boolean isMercenary = true;

        team.removeMember(viceCaptainMember);

        // when
        List<EmailMessage> emails = emailComposer.composeCreated(team, applicantUser, message, isMercenary);

        // then
        assertThat(emails).hasSize(2);

        EmailMessage applicantEmail = findEmailByTo(emails, "applicant@kangwon.ac.kr");
        assertThat(applicantEmail.subject()).isEqualTo("[슛두리] 용병 신청 알림");
        assertThat(applicantEmail.body()).contains("용병 신청이(가) 완료되었습니다.");

        EmailMessage captainEmail = findEmailByTo(emails, "captain@kangwon.ac.kr");
        assertThat(captainEmail.subject()).isEqualTo("[슛두리] 용병 신청 알림");
        assertThat(captainEmail.body()).contains("[용병 신청] 신청자님이");
    }

    @Nested
    @DisplayName("composeApproved 테스트")
    class ComposeApprovedTest {

        @Test
        @DisplayName("팀원 신청 승인 시 - 신청자 1명에게 발송된다")
        void approve_TeamMember() {
            // given
            boolean isMercenary = false;

            // when
            List<EmailMessage> emails = emailComposer.composeApproved(team, applicantUser, approverMember, FIXED_TIME, isMercenary);

            // then
            assertThat(emails).hasSize(1);

            EmailMessage email = emails.get(0);
            assertThat(email.to()).isEqualTo("applicant@kangwon.ac.kr");
            assertThat(email.subject()).isEqualTo("[슛두리] 가입 신청 승인 알림");
            assertThat(email.body()).contains(
                "'강원대 FC' 팀의 가입 신청이 승인되었습니다.",
                "승인자: 팀장",
                "승인 시간: " + FORMATTED_TIME
            );
        }

        @Test
        @DisplayName("용병 신청 승인 시 - 신청자 1명에게 발송된다")
        void approve_Mercenary() {
            // given
            boolean isMercenary = true;

            // when
            List<EmailMessage> emails = emailComposer.composeApproved(team, applicantUser, approverMember, FIXED_TIME, isMercenary);

            // then
            assertThat(emails).hasSize(1);

            EmailMessage email = emails.get(0);
            assertThat(email.to()).isEqualTo("applicant@kangwon.ac.kr");
            assertThat(email.subject()).isEqualTo("[슛두리] 용병 신청 승인 알림");
            assertThat(email.body()).contains(
                "'강원대 FC' 팀의 용병 신청이 승인되었습니다.",
                "승인자: 팀장",
                "승인 시간: " + FORMATTED_TIME
            );
        }
    }

    @Nested
    @DisplayName("composeRejected 테스트")
    class ComposeRejectedTest {

        @Test
        @DisplayName("팀원 신청 거절 시 - 신청자 1명에게 발송된다")
        void reject_TeamMember() {
            // given
            boolean isMercenary = false;
            String rejectReason = "포지션 중복";

            // when
            List<EmailMessage> emails = emailComposer.composeRejected(team, applicantUser, approverMember, FIXED_TIME, rejectReason, isMercenary);

            // then
            assertThat(emails).hasSize(1);

            EmailMessage email = emails.get(0);
            assertThat(email.to()).isEqualTo("applicant@kangwon.ac.kr");
            assertThat(email.subject()).isEqualTo("[슛두리] 가입 신청 거절 안내");
            assertThat(email.body()).contains(
                "'강원대 FC' 팀의 가입 신청이 거절되었습니다.",
                "거절 사유: " + rejectReason,
                "처리자: 팀장",
                "처리 시간: " + FORMATTED_TIME
            );
        }

        @Test
        @DisplayName("용병 신청 거절 시 - 신청자 1명에게 발송된다")
        void reject_Mercenary() {
            // given
            boolean isMercenary = true;
            String rejectReason = "포지션 중복";

            // when
            List<EmailMessage> emails = emailComposer.composeRejected(team, applicantUser, approverMember, FIXED_TIME, rejectReason, isMercenary);

            // then
            assertThat(emails).hasSize(1);

            EmailMessage email = emails.get(0);
            assertThat(email.to()).isEqualTo("applicant@kangwon.ac.kr");
            assertThat(email.subject()).isEqualTo("[슛두리] 용병 신청 거절 안내");
            assertThat(email.body()).contains(
                "'강원대 FC' 팀의 용병 신청이 거절되었습니다.",
                "거절 사유: " + rejectReason,
                "처리자: 팀장",
                "처리 시간: " + FORMATTED_TIME
            );
        }
    }

    @Nested
    @DisplayName("composeCanceled 테스트")
    class ComposeCanceledTest {

        @Test
        @DisplayName("팀원 신청 취소(부회장 1명) 시 - 총 3명에게 메일이 발송된다")
        void cancel_TeamMember_WithViceCaptain() {
            // given
            String cancelReason = "개인 사정";
            boolean isMercenary = false;

            // when
            List<EmailMessage> emails = emailComposer.composeCanceled(team, applicantUser, FIXED_TIME, cancelReason, isMercenary);

            // then
            assertThat(emails).hasSize(3);

            EmailMessage applicantEmail = findEmailByTo(emails, "applicant@kangwon.ac.kr");
            assertThat(applicantEmail.subject()).isEqualTo("[슛두리] 가입 신청 취소 안내");
            assertThat(applicantEmail.body()).contains(
                "'강원대 FC' 팀 가입 신청을 취소하셨습니다.",
                "취소 사유: " + cancelReason,
                "취소 시간: " + FORMATTED_TIME
            );

            EmailMessage captainEmail = findEmailByTo(emails, "captain@kangwon.ac.kr");
            assertThat(captainEmail.subject()).isEqualTo("[슛두리] 가입 신청 취소 안내");
            assertThat(captainEmail.body()).contains(
                "신청자님이 '강원대 FC' 팀에 대한 팀원 신청을 취소했습니다.",
                "취소 사유: " + cancelReason
            );

            EmailMessage viceCaptainEmail = findEmailByTo(emails, "vice@kangwon.ac.kr");
            assertThat(viceCaptainEmail.body()).isEqualTo(captainEmail.body());
        }

        @Test
        @DisplayName("용병 신청 취소(부회장 1명) 시 - 총 3명에게 메일이 발송된다")
        void cancel_Mercenary_WithViceCaptain() {
            // given
            String cancelReason = "개인 사정";
            boolean isMercenary = true;

            // when
            List<EmailMessage> emails = emailComposer.composeCanceled(team, applicantUser, FIXED_TIME, cancelReason, isMercenary);

            // then
            assertThat(emails).hasSize(3);

            EmailMessage applicantEmail = findEmailByTo(emails, "applicant@kangwon.ac.kr");
            assertThat(applicantEmail.subject()).isEqualTo("[슛두리] 용병 신청 취소 안내");
            assertThat(applicantEmail.body()).contains(
                "'강원대 FC' 팀 용병 신청을 취소하셨습니다.",
                "취소 사유: " + cancelReason,
                "취소 시간: " + FORMATTED_TIME
            );

            EmailMessage captainEmail = findEmailByTo(emails, "captain@kangwon.ac.kr");
            assertThat(captainEmail.subject()).isEqualTo("[슛두리] 용병 신청 취소 안내");
            assertThat(captainEmail.body()).contains(
                "신청자님이 '강원대 FC' 팀에 대한 용병 신청을 취소했습니다.",
                "취소 사유: " + cancelReason
            );

            EmailMessage viceCaptainEmail = findEmailByTo(emails, "vice@kangwon.ac.kr");
            assertThat(viceCaptainEmail.body()).isEqualTo(captainEmail.body());
        }

        @Test
        @DisplayName("팀원 신청 취소(부회장 0명) 시 - 총 2명에게 메일이 발송된다")
        void cancel_TeamMember_NoViceCaptain() {
            // given
            String cancelReason = "개인 사정";
            boolean isMercenary = false;

            team.removeMember(viceCaptainMember);

            // when
            List<EmailMessage> emails = emailComposer.composeCanceled(team, applicantUser, FIXED_TIME, cancelReason, isMercenary);

            // then
            assertThat(emails).hasSize(2);

            EmailMessage applicantEmail = findEmailByTo(emails, "applicant@kangwon.ac.kr");
            assertThat(applicantEmail.subject()).isEqualTo("[슛두리] 가입 신청 취소 안내");
            assertThat(applicantEmail.body()).contains(
                "'강원대 FC' 팀 가입 신청을 취소하셨습니다.",
                "취소 사유: " + cancelReason,
                "취소 시간: " + FORMATTED_TIME
            );

            EmailMessage captainEmail = findEmailByTo(emails, "captain@kangwon.ac.kr");
            assertThat(captainEmail.subject()).isEqualTo("[슛두리] 가입 신청 취소 안내");
            assertThat(captainEmail.body()).contains(
                "신청자님이 '강원대 FC' 팀에 대한 팀원 신청을 취소했습니다.",
                "취소 사유: " + cancelReason
            );
        }

        @Test
        @DisplayName("용병 신청 취소(부회장 0명) 시 - 총 2명에게 메일이 발송된다")
        void cancel_Mercenary_NoViceCaptain() {
            // given
            String cancelReason = "개인 사정";
            boolean isMercenary = true;

            team.removeMember(viceCaptainMember);

            // when
            List<EmailMessage> emails = emailComposer.composeCanceled(team, applicantUser, FIXED_TIME, cancelReason, isMercenary);

            // then
            assertThat(emails).hasSize(2);

            EmailMessage applicantEmail = findEmailByTo(emails, "applicant@kangwon.ac.kr");
            assertThat(applicantEmail.subject()).isEqualTo("[슛두리] 용병 신청 취소 안내");
            assertThat(applicantEmail.body()).contains(
                "'강원대 FC' 팀 용병 신청을 취소하셨습니다.",
                "취소 사유: " + cancelReason,
                "취소 시간: " + FORMATTED_TIME
            );

            EmailMessage captainEmail = findEmailByTo(emails, "captain@kangwon.ac.kr");
            assertThat(captainEmail.subject()).isEqualTo("[슛두리] 용병 신청 취소 안내");
            assertThat(captainEmail.body()).contains(
                "신청자님이 '강원대 FC' 팀에 대한 용병 신청을 취소했습니다.",
                "취소 사유: " + cancelReason
            );
        }
    }

    private EmailMessage findEmailByTo(List<EmailMessage> emails, String emailAddress) {
        return emails.stream()
            .filter(email -> email.to().equals(emailAddress))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Email not sent to: " + emailAddress));
    }
}
