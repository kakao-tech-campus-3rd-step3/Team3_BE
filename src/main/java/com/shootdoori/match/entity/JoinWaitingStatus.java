package com.shootdoori.match.entity;

public enum JoinWaitingStatus {
    PENDING("대기중"),
    APPROVED("승인됨"),
    REJECTED("거절됨"),
    CANCELED("취소됨");

    private final String displayName;

    JoinWaitingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static JoinWaitingStatus fromDisplayName(String displayName) {
        for (JoinWaitingStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown join queue status: " + displayName);
    }

    public boolean isPending() {
        return this == PENDING;
    }
}
