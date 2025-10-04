package com.shootdoori.match.entity.match.waiting;

public enum MatchWaitingStatus {
  WAITING("대기중"),
  MATCHED("완료"),
  CANCELED("취소"),
  EXPIRED("만료");

  private final String displayName;

  MatchWaitingStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static MatchWaitingStatus fromDisplayName(String displayName) {
    for (MatchWaitingStatus status : values()) {
      if (status.displayName.equals(displayName)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown match queue status: " + displayName);
  }
}