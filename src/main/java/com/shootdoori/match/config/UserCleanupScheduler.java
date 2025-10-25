package com.shootdoori.match.config;

import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.user.UserStatus;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.service.UserCleanupService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;

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

        List<User> usersToDelete = userRepository.findBySoftDeleteStatusAndAuditUpdatedAtBefore(
            UserStatus.DELETED, sevenDaysAgo);

        if (usersToDelete != null && !usersToDelete.isEmpty()) {
            userCleanupService.permanentlyDeleteUsers(usersToDelete);
        }
    }
}