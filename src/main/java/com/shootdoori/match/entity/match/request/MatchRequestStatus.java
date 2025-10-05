package com.shootdoori.match.entity.match.request;

public enum MatchRequestStatus {
    PENDING("대기중"),
    ACCEPTED("수락"),
    REJECTED("거절"),
    CANCELED("취소");

    private final String displayName;

    MatchRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MatchRequestStatus fromDisplayName(String displayName) {
        for (MatchRequestStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown match application status: " + displayName);
    }
}
