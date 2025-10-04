package com.shootdoori.match.entity.match.waiting;

public enum MatchWaitingSkillLevel {
    PRO("프로"),
    SEMI_PRO("세미프로"),
    AMATEUR("아마추어");

    private final String displayName;

    MatchWaitingSkillLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MatchWaitingSkillLevel fromDisplayName(String displayName) {
        for (MatchWaitingSkillLevel level : values()) {
            if (level.displayName.equals(displayName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown skill level: " + displayName);
    }
}

