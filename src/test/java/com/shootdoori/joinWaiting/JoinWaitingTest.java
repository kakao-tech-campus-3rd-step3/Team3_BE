package com.shootdoori.joinWaiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shootdoori.match.entity.JoinWaiting;
import com.shootdoori.match.entity.JoinWaitingStatus;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.domain.joinwaiting.JoinWaitingNotPendingException;
import com.shootdoori.match.exception.common.NoPermissionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JoinWaiting 도메인 테스트")
public class JoinWaitingTest {

    private Team team;
    private User teamLeader;
    private User applicant;
    private User anotherUser;
    private TeamMember leaderMember;

    @BeforeEach
    void setUp() {
        teamLeader = User.create(
            "팀리더",
            "세미프로",
            "leader@example.com",
            "leader@kangwon.ac.kr",
            "Abcd1234!",
            "010-1111-1111",
            "미드필더",
            "강원대학교",
            "체육학과",
            "25",
            "팀을 이끌어가는 리더입니다."
        );

        applicant = User.create(
            "신청자",
            "아마추어",
            "applicant@example.com",
            "applicant@kangwon.ac.kr",
            "Abcd1234!",
            "010-2222-2222",
            "공격수",
            "강원대학교",
            "컴퓨터공학과",
            "22",
            "축구를 좋아하는 학생입니다."
        );

        anotherUser = User.create(
            "다른사용자",
            "프로",
            "other@example.com",
            "other@kangwon.ac.kr",
            "Abcd1234!",
            "010-3333-3333",
            "수비수",
            "강원대학교",
            "경영학과",
            "24",
            "저는 그저 다른 사용자입니다."
        );

        ReflectionTestUtils.setField(teamLeader, "id", 1L);
        ReflectionTestUtils.setField(applicant, "id", 2L);
        ReflectionTestUtils.setField(anotherUser, "id", 3L);

        team = new Team(
            "강원대 FC",
            teamLeader,
            "강원대학교",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("세미프로"),
            "주 3회 연습합니다."
        );

        team.recruitMember(teamLeader, TeamMemberRole.LEADER);
        leaderMember = team.getMembers().get(0);
    }

    @Nested
    @DisplayName("JoinWaiting 생성 테스트")
    class CreateJoinWaitingTest {

        @Test
        @DisplayName("정상적으로 가입 신청을 생성한다")
        void createJoinWaiting_success() {
            // when
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "파트라슈처럼 뛰겠습니다!");

