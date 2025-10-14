package com.shootdoori.profile;

import com.shootdoori.match.config.UserCleanupScheduler;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.user.UserStatus;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.service.UserCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCleanupSchedulerTest {

    @Mock private ProfileRepository userRepository;
    @Mock private UserCleanupService userCleanupService;

    @InjectMocks private UserCleanupScheduler userCleanupScheduler;

    @Nested
    @DisplayName("정상 동작")
    class NormalOperation {

        @Test
        @DisplayName("성공 - 7일 이상 지난 삭제 대기 사용자 정리")
        void cleanupDeletedUsers_Success() {
            // given
            List<User> usersToDelete = Arrays.asList(createUser1(), createUser2());
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willReturn(usersToDelete);

            // when
            userCleanupScheduler.cleanupDeletedUsers();

            // then
            verify(userRepository, times(1))
                    .findByStatusAndStatusChangedAtBefore(
                            eq(UserStatus.DELETED),
                            any(LocalDateTime.class)
                    );
            verify(userCleanupService, times(1)).permanentlyDeleteUsers(usersToDelete);
        }

        @Test
        @DisplayName("정확히 7일 전 시점으로 조회")
        void cleanupDeletedUsers_ChecksSevenDaysAgo() {
            // given
            ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    dateCaptor.capture()
            )).willReturn(Collections.emptyList());

            // when
            userCleanupScheduler.cleanupDeletedUsers();

            // then
            LocalDateTime capturedDate = dateCaptor.getValue();
            LocalDateTime expectedDate = LocalDateTime.now().minusDays(7);

            assertThat(capturedDate).isCloseTo(expectedDate, within(1, java.time.temporal.ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("삭제할 사용자가 여러명일 때 모두 전달")
        void cleanupDeletedUsers_MultipleUsers() {
            // given
            User user1 = createUser1();
            User user2 = createUser2();
            List<User> usersToDelete = Arrays.asList(user1, user2);
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willReturn(usersToDelete);

            // when
            userCleanupScheduler.cleanupDeletedUsers();

            // then
            ArgumentCaptor<List<User>> usersCaptor = ArgumentCaptor.forClass(List.class);
            verify(userCleanupService).permanentlyDeleteUsers(usersCaptor.capture());

            List<User> capturedUsers = usersCaptor.getValue();
            assertThat(capturedUsers).hasSize(2);
            assertThat(capturedUsers).containsExactly(user1, user2);
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("삭제할 사용자가 없으면 cleanup 서비스를 호출하지 않음")
        void cleanupDeletedUsers_NoUsersToDelete() {
            // given
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willReturn(Collections.emptyList());

            // when
            userCleanupScheduler.cleanupDeletedUsers();

            // then
            verify(userRepository, times(1))
                    .findByStatusAndStatusChangedAtBefore(
                            eq(UserStatus.DELETED),
                            any(LocalDateTime.class)
                    );
            verify(userCleanupService, never()).permanentlyDeleteUsers(any());
        }

        @Test
        @DisplayName("단 한 명의 사용자만 삭제 대상일 때")
        void cleanupDeletedUsers_SingleUser() {
            // given
            List<User> usersToDelete = Collections.singletonList(createUser1());
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willReturn(usersToDelete);

            // when
            userCleanupScheduler.cleanupDeletedUsers();

            // then
            verify(userCleanupService, times(1)).permanentlyDeleteUsers(usersToDelete);
        }
    }

    @Nested
    @DisplayName("스케줄링 동작 검증")
    class SchedulingBehavior {

        @Test
        @DisplayName("조회 조건: DELETED 상태")
        void cleanupDeletedUsers_UsesPendingDeletionStatus() {
            // given
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willReturn(Collections.emptyList());

            // when
            userCleanupScheduler.cleanupDeletedUsers();

            // then
            verify(userRepository).findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            );
        }

        @Test
        @DisplayName("스케줄러가 여러 번 호출되어도 독립적으로 동작")
        void cleanupDeletedUsers_MultipleInvocations() {
            // given
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willReturn(Collections.singletonList(createUser1()));

            // when
            userCleanupScheduler.cleanupDeletedUsers();
            userCleanupScheduler.cleanupDeletedUsers();

            // then
            verify(userRepository, times(2))
                    .findByStatusAndStatusChangedAtBefore(
                            eq(UserStatus.DELETED),
                            any(LocalDateTime.class)
                    );
            verify(userCleanupService, times(2))
                    .permanentlyDeleteUsers(any());
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("UserRepository 조회 실패 시 예외 전파")
        void cleanupDeletedUsers_RepositoryException() {
            // given
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willThrow(new RuntimeException("Database error"));

            // when & then
            org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> userCleanupScheduler.cleanupDeletedUsers()
            );

            verify(userCleanupService, never()).permanentlyDeleteUsers(any());
        }

        @Test
        @DisplayName("UserCleanupService 실패 시 예외 전파")
        void cleanupDeletedUsers_ServiceException() {
            // given
            List<User> usersToDelete = Collections.singletonList(createUser1());
            given(userRepository.findByStatusAndStatusChangedAtBefore(
                    eq(UserStatus.DELETED),
                    any(LocalDateTime.class)
            )).willReturn(usersToDelete);

            doThrow(new RuntimeException("Cleanup error"))
                    .when(userCleanupService).permanentlyDeleteUsers(any());

            // when & then
            org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> userCleanupScheduler.cleanupDeletedUsers()
            );
        }
    }

    private User createUser1() {
        return User.create(
                "홍길동",
                "아마추어",
                "user1@univ.ac.kr",
                "password1",
                "kakao1",
                "공격수",
                "테스트대학교",
                "컴퓨터공학과",
                "20",
                "즐겜해요~"
        );
    }

    private User createUser2() {
        return User.create(
                "김철수",
                "세미프로",
                "user2@univ.ac.kr",
                "password2",
                "kakao2",
                "수비수",
                "테스트대학교",
                "체육학과",
                "21",
                "빡겜해요!"
        );
    }
}