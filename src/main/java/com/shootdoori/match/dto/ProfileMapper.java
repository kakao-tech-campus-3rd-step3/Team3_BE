package com.shootdoori.match.dto;

import com.shootdoori.match.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
    public ProfileResponse toProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        return new ProfileResponse(
            user.getName(),
            user.getUniversity(),
            user.getCreatedAt()
        );
    }
}
