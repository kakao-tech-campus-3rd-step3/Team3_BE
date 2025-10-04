package com.shootdoori.match.config;

import com.shootdoori.match.entity.User;
import com.shootdoori.match.entity.UserStatus;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.service.UserCleanupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserCleanupScheduler {

    private final ProfileRepository userRepository;
    private final UserCleanupService userCleanupService;

    public UserCleanupScheduler(ProfileRepository userRepository,
                                UserCleanupService userCleanupService) {
        this.userRepository = userRepository;
        this.userCleanupService = userCleanupService;
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupDeletedUsers() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<User> usersToDelete = userRepository.findByStatusAndStatusChangedAtBefore(
            UserStatus.PENDING_DELETION, sevenDaysAgo
        );

        if (!usersToDelete.isEmpty()) {
            userCleanupService.permanentlyDeleteUsers(usersToDelete);
        }
    }
}