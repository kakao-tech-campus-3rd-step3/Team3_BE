package com.shootdoori.match.entity;

public enum MatchQueueStatus {
  WAITING("대기중"),
  MATCHED("완료"),
  CANCELED("취소"),
  EXPIRED("만료");

  private final String displayName;

  MatchQueueStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static MatchQueueStatus fromDisplayName(String displayName) {
    for (MatchQueueStatus status : values()) {
      if (status.displayName.equals(displayName)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown match queue status: " + displayName);
  }
}