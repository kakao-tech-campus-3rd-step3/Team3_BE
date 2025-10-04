package com.shootdoori.match.entity;

public enum UserSkillLevel {
    PRO("프로"),
    SEMI_PRO("세미프로"),
    AMATEUR("아마추어");

    private final String displayName;

    UserSkillLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserSkillLevel fromDisplayName(String displayName) {
        for (UserSkillLevel level : values()) {
            if (level.displayName.equals(displayName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown skill level: " + displayName);
    }
}

