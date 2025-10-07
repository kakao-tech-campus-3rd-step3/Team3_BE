package com.shootdoori.profile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.repository.PasswordOtpTokenRepository;
import com.shootdoori.match.repository.PasswordResetTokenRepository;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.service.TeamMemberService;
import com.shootdoori.match.service.UserCleanupService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCleanupServiceTest {

    @Mock private PasswordOtpTokenRepository passwordOtpTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private TeamMemberService teamMemberService;
    @Mock private ProfileRepository profileRepository;

    @InjectMocks private UserCleanupService userCleanupService;

    @Nested
    @DisplayName("단일 사용자 삭제")
    class SingleUserDeletion {

        @Test
        @DisplayName("팀에 소속되지 않은 사용자 삭제 성공")
        void permanentlyDeleteUsers_Success_NoTeam() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            verify(passwordOtpTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(passwordResetTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(teamMemberRepository, times(1)).findByUser_Id(userId);
            verify(teamMemberService, never()).leave(any(), any());
            verify(profileRepository, times(1)).deleteById(userId);
        }

        @Test
        @DisplayName("팀에 소속된 사용자 삭제 성공 - 팀 탈퇴 포함")
        void permanentlyDeleteUsers_Success_WithTeam() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            Long teamId = 10L;
            Team team = mock(Team.class);
            TeamMember teamMember = mock(TeamMember.class);

            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.of(teamMember));
            given(teamMember.getTeam()).willReturn(team);
            given(team.getTeamId()).willReturn(teamId);

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            InOrder inOrder = inOrder(
                    passwordOtpTokenRepository,
                    passwordResetTokenRepository,
                    refreshTokenRepository,
                    teamMemberRepository,
                    teamMemberService,
                    profileRepository
            );

            inOrder.verify(passwordOtpTokenRepository).deleteAllByUserId(userId);
            inOrder.verify(passwordResetTokenRepository).deleteAllByUserId(userId);
            inOrder.verify(refreshTokenRepository).deleteAllByUserId(userId);
            inOrder.verify(teamMemberRepository).findByUser_Id(userId);
            inOrder.verify(teamMemberService).leave(teamId, userId);
            inOrder.verify(profileRepository).deleteById(userId);
        }

        @Test
        @DisplayName("모든 토큰 삭제가 올바른 순서로 실행됨")
        void permanentlyDeleteUsers_TokenDeletionOrder() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            InOrder inOrder = inOrder(
                    passwordOtpTokenRepository,
                    passwordResetTokenRepository,
                    refreshTokenRepository
            );

            inOrder.verify(passwordOtpTokenRepository).deleteAllByUserId(userId);
            inOrder.verify(passwordResetTokenRepository).deleteAllByUserId(userId);
            inOrder.verify(refreshTokenRepository).deleteAllByUserId(userId);
        }
    }

    @Nested
    @DisplayName("다중 사용자 삭제")
    class MultipleUsersDeletion {

        @Test
        @DisplayName("여러 사용자 일괄 삭제 성공")
        void permanentlyDeleteUsers_Success_MultipleUsers() {
            // given
            User user1 = createUserWithId(1L);
            User user2 = createUserWithId(2L);
            Long userId1 = 1L;
            Long userId2 = 2L;
            List<User> users = Arrays.asList(user1, user2);

            given(teamMemberRepository.findByUser_Id(userId1)).willReturn(Optional.empty());
            given(teamMemberRepository.findByUser_Id(userId2)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(users);

            // then
            verify(passwordOtpTokenRepository, times(1)).deleteAllByUserId(userId1);
            verify(passwordOtpTokenRepository, times(1)).deleteAllByUserId(userId2);
            verify(passwordResetTokenRepository, times(1)).deleteAllByUserId(userId1);
            verify(passwordResetTokenRepository, times(1)).deleteAllByUserId(userId2);
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId1);
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId2);
            verify(profileRepository, times(1)).deleteById(userId1);
            verify(profileRepository, times(1)).deleteById(userId2);
        }

        @Test
        @DisplayName("팀 소속 여부가 다른 여러 사용자 삭제 성공")
        void permanentlyDeleteUsers_Success_MixedTeamMembership() {
            // given
            User user1 = createUserWithId(1L);
            User user2 = createUserWithId(2L);
            Long userId1 = 1L;
            Long userId2 = 2L;
            Long teamId = 10L;
            List<User> users = Arrays.asList(user1, user2);
            Team team = mock(Team.class);
            TeamMember teamMember = mock(TeamMember.class);

            given(teamMemberRepository.findByUser_Id(userId1)).willReturn(Optional.of(teamMember));
            given(teamMember.getTeam()).willReturn(team);
            given(team.getTeamId()).willReturn(teamId);
            given(teamMemberRepository.findByUser_Id(userId2)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(users);

            // then
            verify(teamMemberService, times(1)).leave(teamId, userId1);
            verify(teamMemberService, never()).leave(any(), eq(userId2));
            verify(profileRepository, times(1)).deleteById(userId1);
            verify(profileRepository, times(1)).deleteById(userId2);
        }
    }

    @Nested
    @DisplayName("삭제 검증")
    class DeletionVerification {

        @Test
        @DisplayName("모든 토큰이 실제로 삭제되었는지 count로 검증")
        void permanentlyDeleteUsers_VerifyTokensDeletion() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());
            given(passwordOtpTokenRepository.countByUserId(userId)).willReturn(0L);
            given(passwordResetTokenRepository.countByUserId(userId)).willReturn(0L);
            given(refreshTokenRepository.countByUserId(userId)).willReturn(0L);

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            verify(passwordOtpTokenRepository).deleteAllByUserId(userId);
            verify(passwordResetTokenRepository).deleteAllByUserId(userId);
            verify(refreshTokenRepository).deleteAllByUserId(userId);

            // 삭제 후 count 검증
            assertThat(passwordOtpTokenRepository.countByUserId(userId)).isEqualTo(0L);
            assertThat(passwordResetTokenRepository.countByUserId(userId)).isEqualTo(0L);
            assertThat(refreshTokenRepository.countByUserId(userId)).isEqualTo(0L);
        }

        @Test
        @DisplayName("프로필이 실제로 삭제되었는지 검증")
        void permanentlyDeleteUsers_VerifyProfileDeletion() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());
            given(profileRepository.findById(userId)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            verify(profileRepository).deleteById(userId);
            assertThat(profileRepository.findById(userId)).isEmpty();
        }

        @Test
        @DisplayName("여러 사용자의 모든 데이터가 삭제되었는지 검증")
        void permanentlyDeleteUsers_VerifyMultipleUsersDeletion() {
            // given
            User user1 = createUserWithId(1L);
            User user2 = createUserWithId(2L);
            Long userId1 = 1L;
            Long userId2 = 2L;
            List<User> users = Arrays.asList(user1, user2);

            given(teamMemberRepository.findByUser_Id(userId1)).willReturn(Optional.empty());
            given(teamMemberRepository.findByUser_Id(userId2)).willReturn(Optional.empty());
            given(passwordOtpTokenRepository.countByUserId(userId1)).willReturn(0L);
            given(passwordOtpTokenRepository.countByUserId(userId2)).willReturn(0L);
            given(passwordResetTokenRepository.countByUserId(userId1)).willReturn(0L);
            given(passwordResetTokenRepository.countByUserId(userId2)).willReturn(0L);
            given(refreshTokenRepository.countByUserId(userId1)).willReturn(0L);
            given(refreshTokenRepository.countByUserId(userId2)).willReturn(0L);

            // when
            userCleanupService.permanentlyDeleteUsers(users);

            // then
            assertThat(passwordOtpTokenRepository.countByUserId(userId1)).isEqualTo(0L);
            assertThat(passwordOtpTokenRepository.countByUserId(userId2)).isEqualTo(0L);
            assertThat(passwordResetTokenRepository.countByUserId(userId1)).isEqualTo(0L);
            assertThat(passwordResetTokenRepository.countByUserId(userId2)).isEqualTo(0L);
            assertThat(refreshTokenRepository.countByUserId(userId1)).isEqualTo(0L);
            assertThat(refreshTokenRepository.countByUserId(userId2)).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("빈 리스트로 호출 시 아무 작업도 수행하지 않음")
        void permanentlyDeleteUsers_EmptyList() {
            // given
            List<User> emptyList = Collections.emptyList();

            // when
            userCleanupService.permanentlyDeleteUsers(emptyList);

            // then
            verify(passwordOtpTokenRepository, never()).deleteAllByUserId(any());
            verify(passwordResetTokenRepository, never()).deleteAllByUserId(any());
            verify(refreshTokenRepository, never()).deleteAllByUserId(any());
            verify(teamMemberRepository, never()).findByUser_Id(any());
            verify(teamMemberService, never()).leave(any(), any());
            verify(profileRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("사용자 ID가 null인 경우에도 NPE 없이 처리")
        void permanentlyDeleteUsers_NullUserId() {
            // given
            User userWithNullId = User.create(
                    "테스트",
                    "아마추어",
                    "test@example.com",
                    "test@univ.ac.kr",
                    "password",
                    "kakao",
                    "공격수",
                    "테스트대학",
                    "학과",
                    "20",
                    "bio"
            );

            given(teamMemberRepository.findByUser_Id(null)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(userWithNullId));

            // then
            verify(passwordOtpTokenRepository, times(1)).deleteAllByUserId(null);
            verify(passwordResetTokenRepository, times(1)).deleteAllByUserId(null);
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(null);
            verify(profileRepository, times(1)).deleteById(null);
        }
    }

    @Nested
    @DisplayName("팀 탈퇴 처리")
    class TeamLeaveHandling {

        @Test
        @DisplayName("팀 탈퇴 시 올바른 팀 ID와 사용자 ID가 전달됨")
        void permanentlyDeleteUsers_CorrectTeamLeaveParameters() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            Long teamId = 100L;
            Team team = mock(Team.class);
            TeamMember teamMember = mock(TeamMember.class);

            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.of(teamMember));
            given(teamMember.getTeam()).willReturn(team);
            given(team.getTeamId()).willReturn(teamId);

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            verify(teamMemberService, times(1)).leave(teamId, userId);
        }

        @Test
        @DisplayName("팀 탈퇴는 프로필 삭제 이전에 실행됨")
        void permanentlyDeleteUsers_TeamLeaveBeforeProfileDeletion() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            Long teamId = 10L;
            Team team = mock(Team.class);
            TeamMember teamMember = mock(TeamMember.class);

            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.of(teamMember));
            given(teamMember.getTeam()).willReturn(team);
            given(team.getTeamId()).willReturn(teamId);

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            InOrder inOrder = inOrder(teamMemberService, profileRepository);
            inOrder.verify(teamMemberService).leave(teamId, userId);
            inOrder.verify(profileRepository).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("삭제 작업 호출 횟수 검증")
    class DeletionCallCount {

        @Test
        @DisplayName("각 토큰 저장소에서 정확히 한 번씩 삭제 호출")
        void permanentlyDeleteUsers_EachRepositoryCalledOnce() {
            // given
            User user = createUserWithId(1L);
            Long userId = 1L;
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(Collections.singletonList(user));

            // then
            verify(passwordOtpTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(passwordResetTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(profileRepository, times(1)).deleteById(userId);
        }

        @Test
        @DisplayName("3명의 사용자 삭제 시 각 저장소에서 3번씩 호출")
        void permanentlyDeleteUsers_ThreeUsers() {
            // given
            User user1 = createUserWithId(1L);
            User user2 = createUserWithId(2L);
            User user3 = createUserWithId(3L);

            Long userId1 = 1L;
            Long userId2 = 2L;
            Long userId3 = 3L;

            List<User> users = Arrays.asList(user1, user2, user3);

            given(teamMemberRepository.findByUser_Id(userId1)).willReturn(Optional.empty());
            given(teamMemberRepository.findByUser_Id(userId2)).willReturn(Optional.empty());
            given(teamMemberRepository.findByUser_Id(userId3)).willReturn(Optional.empty());

            // when
            userCleanupService.permanentlyDeleteUsers(users);

            // then
            verify(passwordOtpTokenRepository, times(3)).deleteAllByUserId(any());
            verify(passwordResetTokenRepository, times(3)).deleteAllByUserId(any());
            verify(refreshTokenRepository, times(3)).deleteAllByUserId(any());
            verify(profileRepository, times(3)).deleteById(any());
        }
    }

    private User createUserWithId(Long id) {
        User user = User.create(
                "김두리",
                "아마추어",
                "user" + id + "@test.com",
                "user" + id + "@univ.ac.kr",
                "password" + id,
                "kakao" + id,
                "공격수",
                "테스트대학교",
                "컴퓨터공학과",
                "20",
                "즐겜해요~"
        );

        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user id", e);
        }

        return user;
    }
}