            // then
            assertThat(joinWaiting.getTeam()).isEqualTo(team);
            assertThat(joinWaiting.getApplicant()).isEqualTo(applicant);
            assertThat(joinWaiting.getMessage()).isEqualTo("파트라슈처럼 뛰겠습니다!");
            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.PENDING);
            assertThat(joinWaiting.getDecidedBy()).isNull();
            assertThat(joinWaiting.getDecidedAt()).isNull();
            assertThat(joinWaiting.getDecisionReason()).isNull();
        }

        @Test
        @DisplayName("빈 메시지로도 가입 신청을 생성할 수 있다")
        void createJoinWaiting_withEmptyMessage_success() {
            // when
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "");

            // then
            assertThat(joinWaiting.getTeam()).isEqualTo(team);
            assertThat(joinWaiting.getApplicant()).isEqualTo(applicant);
            assertThat(joinWaiting.getMessage()).isEmpty();
            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.PENDING);
        }

        @Test
        @DisplayName("null 메시지로도 가입 신청을 생성할 수 있다")
        void createJoinWaiting_withNullMessage_success() {
            // when
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, null);

            // then
            assertThat(joinWaiting.getTeam()).isEqualTo(team);
            assertThat(joinWaiting.getApplicant()).isEqualTo(applicant);
            assertThat(joinWaiting.getMessage()).isNull();
            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("가입 승인 테스트")
    class ApproveJoinWaitingTest {

        @Test
        @DisplayName("팀 리더가 가입을 승인한다")
        void approve_byLeader_success() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, null);

            // when
            joinWaiting.approve(leaderMember, TeamMemberRole.MEMBER, "환영합니다!!");

            // then
            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.APPROVED);
            assertThat(joinWaiting.getDecisionReason()).isEqualTo("환영합니다!!");
            assertThat(joinWaiting.getDecidedBy()).isEqualTo(teamLeader);
            assertThat(joinWaiting.getDecidedAt()).isNotNull();

            assertThat(team.getMembers()).hasSize(2);
            assertThat(team.getMembers().get(1).getUser()).isEqualTo(applicant);
            assertThat(team.getMembers().get(1).getRole()).isEqualTo(TeamMemberRole.MEMBER);
        }

        @Test
        @DisplayName("이미 처리된 신청을 승인할 때 예외 발생")
        void approve_alreadyProcessed_throwsException() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");
            joinWaiting.approve(leaderMember, TeamMemberRole.MEMBER, "승인 완료");

            // when & then
            assertThatThrownBy(() ->
                joinWaiting.approve(leaderMember, TeamMemberRole.MEMBER, "승인 2번째"))
                .isInstanceOf(JoinWaitingNotPendingException.class);

            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.APPROVED);
        }

        @Test
        @DisplayName("일반 멤버가 승인을 시도하면 권한 예외 발생")
        void approve_byRegularMember_throwsNoPermission() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");
            team.recruitMember(anotherUser, TeamMemberRole.MEMBER);
            TeamMember regularMember = team.getMembers().get(1);

            // when & then
            assertThatThrownBy(() ->
                joinWaiting.approve(regularMember, TeamMemberRole.MEMBER, "승인"))
                .isInstanceOf(NoPermissionException.class);
        }
    }

    @Nested
    @DisplayName("가입 거절 테스트")
    class RejectJoinWaitingTest {

        @Test
        @DisplayName("팀 리더가 가입을 거절한다")
        void reject_byLeader_success() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");

            // when
            joinWaiting.reject(leaderMember, "죄송합니다.");

            // then
            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.REJECTED);
            assertThat(joinWaiting.getDecisionReason()).isEqualTo("죄송합니다.");
            assertThat(joinWaiting.getDecidedBy()).isEqualTo(teamLeader);
            assertThat(joinWaiting.getDecidedAt()).isNotNull();

            assertThat(team.getMembers()).hasSize(1);
        }

        @Test
        @DisplayName("이미 처리된 신청을 거절할 때 예외 발생")
        void reject_alreadyProcessed_throwsException() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");
            joinWaiting.reject(leaderMember, "거절 완료");

            // when & then
            assertThatThrownBy(() ->
                joinWaiting.reject(leaderMember, "거절 2번째"))
                .isInstanceOf(JoinWaitingNotPendingException.class);

            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.REJECTED);
        }

        @Test
        @DisplayName("일반 멤버가 거절을 시도하면 권한 예외 발생")
        void reject_byRegularMember_throwsNoPermission() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");
            team.recruitMember(anotherUser, TeamMemberRole.MEMBER);
            TeamMember regularMember = team.getMembers().get(1);

            // when & then
            assertThatThrownBy(() ->
                joinWaiting.reject(regularMember, "거절"))
                .isInstanceOf(NoPermissionException.class);
        }
    }

    @Nested
    @DisplayName("가입 취소 테스트")
    class CancelJoinWaitingTest {

        @Test
        @DisplayName("신청자가 가입 신청을 취소한다")
        void cancel_byApplicant_success() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");

            // when
            joinWaiting.cancel(applicant, "개인 사정으로 취소합니다.");

            // then
            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.CANCELED);
            assertThat(joinWaiting.getDecisionReason()).isEqualTo("개인 사정으로 취소합니다.");
            assertThat(joinWaiting.getDecidedBy()).isEqualTo(applicant);
            assertThat(joinWaiting.getDecidedAt()).isNotNull();

            assertThat(team.getMembers()).hasSize(1);
        }

        @Test
        @DisplayName("신청자가 아닌 사용자가 취소할 때 예외 발생")
        void cancel_byNonApplicant_throwsException() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");

            // when & then
            assertThatThrownBy(() ->
                joinWaiting.cancel(teamLeader, "개인 사정으로 취소합니다."))
                .isInstanceOf(NoPermissionException.class);
        }

        @Test
        @DisplayName("이미 처리된 신청을 취소할 때 예외 발생")
        void cancel_alreadyProcessed_throwsException() {
            // given
            JoinWaiting joinWaiting = JoinWaiting.create(team, applicant, "가입 요청입니다.");
            joinWaiting.cancel(applicant, "개인 사정으로 취소합니다.");

            // when & then
            assertThatThrownBy(() ->
                joinWaiting.cancel(applicant, "2번째 취소 요청"))
                .isInstanceOf(JoinWaitingNotPendingException.class);

            assertThat(joinWaiting.getStatus()).isEqualTo(JoinWaitingStatus.CANCELED);
        }
    }
}
