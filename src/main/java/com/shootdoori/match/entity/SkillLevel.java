package com.shootdoori.match.entity;

public enum SkillLevel {
    PRO("프로"),
    SEMI_PRO("세미프로"),
    AMATEUR("아마추어");

    private final String displayName;

    SkillLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SkillLevel fromDisplayName(String displayName) {
        for (SkillLevel level : values()) {
            if (level.displayName.equals(displayName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown skill level: " + displayName);
    }
}
