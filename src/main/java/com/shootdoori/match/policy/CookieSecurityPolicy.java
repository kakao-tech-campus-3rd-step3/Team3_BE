package com.shootdoori.match.policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieSecurityPolicy {
    private final boolean isSecure;
    private final String activeProfile;

    public CookieSecurityPolicy(@Value("${spring.profiles.active:test}") String activeProfile) {
        this.activeProfile = activeProfile;
        this.isSecure = !"test".equals(activeProfile);
    }

    public boolean isSecure() {
        return isSecure;
    }

    public String getActiveProfile() {
        return activeProfile;
    }
}