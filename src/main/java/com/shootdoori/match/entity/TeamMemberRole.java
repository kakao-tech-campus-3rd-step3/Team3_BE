package com.shootdoori.match.entity;

public enum TeamMemberRole {
    LEADER("회장"),
    VICE_LEADER("부회장"),
    MEMBER("일반멤버");

    private final String displayName;

    TeamMemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TeamMemberRole fromDisplayName(String displayName) {
        for (TeamMemberRole role : values()) {
            if (role.displayName.equals(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + displayName);
    }
}