package com.shootdoori.match.entity;

public enum JoinQueueStatus {
    PENDING("대기중"),
    APPROVED("승인됨"),
    REJECTED("거절됨"),
    CANCELED("취소됨");

    private final String displayName;

    JoinQueueStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static JoinQueueStatus fromDisplayName(String displayName) {
        for (JoinQueueStatus status : values()) {
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
