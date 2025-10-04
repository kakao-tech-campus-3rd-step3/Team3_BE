package com.shootdoori.match.entity;

public enum MatchStatus {
    RECRUITING("모집중"),
    MATCHED("매칭완료"),
    FINISHED("경기완료"),
    CANCELED("취소");

    private final String displayName;

    MatchStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MatchStatus fromDisplayName(String displayName) {
        for (MatchStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown match status: " + displayName);
    }
}